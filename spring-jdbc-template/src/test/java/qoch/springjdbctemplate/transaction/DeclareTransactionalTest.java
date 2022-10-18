package qoch.springjdbctemplate.transaction;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import qoch.springjdbctemplate.service.FirstService;

import static org.assertj.core.api.Assertions.assertThat;

/*
* 트랜잭션 및 aop의 동작 여부를 판별하는 라이브러리 검토
*/
@Slf4j
@SpringBootTest
public class DeclareTransactionalTest {
    @Autowired
    FirstService firstService;

    // 클래스 전체에 선언하든 하나에만 선언하든 @Transactional만 있으면 true를 반환한다.
    @Test
    void declared_in_service() {
        assertThat(AopUtils.isAopProxy(firstService)).isTrue();
    }

    @Test
    @Transactional
    void declared(){
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
    }

    @Test
    void not_declared(){
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
    }
}
