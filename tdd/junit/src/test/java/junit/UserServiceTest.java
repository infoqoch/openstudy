package junit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

public class UserServiceTest {
    @Test
    void success_change_email(){
        // 실제 DB와 연결되는 UserDao 구현체가 아닌, Mockito를 대신 주입한다.
        // Mockito를 통하여 대역, 스파이 등의 행위를 정의할 수 있다.
        UserDao userDao = mock(UserDao.class);
        UserService userService = new UserService(userDao);

        // 대역
        // userDao.countById() 메서드를 실행할 경우 언제나 1을 응답한다.
        given(userDao.countById(anyString())).willReturn(1);

        final String email = "user.id@naver.com";
        userService.changeEmail("userId", email);

        // 스파이
        // 누군가 updateEmail 메서드를 호출하며, 그때 기입된 문자열이 변수 email과 동일함을 확인한다.
        then(userDao).should().updateEmail(anyString(), matches(email));
    }

    @Test
    void does_not_exist_then_throw_ex(){
        UserDao userDao = mock(UserDao.class);
        UserService userService = new UserService(userDao);

        // 이번에는 0을 리턴한다.
        // UserService.changeEmail은 dao에서 어떤 값도 찾지 못하면 예외를 던진다.
        given(userDao.countById(anyString())).willReturn(0);

        assertThatThrownBy(() -> userService.changeEmail("userId", "user.id@naver.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 아이디 입니다.");
    }
}