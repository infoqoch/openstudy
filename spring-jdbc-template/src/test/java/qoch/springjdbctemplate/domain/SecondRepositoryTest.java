package qoch.springjdbctemplate.domain;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class SecondRepositoryTest {
    @Autowired
    SecondRepository secondRepository;

    @Test
    @Transactional
    void count_0(){
        assertThat(secondRepository.countByFirstId(ThreadLocalRandom.current().nextLong())).isEqualTo(0);
    }

    @Test
    @Transactional
    void save_and_count(){
        Second second = Second.builder().firstId(ThreadLocalRandom.current().nextLong()).build();
        secondRepository.save(second);

        assertThat(second.getId()).isNotNull();
        assertThat(second.getId()).isGreaterThan(0);

        assertThat(secondRepository.countByFirstId(second.getFirstId())).isEqualTo(1);
    }
}