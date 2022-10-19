package qoch.springjdbctemplate.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DeadlockLoserDataAccessException;
import qoch.springjdbctemplate.domain.First;
import qoch.springjdbctemplate.domain.FirstRepository;
import qoch.springjdbctemplate.domain.SecondRepository;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
* second 를 select 하여 유효성을 검사하는 로직이 없앴다.
*
* 기존의 로직은....
* - first 를 체크하는 로직은 갯수가 1 이어야 한다. second 를 체크하는 로직은 갯수가 0 이어야 한다.
* - 두 개의 차이로 인하여 락의 소유가 문제가 달라진다.
* - serializable(s락)과 for update(x락)은 first에 값이 있으면 second에 값이 없어야 하므로 최소한 second에 락이 걸릴 가능성을 내포한다.
*
* 지금의 로직은..
* - first에 값이 있으면 인덱스를 기준으로 조회했기 때문에 레코드락이 걸린다.
* - serializable을 사용할 경우
*   - 다른 요청에 대하여 락이 발생하지 않는다. 트랜잭션 별 락을 소유한 레코드가 다르기 때문이다.
*   - 같은 요청에 대해서는 락이 발생한다. 데드락이 발생한다.
* - for update 의 경우
*   -  select에 대한 배타적 락 때문에 대기한다.
* - 어찌 됐든 데드락 보다는 for update의 대기 이후 발생하는 로직 내부의 IllegalArgumentException 이 더 보기 좋아 보인다.
*/
@Slf4j
@SpringBootTest
public class DeadlockSimpleServiceTest {
    @Autowired
    DeadlockSimpleService service;

    @Autowired
    FirstRepository firstRepository;

    @Autowired
    SecondRepository secondRepository;

    @Test
    @DisplayName("v1. 하나의 요청 - 정상 수행")
    void v1_one_request_then_success() throws ExecutionException, InterruptedException {
        // given
        First first = firstRepository.save(new First(null, First.Status.NEW));

        // when
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> service.executeV1(first.getId(), 0));
        future.get();
        assertThat(future).isDone();

