package qoch.springjdbctemplate.transaction;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import qoch.springjdbctemplate.domain.First;
import qoch.springjdbctemplate.domain.FirstRepository;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class RepositoryTest {
    @Autowired
    FirstRepository firstRepository;

    @Test
    @Transactional
    @DisplayName("@Transactional을 붙이면 이후 생성된 스레드는 해당 레코드를 읽을 수 없다. 트랜잭션이 커밋되지 않았기 때문이다.")
    void save_transactional() throws ExecutionException, InterruptedException {
        // 메인스레드에서 insert를 한다.
        First first = First.builder().status(First.Status.NEW).build();
        firstRepository.save(first);
        assertThat(first.getId()).isNotNull();

        // 쓰레드를 만들고 리포지토리에서 갓 삽입한 데이터를 찾는다.
        CompletableFuture<Optional<First>> future = CompletableFuture.supplyAsync(() -> firstRepository.findById(first.getId()));
        Optional<First> result = future.get();
        assertThat(future.isDone()).isTrue();

        // 다른 스레드에서 insert를 하였으나 해당 트랜잭션은 커밋하지 않아 다른 트랜잭션은 확인할 수 없다.
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("@Transactional이 없을 경우 바로 commit이 된다. 이후 생성된 스레드는 해당 데이터를 읽을 수 있다.")
    void save_non_transactional() throws ExecutionException, InterruptedException {
        // 메인스레드에서 insert를 한다.
        First first = First.builder().status(First.Status.NEW).build();
        firstRepository.save(first);
        assertThat(first.getId()).isNotNull();

        // 쓰레드를 만들고 리포지토리에서 갓 삽입한 데이터를 찾는다.
        CompletableFuture<Optional<First>> future = CompletableFuture.supplyAsync(() -> firstRepository.findById(first.getId()));
        Optional<First> result = future.get();
        assertThat(future.isDone()).isTrue();

        // 다른 스레드에서 insert 및 commit하였다. 이후 발생한 모든 트랜잭션은 읽을 수 있다.
        assertThat(result).isPresent();
    }
}