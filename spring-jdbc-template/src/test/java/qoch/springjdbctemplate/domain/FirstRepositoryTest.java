package qoch.springjdbctemplate.domain;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class FirstRepositoryTest {
    @Autowired
    FirstRepository firstRepository;

    @Test
    @Transactional
    void save(){
        First first = First.builder().status(First.Status.NEW).build();
        firstRepository.save(first);

        assertThat(first.getId()).isNotNull();
        assertThat(first.getId()).isGreaterThan(0);

        Optional<First> result = firstRepository.findById(first.getId());
        assertThat(result).isPresent();
    }

    @Test
    @Transactional
    void count(){
        First first = First.builder().status(First.Status.NEW).build();
        firstRepository.save(first);
        assertThat(firstRepository.countByIdAndStatus(First.Status.NEW, first.getId()+1)).isEqualTo(0);

        firstRepository.save(First.builder().status(First.Status.NEW).build());
        assertThat(firstRepository.countByIdAndStatus(First.Status.NEW, first.getId()+1)).isEqualTo(1);
    }
}