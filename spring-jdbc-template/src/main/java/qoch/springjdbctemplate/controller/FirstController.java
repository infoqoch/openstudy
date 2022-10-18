package qoch.springjdbctemplate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import qoch.springjdbctemplate.domain.First;
import qoch.springjdbctemplate.domain.FirstRepository;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FirstController {
    private final FirstRepository firstRepository;

    @GetMapping("/save/first/new")
    public First saveFirstAsNew(){
        First first = First.builder().status(First.Status.NEW).build();
        sleep(1000);
        firstRepository.save(first);
        log.info("saved : {}", first);
        return first;
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
