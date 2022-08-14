package com.example.unittestpollution.sample;

import com.example.unittestpollution.config.TestClassesOrder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@TestClassesOrder(3)
@Slf4j
public class NothingTest {
    @Test
    void test(){
        log.info("hihihi");
    }
}
