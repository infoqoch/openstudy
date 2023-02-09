package qoch.springexceptionhandlerscopetest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/rest")
public class RestsController {
    @GetMapping("/api")
    public ResponseEntity<Map<String, String>> greeting(){
        throw new RuntimeException("rest api 요청!");
    }
}
