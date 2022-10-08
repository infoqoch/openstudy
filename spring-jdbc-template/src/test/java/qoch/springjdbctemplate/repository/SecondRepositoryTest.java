package qoch.springjdbctemplate.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import qoch.springjdbctemplate.model.Second;

import java.util.List;

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

    @Test
    void find_by_id(){
        final long firstId = System.currentTimeMillis();
        secondRepository.save(Second.builder().firstId(firstId).build());
        secondRepository.save(Second.builder().firstId(firstId).build());

        secondRepository.save(Second.builder().firstId(firstId+1).build());

        final List<Second> result = secondRepository.findByFirstId(firstId);
        assertThat(result).size().isEqualTo(2);
    }
}