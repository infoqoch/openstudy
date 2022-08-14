package com.example.unittestpollution.sample;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StaticClass {
    public static String calledBy;

    public static void setCalledBy(String calledBy) {
        log.info("StaticClass#setCalledBy working!");
        StaticClass.calledBy = calledBy;
    }

    public static boolean isCalled(){
        log.info("StaticClass#isCalled");
        if(calledBy==null){
            log.info("never called");
            return false;
        }
        log.info("called by : {}", calledBy);
        return true;
    }
}
