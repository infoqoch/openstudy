package qoch.springjdbctemplate.forupdate;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import qoch.springjdbctemplate.domain.First;
import qoch.springjdbctemplate.domain.FirstRepository;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

// ForUpdateRepositoryTest 에서는 트랜잭션 상태에서 for update가 해당 select 에 대한 배타적인 락의 획득을 실패함을 확인하였다.
@Slf4j
@SpringBootTest
public class ForUpdateServiceTest {
    @Autowired
    ForUpdateService forUpdateService;

    @Autowired
    FirstRepository firstRepository;

    @Test
    @DisplayName("for update는 존재하는 값에 대해서는 동기적 동작을 보장한다.")
    void if_exist_then_working_synchronously() throws ExecutionException, InterruptedException {
        /*  !! 중요 !!  */
        // db에 실제로 존재하는 레코드를 준비해야 한다. 테스트 코드에서 생성하려 하였으나 before each는 유닛 테스트의 트랜잭션에 전파되어 정상적으로 동작하지 않는다.
        First first = alreadyInsertedRecordAndNew(206l);

        // when
        Supplier<Long> call = () -> forUpdateService.updateStatus(first, 1000);
        CompletableFuture<Long> f1 = CompletableFuture.supplyAsync(call);
        CompletableFuture<Long> f2 = CompletableFuture.supplyAsync(call);
        CompletableFuture<Void> future = CompletableFuture.allOf(f1, f2);

        sleep(100);
        future.get();

        long diff = f2.get() - f1.get();
        assertThat(Math.abs(diff)).isGreaterThanOrEqualTo(1000l);
    }

    @Test
    @DisplayName("for update는 존재하지 않는 값에 대해서 동기적 동작을 보장하지 않는다. 두 개 스레드의 시간 차이가 슬립을 초과하는 시간이 걸리지 않았다는 것으로 미루어 볼 수 있다.")
    void if_not_exist_then_deadlock() throws ExecutionException, InterruptedException {
        // given
        // 존재하지 않는 값
        First first = First.builder().status(First.Status.NEW).id(ThreadLocalRandom.current().nextLong(10000l)).build();

        // when
        Supplier<Long> call = () -> forUpdateService.updateStatus(first, 1000);
        CompletableFuture<Long> f1 = CompletableFuture.supplyAsync(call);
        CompletableFuture<Long> f2 = CompletableFuture.supplyAsync(call);

        CompletableFuture<Void> future = CompletableFuture.allOf(f1, f2);

        // then
        // 존재하지 않는 값에 대해서는 select에 대한 배타적인 락을 보장하지 않는다.
        // 그러므로 두 개의 수행 시간을 뺀 값은 1000을 초과하지 않는다( !중요! 인프라스트럭쳐가 해당 시간 이하로 동작함을 보장해야한다.)
        future.get();
        long diff = f2.get() - f1.get();
        assertThat(Math.abs(diff)).isLessThan(1000l);
    }

    private First alreadyInsertedRecordAndNew(long existFirstId) {
        Optional<First> op = firstRepository.findById(existFirstId);
        assert op.isPresent();
        return op.get();
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
            log.info("it finally has awakened!! millis : {}", millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
