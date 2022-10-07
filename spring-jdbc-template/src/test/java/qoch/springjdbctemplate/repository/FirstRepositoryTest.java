package qoch.springjdbctemplate.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import qoch.springjdbctemplate.model.First;

import java.util.Optional;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
// @Rollback(value = false)
@Transactional
@SpringBootTest
class FirstRepositoryTest {
    @Autowired
    FirstRepository firstRepository;

    @Test
    void save(){
        First first = First.builder().status(First.Status.NEW).build();
        firstRepository.save(first);

        assertThat(first.getId()).isNotNull();
        assertThat(first.getId()).isGreaterThan(0);

        Optional<First> result = firstRepository.findById(first.getId());
        assertThat(result).isPresent();
    }

    @Test
    void update_status(){
        First first = First.builder().status(First.Status.NEW).build();
        firstRepository.save(first);

        // when
        first.setStatus(First.Status.DONE);
        firstRepository.updateStatus(first);

        // when
        Optional<First> result = firstRepository.findById(first.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(First.Status.DONE);
    }

    @Test
    void count_by_id_and_status(){
        First first = First.builder().status(First.Status.NEW).build();
        firstRepository.save(first);

        // then 1
        assertThat(firstRepository.countByIdAndStatus(first.getId(), First.Status.NEW)).isEqualTo(1);
        assertThat(firstRepository.countByIdAndStatus(first.getId(), First.Status.DONE)).isEqualTo(0);

        // when 2
        first.setStatus(First.Status.DONE);
        firstRepository.updateStatus(first);

        // then 2
        assertThat(firstRepository.countByIdAndStatus(first.getId(), First.Status.NEW)).isEqualTo(0);
        assertThat(firstRepository.countByIdAndStatus(first.getId(), First.Status.DONE)).isEqualTo(1);
    }
}