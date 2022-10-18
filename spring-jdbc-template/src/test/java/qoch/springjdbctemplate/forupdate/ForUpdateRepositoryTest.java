package qoch.springjdbctemplate.forupdate;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import qoch.springjdbctemplate.domain.First;
import qoch.springjdbctemplate.domain.FirstRepository;

import java.util.concurrent.*;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
class ForUpdateRepositoryTest {
    @Autowired
    FirstRepository firstRepository;

    @Test
    @DisplayName("트랜잭션이 아닌 상태에서 스레드를 분리하고 for update를 할 경우 특별한 문제가 없다.")
    void for_update_multi_thread_and_non_transaction() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        First first = First.builder().status(First.Status.NEW).build();
        firstRepository.save(first);

        // when
        Supplier<Long> call = () -> {
            log.info("[{}] before select for update", Thread.currentThread().getName());
            int count = firstRepository.countByIdAndStatusForUpdate(First.Status.NEW, first.getId());
            log.info("count : {}", count);
            sleep(1000);
            log.info("[{}] before updateStatus", Thread.currentThread().getName());
            firstRepository.updateStatusNewToDone(first.getId());
            return System.currentTimeMillis();
        };

        CompletableFuture<Long> future = CompletableFuture.supplyAsync(call);
        future.get(3000, TimeUnit.MILLISECONDS);
    }

    @Test
    @Transactional
    @DisplayName("두 트랜잭션이 갱신으로 인한 x락 교착상태에 빠진다.")
    void for_update_deadlock_x_lock_hold() {
        // given
        First first = First.builder().status(First.Status.NEW).build();
        firstRepository.save(first);

        // when
        Supplier<Long> call = () -> {
            firstRepository.updateStatusNewToDone(first.getId());
            return System.currentTimeMillis();
        };

        CompletableFuture<Long> future = CompletableFuture.supplyAsync(call);

        assertThatThrownBy(()->
                future.get(1000, TimeUnit.MILLISECONDS)
        ).isInstanceOf(TimeoutException.class);
    }
    
    @Test
    @DisplayName("트랜잭션 상태에서 for update로 없는 값을 탐색할 경우, select 락을 여러 트랜잭션이 소유할 수 있다. 이때 갱신을 하려면 교착상태에 빠진다. " +
            " 검색 결과가 없는 경우 select 에 배타적 락을 부여하여 동기적으로 처리할 수 없다.")
    @Transactional
    void for_update_transaction_multiple_select_allowed_when_result_nothing() {
        // given
        // select for udpate를 통해 select에 대한 배타적 락을 획득하여 동기적 처리를 기대한다.
        First first = First.builder().status(First.Status.NEW).id(ThreadLocalRandom.current().nextLong(10000l)).build();
        int count = firstRepository.countByIdAndStatusForUpdate(First.Status.NEW, first.getId());
        assertThat(count).isEqualTo(0); // DB에 존재하지 않는 레코드

        // when
        Supplier<Long> call = () -> {
            log.info("before select for update");
            // 하지만 실제로는 select에 대한 락이 걸리지 않고 아래의 메서드를 통과한다.
            int count2 = firstRepository.countByIdAndStatusForUpdate(First.Status.NEW, first.getId());
            log.info("count2 : {}", count2);
            // 테이블의 모든 레코드에 대한 락을 두 개의 트랜잭션이 가지고 있고, 한 트랜잭션이 갱신을 시도하자 락이 걸린다.
            firstRepository.save(first);
            return System.currentTimeMillis();
        };

        // then
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(call);
        assertThatThrownBy(()->
                future.get(1000, TimeUnit.MILLISECONDS)
        ).isInstanceOf(TimeoutException.class);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}