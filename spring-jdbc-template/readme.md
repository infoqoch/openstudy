# DDL

```sql
USE test;

drop table if exists first;
CREATE TABLE `first` (
id bigint AUTO_INCREMENT PRIMARY KEY,
status varchar(100) 
)

drop table if exists second;
CREATE TABLE `second` (
id bigint AUTO_INCREMENT PRIMARY KEY,
first_id bigint 
)
```

# 트랜잭션 고민
## 스레드 간 트랜잭션은 공유하지 않는다.
- 스프링은 트랜잭션 동기화 매니저를 제공한다. 이것은 쓰레드 로컬( ThreadLocal )을 사용해서 커넥션을 동기화해준다. 트랜잭션 매니저는 내부에서 이 트랜잭션 동기화 매니저를 사용한다.
- 트랜잭션 동기화 매니저는 쓰레드 로컬을 사용하기 때문에 멀티쓰레드 상황에 안전하게 커넥션을 동기화 할 수 있다. 따라서 커넥션이 필요하면 트랜잭션 동기화 매니저를 통해 커넥션을 획득하면 된다. 따라서 이전처럼 파라미터로 커넥션을 전달하지 않아도 된다
- 출처 : 김영한 db1

## 메인스레드에서 생성된 스레드 역시 별도의 스레드이다.
- 메인 스레드에서 트랜잭션을 사용하더라도 트랜잭션을 공유하거나 전파할 수 없다.

## 기본적으로 @Service, @Repository 는 @Transactional을 내포하지 않는다.
- 트랜잭션을 사용하고 싶은 경우 @Transactional을 명시하지 않는다.
- 하지만 영속성 프레임워크나 어너테이션마다 차이를 가질 수 있다. (특히 jpa)

## 트랜잭션의 전파에 유의하여야 한다. 예를 들면 테스트할 빈에 @Transactional이 선언되었다 하더라도, 테스트에 전파됨을 보장하지 않는다 : 롤백되지 않는다.
- SomeService#save에 @Transactional을 선언한다 하더라도, 이를 테스트 할 테스트 유닛 메서드에 선언하지 않으면, 테스트 유닛 차원에서는 어떤 트랜잭션도 없다.
- (중요) 이 말은 다음과 같은 결과를 초래한다.
  - 유닛 테스트의 동작 결과는 롤백되지 않고 커밋된다.
  - 테스트 대상 메서드 내부에서는 트랜잭션이 동작한다.
- 테스트에 @Transactional 을 선언한다는 의미는 (default 값이 required의 경우) 이후 등장할 트랜잭션을 흡수하여 하나의 트랜잭션으로 사용한다는 의미와 같다. 유닛테스트의 결과를 롤백할 수 있다.

## 다중 스레드와 다중 트랜잭션에서의 트랜잭션 선언은 기본적으로 사용하지 않는 것이 낫다.
- 다중 스레드나 다중 트랜잭션 상황을 테스트를 할 경우, 기본적으로 트랜잭션을 사용하지 않는 것이 낫다. 그러니까...

```java
@SpringBootTest
public class SomeTest {
  @Test
  @Transactional(REPEATABLE_READ) 
  void test(){
    Something something = new Something();
    repository.save(something);
    CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
            repository.findById(something.getId())
    );
  }    
}
```

- 위와 같은 코드가 있을 경우, repository#save는 트랜잭셔널로 인하여 메인스레드에서 저장한 레코드를 db에서 찾을 수 없다.
- 메인스레드의 경우 트랜잭션을 사용하지 않고 바로 커밋하여, 환경 처럼 세팅하는 것이 낫다. 물론 유닛 테스트 종료 후 롤백되지 않으므로 후처리가 필요로 할 수 있다.

```java
import java.util.concurrent.CompletableFuture;

@SpringBootTest
public class SomeTest {
  @Test
  void test() {
    // given
    Something something = new Something();
    repository.save(something);
    
    // when
    // given 으로 설정한 상태를 이후로 트랜잭션이 생성된다. 다만 이로 인하여 롤백 처리를 따로 해야할 수 있다.
    CompletableFuture<Void> f1 = CompletableFuture.runAsync(/*... do something...*/);
    CompletableFuture<Void> f1 = CompletableFuture.runAsync(/*... do something...*/);
    CompletableFuture<Void> future = CompletableFuture.allOf(f1, f2);
    
    future.get();
  }
}
```

## 유닛 테스테에 선언한 트랜잭셔널은 생각보다 다양하게 사용 가능하다.
- 테스트를 해본 결과 MockMVC 테스트에 트랜잭셔널이 먹힌다.
- 아마 다른 테스트 방식도 먹히겠지.

# 별첨... 트랜잭션 관련한 테스트에서 영향을 받는 요소
- 트랜잭션의 경우 고려할 부분이 많다. 모든 부분을 고려하지 않으면 테스트 상황에서 혼란이 발생한다.
- 멀티 스레드 환경에서 트랜잭션과 관련해 고려해야 할 부분은 다음과 같다.
  - 스레드, 커넥션의 분리
  - 트랜잭션의 적용 여부
  - 트랜잭션의 전파 수준
  - 락이 걸리는 위치 (혹은 예외가 발생하는 위치)
  - 격리수준
- 내가 테스트 과정에서 계속 엉켰던 이유는 복잡한 상황에 대하여 분리하여 사고하지 못했기 때문이다.
- 특히 유닛 테스트에 트랜잭셔널을 사용하여 핸들링이 무척 어려웠다. 환경 조건으로 마련하고자 하였던 레코드가 메인스레드의 트랜잭션으로 묶여서, 생성한 스레드가 트랜잭션을 생성하더라도 메인스레드에서 생성한 레코드를 읽지 못했다.
- beforeeach를 사용하고자 하였지만, 이는 유닛테스트의 트랜잭션으로 전파된다. 그러니까 앞서의 동일한 문제가 발생한다.


---

# 중복 insert 및 데드락 테스트와 관련하여
## 변인
- 그냥 하나 요청
- 요청이 두 개
  - 동일한 요청
  - 다른 요청
  - 정상과 비정상(db에 없음)
- serializable
  - 데이터 정합성을 보장하나 다른 값을 가진 정상 요청 간 데드락 문제가 발생한다.
- for update
  - 인덱스가 아닌 값을 조회하거나 검색 결과가 없는 경우에만 for update를 하더라도 serializable과 같이 비배타적으로 락을 소유한다.
  - 다만 실제 어플리케이션 동작 과정에서는 해당 문제가 아래와 같은 이유로 발생하지 않았다. 데드락도 발생하지 않는다.
    - 검색결과가 없는 조건은 어플리케이션에서 예외 처리를 한다.
    - 인덱스에 걸리는 요청은 배타적인 락을 보장한다. (serializable로 할 경우 데드락이 걸리는 위치)
  - 다만 다루기 어렵다. 어플리케이션 개발 과정에서 락과 격리수준을 고려야해야 한다.
- update ... affected rows...
  - update 성공 여부에 따라 판단하므로 직관적이다. 트랜잭션이나 락에 대한 이해가 높지 않아도 된다.
  - update rows 를 통한 성공 여부는 select을 한 것과 진배 없다. 쿼리 하나를 하지 않는다.
  - 가장 단순하고 가장 성능이 좋다.