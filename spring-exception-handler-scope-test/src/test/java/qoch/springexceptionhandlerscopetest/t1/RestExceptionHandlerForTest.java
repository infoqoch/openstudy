package qoch.springexceptionhandlerscopetest.t1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestControllerAdvice
@Profile("test1")
public class RestExceptionHandlerForTest {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public Map<String, Object> exException(Exception e) {
        log.info("RestExceptionHandlerForTest working! exception message : {}", e.getMessage());
        return Map.of("message", "api error 발생! for test!~!");
    }
}
