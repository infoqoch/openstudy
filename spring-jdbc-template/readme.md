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

# 트랜잭션 테스트를 위한 고민
## 스레드 간 트랜잭션은 공유하지 않는다.
- 스프링은 트랜잭션 동기화 매니저를 제공한다. 이것은 쓰레드 로컬( ThreadLocal )을 사용해서 커넥션을 동기화해준다. 트랜잭션 매니저는 내부에서 이 트랜잭션 동기화 매니저를 사용한다.
- 트랜잭션 동기화 매니저는 쓰레드 로컬을 사용하기 때문에 멀티쓰레드 상황에 안전하게 커넥션을 동기화 할 수 있다. 따라서 커넥션이 필요하면 트랜잭션 동기화 매니저를 통해 커넥션을 획득하면 된다. 따라서 이전처럼 파라미터로 커넥션을 전달하지 않아도 된다
- 출처 : 김영한 db1

## @Service, @Repository는 @Transactional을 내포하지 않는다. 
- jpa 등 영속성 프레임워크에 따라 차이를 가질 수 있다.

## @Test의 트랜잭션 전파는 일반 빈의 동작과 동일하다.
- @Transactional @Repository 빈이 @Transactional @Service 빈에 주입될 경우, 하나의 트랜잭션이 된다. 
- @Transactional @Repository 빈이 @Transactional @Test 메서드에서 사용될 경우, 하나의 트랜잭션이 된다.

## 다중 스레드와 다중 트랜잭션에서의 테스트를 위해서는 @Test 메서드에 @Transactional이 선언되지 않는 것이 낫다
- 테스트 메서드에 트랜잭셔널이 선언될 경우 테스트가 어렵다. 왜냐하면 메인 스레드가 트랜잭션이 생기고, 이는 해당 메서드 종료 때까지 트랜잭션이 잡힌다. 이 말은 메인 스레드 내부에 생성된 스레드가 메인 스레드의 갱신 정보를 알 수 없기 때문이다.
- 그러므로 다중 스레드를 테스트 할 때는, 롤백되지 않은 불편함은 있지만, 가능하면 트랜잭셔널을 선언하지 않는다. ..

```java
@SpringBootTest
public class SomeTest {
  @Test
  @Transactional(REPEATABLE_READ) 
  void test(){
    Something something = new Something();
    repository.save(something); 
    CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
            repository.findById(something.getId()) // 메인 스레드의 트랜잭션이 종료되지 않아 영원히 찾을 수 없다. 
    );
  }    
}
```

## 그 외 
- MockMVC 는 트랜잭셔널이 먹힌다. 그 외 테스트 용 도구도 동작하겠지..? 
- @BeforeEach는 트랜잭션이 전파된다. 
