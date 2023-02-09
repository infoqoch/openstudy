package qoch.springexceptionhandlerscopetest.t2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@ControllerAdvice(annotations = RestController.class)
@Profile("test2")
public class RestExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public @ResponseBody Map<String, Object> exException(Exception e) {
        log.info("RestExceptionHandler working! exception message : {}", e.getMessage());
        return Map.of("message", "api error 발생!");
    }
}
