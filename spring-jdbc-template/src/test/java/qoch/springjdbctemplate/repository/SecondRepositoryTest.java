package qoch.springjdbctemplate.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import qoch.springjdbctemplate.model.First;
import qoch.springjdbctemplate.model.Second;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
//@Rollback(value = false)
@Transactional
@SpringBootTest
class SecondRepositoryTest {

    @Autowired
    SecondRepository secondRepository;

    @Test
    void save(){
        Second second = Second.builder().firstId(System.currentTimeMillis()).build();
        secondRepository.save(second);

        assertThat(second.getId()).isNotNull();
        assertThat(second.getId()).isGreaterThan(0);
    }
}