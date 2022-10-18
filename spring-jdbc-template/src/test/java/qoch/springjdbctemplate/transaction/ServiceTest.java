package qoch.springjdbctemplate.transaction;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import qoch.springjdbctemplate.domain.First;
import qoch.springjdbctemplate.service.FirstService;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class ServiceTest {
    @Autowired
    FirstService firstService;

    @Test
    @DisplayName("@Transactional을 붙이지 않은 유닛 테스트는 service로 트랜잭션이 전파되지 않는다. " +
            " save메서드가 종료되며 트랜잭션 역시 commit 후 종료되었고, 이후 생성된 스레드는 db에서 해당 데이터를 찾을 수 있다. " +
            " 롤백되지 않는다. ")
    void transaction_not_propagated() throws ExecutionException, InterruptedException {
        First first = First.builder().status(First.Status.NEW).build();
        firstService.save(first);
        assertThat(first.getId()).isNotNull();

        // 쓰레드를 만들고 리포지토리에서 갓 삽입한 데이터를 찾는다.
        CompletableFuture<Optional<First>> future = CompletableFuture.supplyAsync(() -> firstService.findById(first.getId()));
        Optional<First> futureResult = future.get();
        assertThat(future.isDone()).isTrue();

        // 트랜잭션이 없으므로 메인 스레드의 save는 동작 후 커밋된다. 그러므로 이후 스레드에서는 insert 된 데이터를 찾을 수 있다.
        assertThat(futureResult).isPresent();
    }

    @Test
    @Transactional
    @DisplayName("@Transactional가 있는 유닛 테스트는 service로 트랜잭션이 전파된다(default : required)" +
            " 유닛 테스트(메인 스레드)가 종료되기 전까지 해당 데이터는 commit 되지 않고, 이후 발생한 스레드는 해당 데이터를 찾을 수 없다. " +
            " 롤백 된다.")
    void transaction_propagated_by_anno() throws ExecutionException, InterruptedException {
        First first = First.builder().status(First.Status.NEW).build();
        firstService.save(first);
        assertThat(first.getId()).isNotNull();

        // 쓰레드를 만들고 리포지토리에서 갓 삽입한 데이터를 찾는다.
        CompletableFuture<Optional<First>> future = CompletableFuture.supplyAsync(() -> firstService.findById(first.getId()));
        Optional<First> futureResult = future.get();
        assertThat(future.isDone()).isTrue();

        // 메인 스레드의 트랜잭션이 service#save에 전파되었다. 그러므로 다른 스레드는 데이터를 찾을 수 없다.
        assertThat(futureResult).isEmpty();
    }
}