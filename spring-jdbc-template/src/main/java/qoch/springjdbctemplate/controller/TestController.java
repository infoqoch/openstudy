package qoch.springjdbctemplate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class TestController {
    @GetMapping("/hello")
    public Map<String, String> hello(){
        Map<String, String> map = new HashMap<>();
        map.put("hello", "world");
        return map;
    }
}
