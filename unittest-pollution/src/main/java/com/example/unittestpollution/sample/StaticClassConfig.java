package com.example.unittestpollution.sample;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StaticClassConfig {
    @Bean
    InitializingBean initializingBean(){
        return () -> StaticClass.setCalledBy("StaticClassConfig#initializingBean");
    }

}
