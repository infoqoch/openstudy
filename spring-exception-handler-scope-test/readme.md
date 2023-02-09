# @RestControllerAdvice 와 @ControllerAdvice 는 같이 사용 가능한가?
## 같이 사용하면 동작하지 않는다.
- test의 qoch.springexceptionhandlerscopetest.t1 와 t2 참고. 
- @RestControllerAdvice가 @ControllerAdvice를 잡아 먹는다. 그러니까 view에 대한 예외를 rest api로 응답한다.  

## @ControllerAdvice로만 정의한다.
- @RestControllerAdvice를 사용하지 않고 @ControllerAdvice 두 개를 만들었다. 
- @RestControllerAdvice 대신 `@ControllerAdvice(annotations = RestController.class)`로 정의하였다.
- 우선순위를 위하여 `@Order(Ordered.HIGHEST_PRECEDENCE)`를 붙였다.
- 기대하는 방향으로 동작한다.

## 이유는 모른다.
- 음.. 이유는 모른다^^

# view template 없이 static html 만들기
- 타임리프나 JSP 없이 어떻게 구현할지 고민하였음. 
- 오래된 전통에 따라 `response.getWriter()` 로 하였음!