package qoch.springexceptionhandlerscopetest.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import qoch.springexceptionhandlerscopetest.utils.HtmlRenderingUtil;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/view")
public class ViewController {

    @GetMapping("/page")
    public String greeting(){
        throw new RuntimeException("view page 요청!");
    }

    @GetMapping("/api")
    public @ResponseBody ResponseEntity<Map<String, String>> api(){
        throw new RuntimeException("view - api의 ResponseBody 요청!");
    }
}
