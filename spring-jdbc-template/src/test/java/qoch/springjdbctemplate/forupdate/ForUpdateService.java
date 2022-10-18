package qoch.springjdbctemplate.forupdate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import qoch.springjdbctemplate.domain.First;
import qoch.springjdbctemplate.domain.FirstRepository;

@Slf4j
@Service
@Transactional(isolation = Isolation.REPEATABLE_READ)
@RequiredArgsConstructor
public class ForUpdateService {
    private final FirstRepository firstRepository;

    public long updateStatus(First first, int sleep){
        log.info("[{}] before select for update", Thread.currentThread().getName());
        int count = firstRepository.countByIdAndStatusForUpdate(First.Status.NEW, first.getId());
        log.info("count : {}", count);
        sleep(sleep);
        log.info("[{}] before updateStatus", Thread.currentThread().getName());
        firstRepository.updateStatusNewToDone(first.getId());
        return System.currentTimeMillis();
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
            log.info("it finally has awakened!! millis : {}", millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
