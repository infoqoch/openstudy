package com.example.unittestpollution.config;

import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

import java.util.Collections;
import java.util.Comparator;

public class AnnotationTestsOrderConfig implements ClassOrderer {
    @Override
    public void orderClasses(ClassOrdererContext context) {
        Collections.sort(context.getClassDescriptors(), new Comparator<ClassDescriptor>() {
            @Override
            public int compare(ClassDescriptor o1, ClassDescriptor o2) {
                TestClassesOrder a1 = o1.getTestClass().getDeclaredAnnotation(TestClassesOrder.class);
                TestClassesOrder a2 = o2.getTestClass().getDeclaredAnnotation(TestClassesOrder.class);
                if (a1 == null) {
                    return 1;
                }

                if (a2 == null) {
                    return -1;
                }
                if (a1.value() < a2.value()) {
                    return -1;
                }

                if (a1.value() == a2.value()) {
                    return 0;
                }

                if (a1.value() > a2.value()) {
                    return 1;
                }
                return 0;
            }
        });
    }
}
