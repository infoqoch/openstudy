package qoch.springexceptionhandlerscopetest.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE) // 해당 건이 없으면 다른 것이 먹기도.
@ControllerAdvice(annotations = RestController.class)
@Profile("default")
public class RestExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public @ResponseBody Map<String, Object> exException(Exception e) {
        log.info("RestExceptionHandler working! exception message : {}", e.getMessage());
        return Map.of("message", "api error 발생!");
    }
}
