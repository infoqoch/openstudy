package com.example.unittestpollution.sample;

import com.example.unittestpollution.config.TestClassesOrder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@TestClassesOrder(1)
@SpringBootTest
@Slf4j
public class LoadSpringBootContextTest {
    @Test
    void loading_SpringBootContext(){
        log.info("🎇🎇🎇🎇🎇🎇LoadSpringBootContextTest#loading_SpringBootContext🎇🎇🎇🎇🎇🎇");
        assertThat(true).isTrue();
    }
}
