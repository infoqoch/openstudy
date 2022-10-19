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

@Slf4j
@SpringBootTest
public class DeadlockServiceTest {
    @Autowired
    DeadlockService service;

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

    @Test
    @DisplayName("v2. 두 개의 다른 요청 - 데드락 및 하나만 성공")
    void v2_two_diff_request_then_deadlock() {
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
        assertThatThrownBy(()->
                future.get())
                .isInstanceOf(ExecutionException.class)
                .getCause().isInstanceOf(DeadlockLoserDataAccessException.class);

        // then
        int count1 = firstRepository.countByIdAndStatus(First.Status.DONE, first1.getId());
        int count2 = firstRepository.countByIdAndStatus(First.Status.DONE, first2.getId());
        assertThat(count1 + count2).isEqualTo(1);

        int count11 = secondRepository.countByFirstId(first1.getId());
        int count22 = secondRepository.countByFirstId(first2.getId());
        assertThat(count11 + count22).isEqualTo(1);
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

    @Test
    @DisplayName("v3. 두 개의 다른 요청 - for update를 통해 동기적으로 정상 처리")
    void v3_two_diff_request_then_success_synchronously() throws ExecutionException, InterruptedException {
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

        assertThat(Math.abs(f1.get()-f2.get())).isGreaterThanOrEqualTo(1000l); // 동기적으로 동작한다.
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

        // 하나의 스레드가 동기적으로 먼저 처리되고 나머지는 first를 체크하면서 예외가 터진다.
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