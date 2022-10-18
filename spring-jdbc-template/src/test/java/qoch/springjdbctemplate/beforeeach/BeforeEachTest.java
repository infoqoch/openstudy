package qoch.springjdbctemplate.beforeeach;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import qoch.springjdbctemplate.domain.First;
import qoch.springjdbctemplate.domain.FirstRepository;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
* 테스트 메서드 레벨에 @Transcational을 적용해서 실행하면 @BeforeEach 메서드에는 @Transactional이 적용되지 않는다.
* https://velog.io/@tmdgh0221/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%85%8C%EC%8A%A4%ED%8A%B8-%EC%BC%80%EC%9D%B4%EC%8A%A4%EC%97%90%EC%84%9C%EC%9D%98-Transactional-%EC%9C%A0%EC%9D%98%EC%A0%90
* 하지만 실제 도큐먼트에 보면 다음과 같다.
* "Note that @Transactional is not supported on test lifecycle methods — for example, methods annotated with JUnit Jupiter’s @BeforeAll, @BeforeEach, etc."
* 이 말은 BeforeEadch에 트랜잭션이 적용되지 않는다 보다는, 그냥 해당 메서드에는 @Transactioanl을 사용할 수 없다로 봐야 할 것 같다.
* 실제로 실제 테스트 메서드의 트랜잭션과 결합됨을 아래에서 확인할 수 있다.
*/
@Slf4j
@SpringBootTest
class BeforeEachTest {
    @Autowired
    FirstRepository firstRepository;

    First storedFirst;

    @BeforeEach
    void setUp(){
        storedFirst = First.builder().status(First.Status.NEW).build();
        firstRepository.save(storedFirst);
    }

    @Test
    @DisplayName("트랜잭셔널이 선언되지 않으면, 커밋된 값이 실제로 DB에 들어간다. 로그에 began 등 트랜잭션과 관련한 로그가 없다.")
    void non_transactional(){
        assertThat(firstRepository.findById(storedFirst.getId())).isPresent();
    }

    @Test
    @DisplayName("트랜잭셔널이 선언되면 트랜잭션 내부에서 동작한다. 롤백이 된다." +
            " 여기서의 중요한 특징은 DB에서 해당 데이터를 검색할 수 있다는 점이다. " +
            " 롤백이 되며 동시에 DB에서 조회가 된다는 의미는 BeforeEach의 코드블럭과 유닛테스트의 메인 스레드가 같은 트랜잭션으로 묶여 있다는 의미이기 때문이다.")
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    void transactional(){
        assertThat(firstRepository.findById(storedFirst.getId())).isPresent();
    }

    @Test
    @DisplayName("트랜잭셔널 상태에서 BeforeEach 블럭에서 갱신한 레코드에 대해, 유닛 테스트 블럭에서 생성한 스레드가 update를 할 경우 락이 걸린다." +
            " repeatable-read는 갱신 때 x락을 얻고 커밋/롤백 전까지 가지고 있는다. BeforeEach의 트랜잭션이 종료되지 않고 유지됨을 보여준다." +
            " 교착 상태를 유지한다. 예외 처리하지 않는다. ")
    @Transactional
    void transaction_propagated() {
        CompletableFuture<Void> f1 = CompletableFuture.runAsync(() ->
                firstRepository.updateStatusNewToDone(storedFirst.getId())
        );

        assertThatThrownBy(()->
            f1.get(2000, TimeUnit.MILLISECONDS)
        ).isInstanceOf(TimeoutException.class);
    }
}