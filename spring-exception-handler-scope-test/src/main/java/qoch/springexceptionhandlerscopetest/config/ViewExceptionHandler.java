package qoch.springexceptionhandlerscopetest.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import qoch.springexceptionhandlerscopetest.utils.HtmlRenderingUtil;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@ControllerAdvice
@Profile("default")
public class ViewExceptionHandler {

    @ExceptionHandler
    public void exException(Exception e, HttpServletResponse response) {
        log.info("ViewExceptionHandler working! exception message : {}", e.getMessage());
        response.setStatus(444);
        HtmlRenderingUtil.staticViewWriter(response, "static/view/myerror.html");
    }
}
