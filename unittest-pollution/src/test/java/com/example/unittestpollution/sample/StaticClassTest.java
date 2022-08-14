package com.example.unittestpollution.sample;

import com.example.unittestpollution.config.TestClassesOrder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@TestClassesOrder(2)
@Slf4j
class StaticClassTest {
    @Test
    void staticMethod_calling(){
        log.info("🎇🎇🎇🎇🎇🎇StaticClassTest#staticMethod_calling🎇🎇🎇🎇🎇🎇");
        final boolean called = StaticClass.isCalled();
        assertThat(called).isFalse();
    }
}