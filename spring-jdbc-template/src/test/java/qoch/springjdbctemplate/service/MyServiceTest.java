package qoch.springjdbctemplate.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
public class MyServiceTest {
    @Autowired
    MyService myService;

    @Autowired
    FirstRepository firstRepository;

    @Autowired
    SecondRepository secondRepository;


    @Test
    @DisplayName("v1. 하나의 요청 - 정상 수행")
    void v1_one_request_success() throws ExecutionException, InterruptedException {
        // given
        First first = firstRepository.save(new First(null, First.Status.NEW));

        // when
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> myService.executeV1(first.getId(), 0));
        future.get();
        assertThat(future).isDone();

        // then
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first.getId())).isEqualTo(1);
        assertThat(secondRepository.countByFirstId(first.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("v1. 두 개의 동시 요청 - insert가 중복")
    void v1_two_request_duplicated() throws ExecutionException, InterruptedException {
        // given
        First first = firstRepository.save(new First(null, First.Status.NEW));

        // when
        Supplier<Long> run = () -> myService.executeV1(first.getId(), 1000);
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
    @DisplayName("v2. 두 개의 동시 요청 - 중복된 값 데드락")
    void v2_two_request_one_deadlock() {
        // given
        First first = firstRepository.save(new First(null, First.Status.NEW));

        // when
        Supplier<Long> run = () -> myService.executeV2(first.getId(), 1000);
        CompletableFuture<Long> f1 = CompletableFuture.supplyAsync(run);
        CompletableFuture<Long> f2 = CompletableFuture.supplyAsync(run);
        CompletableFuture<Void> future = CompletableFuture.allOf(f1, f2);

        assertThatThrownBy(()->
                future.get()
        ).isInstanceOf(Exception.class);

        // then
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first.getId())).isEqualTo(1);
        assertThat(secondRepository.countByFirstId(first.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("v2. 두 개의 다른 동시 요청 : 정상동작")
    void v2_two_diff_request_then_deadlock() throws ExecutionException, InterruptedException {
        // given
        First first1 = firstRepository.save(new First(null, First.Status.NEW));
        First first2 = firstRepository.save(new First(null, First.Status.NEW));

        // when
        CompletableFuture<Long> f1 = CompletableFuture.supplyAsync(() ->
                myService.executeV2(first1.getId(), 1000)
        );
        CompletableFuture<Long> f2 = CompletableFuture.supplyAsync(() ->
                myService.executeV2(first2.getId(), 1000)
        );

        CompletableFuture<Void> future = CompletableFuture.allOf(f1, f2);
        future.get();

        // then
        int count1 = firstRepository.countByIdAndStatus(First.Status.DONE, first1.getId());
        int count2 = firstRepository.countByIdAndStatus(First.Status.DONE, first2.getId());
        assertThat(count1 + count2).isEqualTo(2);

        int count11 = secondRepository.countByFirstId(first1.getId());
        int count22 = secondRepository.countByFirstId(first2.getId());
        assertThat(count11 + count22).isEqualTo(2);
    }

    @Test
    @DisplayName("v2. 두 개의 다른 동시 요청. 한 요청은 값이 없음 : 데드락 발생")
    void v2_two_diff_request_and_one_not_exist_then_deadlock() {
        // given
        long maybeNotExistFirstId = ThreadLocalRandom.current().nextLong(100000l);
        First first1 = new First(maybeNotExistFirstId, First.Status.NEW);
        First first2 = firstRepository.save(new First(null, First.Status.NEW));

        // when
        CompletableFuture<Long> f1 = CompletableFuture.supplyAsync(() ->
                myService.executeV2(first1.getId(), 1000)
        );
        CompletableFuture<Long> f2 = CompletableFuture.supplyAsync(() ->
                myService.executeV2(first2.getId(), 1000)
        );

        CompletableFuture<Void> future = CompletableFuture.allOf(f1, f2);

        assertThatThrownBy(() ->
                future.get()
        ).isInstanceOf(Exception.class);

        // then
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first1.getId())).isEqualTo(0);
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first2.getId())).isEqualTo(1);
        assertThat(secondRepository.countByFirstId(first1.getId())).isEqualTo(0);
        assertThat(secondRepository.countByFirstId(first2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("v3. 두 개의 다른 동시 요청. 한 요청은 값이 없음. for update 임에도 불구하고 동기적 행동 보장 안함 : 데드락 발생")
    void v3_two_diff_request_and_one_not_exist_then_deadlock() {
        // given
        long maybeNotExistFirstId = ThreadLocalRandom.current().nextLong(100000l);
        First first1 = new First(maybeNotExistFirstId, First.Status.NEW);
        First first2 = firstRepository.save(new First(null, First.Status.NEW));

        // when
        CompletableFuture<Long> f1 = CompletableFuture.supplyAsync(() ->
                myService.executeV3(first1.getId(), 1000)
        );
        CompletableFuture<Long> f2 = CompletableFuture.supplyAsync(() ->
                myService.executeV3(first2.getId(), 1000)
        );

        CompletableFuture<Void> future = CompletableFuture.allOf(f1, f2);

        assertThatThrownBy(()->
                future.get()
        ).isInstanceOf(Exception.class);

        // then
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first1.getId())).isEqualTo(0);
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first2.getId())).isEqualTo(1);
        assertThat(secondRepository.countByFirstId(first1.getId())).isEqualTo(0);
        assertThat(secondRepository.countByFirstId(first2.getId())).isEqualTo(1);

    }

    @Test
    @DisplayName("v4. 두 개의 다른 동시 요청. update affected rows로 해소")
    void v4_two_diff_request_and_one_not_exist_then_deadlock() {
        // given
        long maybeNotExistFirstId = ThreadLocalRandom.current().nextLong(100000l);
        First first1 = new First(maybeNotExistFirstId, First.Status.NEW);
        First first2 = firstRepository.save(new First(null, First.Status.NEW));

        // when
        CompletableFuture<Long> f1 = CompletableFuture.supplyAsync(() ->
                myService.executeV4(first1.getId(), 1000)
        );
        CompletableFuture<Long> f2 = CompletableFuture.supplyAsync(() ->
                myService.executeV4(first2.getId(), 1000)
        );

        CompletableFuture<Void> future = CompletableFuture.allOf(f1, f2);

        assertThatThrownBy(()->
                future.get()
        )
                .isInstanceOf(ExecutionException.class)
                .getCause().isInstanceOf(IllegalArgumentException.class)
                .message().startsWith("no first data with status NEW and first.id");

        assertThat(future).isDone();

        // then
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first1.getId())).isEqualTo(0);
        assertThat(firstRepository.countByIdAndStatus(First.Status.DONE, first2.getId())).isEqualTo(1);
        assertThat(secondRepository.countByFirstId(first1.getId())).isEqualTo(0);
        assertThat(secondRepository.countByFirstId(first2.getId())).isEqualTo(1);
    }

}