        // then
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first.getId())).isEqualTo(1);
        assertThat(secondRepository.countByFirstId(first.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("v1. 두 개의 같은 요청 - insert 중복")
    void v1_two_request_then_duplicated_insert() throws ExecutionException, InterruptedException {
        // given
        First first = firstRepository.save(new First(null, First.Status.NEW));

        // when
        Supplier<Long> run = () -> service.executeV1(first.getId(), 1000);
        CompletableFuture<Long> f1 = CompletableFuture.supplyAsync(run);
        CompletableFuture<Long> f2 = CompletableFuture.supplyAsync(run);
        CompletableFuture<Void> future = CompletableFuture.allOf(f1, f2);

        future.get();
        assertThat(future).isDone();

        // then
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first.getId())).isEqualTo(1);
        assertThat(secondRepository.countByFirstId(first.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("v2. 두 개의 같은 요청 - 하나는 데드락에 걸리며 정상 처리")
    void v2_two_request_then_one_deadlock_one_success() {
        // given
        First first = firstRepository.save(new First(null, First.Status.NEW));

        // when
        Supplier<Long> run = () -> service.executeV2(first.getId(), 1000);
        CompletableFuture<Long> f1 = CompletableFuture.supplyAsync(run);
        CompletableFuture<Long> f2 = CompletableFuture.supplyAsync(run);
        CompletableFuture<Void> future = CompletableFuture.allOf(f1, f2);

        assertThatThrownBy(()->
                future.get())
                .isInstanceOf(ExecutionException.class)
                .getCause().isInstanceOf(DeadlockLoserDataAccessException.class);

        // then
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first.getId())).isEqualTo(1);
        assertThat(secondRepository.countByFirstId(first.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("v2. 한 개의 db에 존재하지 않는 비정상 요청, 한 개의 정상 요청 - 하나는 예외 및 무시, 하나는 정상")
    void v2_two_request_and_one_not_exist_then_success() {
        // given
        long maybeNotExistFirstId = ThreadLocalRandom.current().nextLong(100000l);
        First first1 = new First(maybeNotExistFirstId, First.Status.NEW);
        First first2 = firstRepository.save(new First(null, First.Status.NEW));

        // when
        CompletableFuture<Long> f1 = CompletableFuture.supplyAsync(() ->
                service.executeV2(first1.getId(), 1000)
        );
        CompletableFuture<Long> f2 = CompletableFuture.supplyAsync(() ->
                service.executeV2(first2.getId(), 1000)
        );

        CompletableFuture<Void> future = CompletableFuture.allOf(f1, f2);

        assertThatThrownBy(() ->
                future.get())
                .isInstanceOf(ExecutionException.class)
                .getCause().isInstanceOf(IllegalArgumentException.class);

        // then
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first1.getId())).isEqualTo(0);
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first2.getId())).isEqualTo(1);
        assertThat(secondRepository.countByFirstId(first1.getId())).isEqualTo(0);
        assertThat(secondRepository.countByFirstId(first2.getId())).isEqualTo(1);
    }

    /*
    * second select이 없을 경우, first select의 인덱스가 걸린 쿼리를 한다. 결과적으로 레코드락만 걸리며 중복 처리가 정상적으로 수행된다.
    */
    @Test
    @DisplayName("v2. 두 개의 다른 요청 - 데드락 및 하나만 성공")
    void v2_two_diff_request_then_success() throws ExecutionException, InterruptedException {
        // given
        First first1 = firstRepository.save(new First(null, First.Status.NEW));
        First first2 = firstRepository.save(new First(null, First.Status.NEW));

        // when
        CompletableFuture<Long> f1 = CompletableFuture.supplyAsync(() ->
                service.executeV2(first1.getId(), 1000)
        );
        CompletableFuture<Long> f2 = CompletableFuture.supplyAsync(() ->
                service.executeV2(first2.getId(), 1000)
        );

        CompletableFuture<Void> future = CompletableFuture.allOf(f1, f2);
        future.get();

        // then
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first1.getId())).isEqualTo(1);
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first2.getId())).isEqualTo(1);
        assertThat(secondRepository.countByFirstId(first1.getId())).isEqualTo(1);
        assertThat(secondRepository.countByFirstId(first2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("v3. 두 개의 같은 요청 - 하나는 예외 걸리며 정상 처리 - 동기적으로 처리됨")
    void v3_two_request_then_one_success_one_ex_synchronously() {
        // given
        First first = firstRepository.save(new First(null, First.Status.NEW));

        // when
        Supplier<Long> run = () -> service.executeV3(first.getId(), 1000);
        CompletableFuture<Long> f1 = CompletableFuture.supplyAsync(run);
        CompletableFuture<Long> f2 = CompletableFuture.supplyAsync(run);
        CompletableFuture<Void> future = CompletableFuture.allOf(f1, f2);

        // 하나의 스레드가 먼저 처리되고 나머지는 first를 체크하면서 예외가 터진다. 동기적으로 동작한다.
        assertThatThrownBy(()->
                future.get())
                .isInstanceOf(ExecutionException.class)
                .getCause().isInstanceOf(IllegalArgumentException.class);

        // then
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first.getId())).isEqualTo(1);
        assertThat(secondRepository.countByFirstId(first.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("v3. 한 개의 db에 존재하지 않는 비정상 요청, 한 개의 정상 요청 - 하나는 예외 및 무시, 하나는 정상")
    void v3_two_request_and_one_not_exist_then_success() {
        // given
        long maybeNotExistFirstId = ThreadLocalRandom.current().nextLong(100000l);
        First first1 = new First(maybeNotExistFirstId, First.Status.NEW);
        First first2 = firstRepository.save(new First(null, First.Status.NEW));

        // when
        CompletableFuture<Long> f1 = CompletableFuture.supplyAsync(() ->
                service.executeV3(first1.getId(), 1000)
        );
        CompletableFuture<Long> f2 = CompletableFuture.supplyAsync(() ->
                service.executeV3(first2.getId(), 1000)
        );

        CompletableFuture<Void> future = CompletableFuture.allOf(f1, f2);

        assertThatThrownBy(() ->
                future.get())
                .isInstanceOf(ExecutionException.class)
                .getCause().isInstanceOf(IllegalArgumentException.class);

        // then
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first1.getId())).isEqualTo(0);
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first2.getId())).isEqualTo(1);
        assertThat(secondRepository.countByFirstId(first1.getId())).isEqualTo(0);
        assertThat(secondRepository.countByFirstId(first2.getId())).isEqualTo(1);
    }

    /*
    * 이전과 차이나는 지점!!
    * 인덱스를 검색하되 레코드가 다르므로, 각자 레코드에 대한 락이 걸림 : 동기적 수행의 이유가 없음.
    */
    @Test
    @DisplayName("v3. 두 개의 다른 요청 - 인덱스가 있는 검색(where)을 하여 각자 다른 레코드락이 걸린다 : 동기적으로 처리하지 않는다.  ")
    void v3_two_diff_request_then_success() throws ExecutionException, InterruptedException {
        // given
        First first1 = firstRepository.save(new First(null, First.Status.NEW));
        First first2 = firstRepository.save(new First(null, First.Status.NEW));

        // when
        CompletableFuture<Long> f1 = CompletableFuture.supplyAsync(() ->
                service.executeV3(first1.getId(), 1000)
        );
        CompletableFuture<Long> f2 = CompletableFuture.supplyAsync(() ->
                service.executeV3(first2.getId(), 1000)
        );

        CompletableFuture<Void> future = CompletableFuture.allOf(f1, f2);
        future.get();

        // then
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first1.getId())).isEqualTo(1);
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first2.getId())).isEqualTo(1);
        assertThat(secondRepository.countByFirstId(first1.getId())).isEqualTo(1);
        assertThat(secondRepository.countByFirstId(first2.getId())).isEqualTo(1);

        assertThat(Math.abs(f1.get()-f2.get())).isLessThan(1000l); // 동기적으로 동작한다.
    }

    @Test
    @DisplayName("v4. 두 개의 같은 요청 - 하나는 예외 걸리며 정상 처리")
    void v4_two_request_then_success(){
        // given
        First first = firstRepository.save(new First(null, First.Status.NEW));

        // when
        Supplier<Long> run = () -> service.executeV4(first.getId(), 1000);
        CompletableFuture<Long> f1 = CompletableFuture.supplyAsync(run);
        CompletableFuture<Long> f2 = CompletableFuture.supplyAsync(run);
        CompletableFuture<Void> future = CompletableFuture.allOf(f1, f2);

        // 하나의 스레드가 먼저 처리되고 나머지는 first를 체크하면서 예외가 터진다.
        assertThatThrownBy(()->
                future.get())
                .isInstanceOf(ExecutionException.class)
                .getCause().isInstanceOf(IllegalArgumentException.class);

        // then
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first.getId())).isEqualTo(1);
        assertThat(secondRepository.countByFirstId(first.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("v4. 두 개의 다른 요청 - 정상처리")
    void v4_two_diff_request_then_success() throws ExecutionException, InterruptedException {
        // given
        First first1 = firstRepository.save(new First(null, First.Status.NEW));
        First first2 = firstRepository.save(new First(null, First.Status.NEW));

        // when
        CompletableFuture<Long> f1 = CompletableFuture.supplyAsync(() ->
                service.executeV4(first1.getId(), 1000)
        );
        CompletableFuture<Long> f2 = CompletableFuture.supplyAsync(() ->
                service.executeV4(first2.getId(), 1000)
        );

        CompletableFuture<Void> future = CompletableFuture.allOf(f1, f2);
        future.get();

        // then
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first1.getId())).isEqualTo(1);
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first2.getId())).isEqualTo(1);
        assertThat(secondRepository.countByFirstId(first1.getId())).isEqualTo(1);
        assertThat(secondRepository.countByFirstId(first2.getId())).isEqualTo(1);
    }
}