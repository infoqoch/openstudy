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

# 트랜잭션을 테스트코드와 함께 테스트하는 과정에서 고려한 부분들
## @Test의 트랜잭션 전파는 일반 빈의 동작과 동일하다.
- @Transactional @Repository 빈이 @Transactional @Service 빈에 주입될 경우, 하나의 트랜잭션이 된다. 
- @Transactional @Repository 빈이 @Transactional @Test 메서드에서 사용될 경우, 하나의 트랜잭션이 된다.

## @Test 메서드에 @Transactional이 선언되지 않는 것이 낫다
- 테스트 메서드에 트랜잭셔널이 선언될 경우 테스트가 어렵다. 메인 스레드에 생성된 트랜잭션은 해당 메서드 종료 때까지 종료되지 않는다. 이 말은 메인 스레드가 insert한 데이터를 하위 트랜잭션이 select 쿼리로 접근하지 못한다. 
- 그러므로 초깃값 제공이 필요한 경우 트랜잭셔널을 선언하지 않는 것이 낫다. 

```java
@SpringBootTest
public class SomeTest {
  @Test
  @Transactional(REPEATABLE_READ) 
  void test(){
    Something something = new Something();
    repository.save(something); 
    CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
            repository.findById(something.getId()) // 메인 스레드의 트랜잭션이 종료되지 않아 db에서 찾을 수 없다. 
    );
  }    
}
```

## 그 외 
- MockMVC, @BeforeEach 등 많은 테스트 도구에서 트랜잭션이 동작했다. 
