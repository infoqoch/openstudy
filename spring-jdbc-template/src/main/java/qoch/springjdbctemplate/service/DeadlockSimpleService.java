package qoch.springjdbctemplate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import qoch.springjdbctemplate.domain.First;
import qoch.springjdbctemplate.domain.FirstRepository;
import qoch.springjdbctemplate.domain.Second;
import qoch.springjdbctemplate.domain.SecondRepository;

@Slf4j
@Service
@Transactional(isolation = Isolation.REPEATABLE_READ)
@RequiredArgsConstructor
public class DeadlockSimpleService {
    private final FirstRepository firstRepository;
    private final SecondRepository secondRepository;

    public long executeV1(Long firstId, int sleep){
        if(firstRepository.countByIdAndStatus(First.Status.NEW, firstId)==0)
            throw new IllegalArgumentException("first 테이블 중 상태가 NEW인 레코드가 없다. first.id : "+  firstId);

        sleep(sleep);

        log.info("before FirstRepository#updateStatusNewToDone | first.id : {}", firstId);
        firstRepository.updateStatusNewToDone(firstId);

        log.info("before SecondRepository#save | first.id : {}", firstId);
        secondRepository.save(Second.builder().firstId(firstId).build());

        return System.currentTimeMillis();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public long executeV2(Long firstId, int sleep){
        if(firstRepository.countByIdAndStatus(First.Status.NEW, firstId)==0)
            throw new IllegalArgumentException("first 테이블 중 상태가 NEW인 레코드가 없다. first.id : "+  firstId);

        sleep(sleep);

        log.info("before FirstRepository#updateStatusNewToDone | first.id : {}", firstId);
        firstRepository.updateStatusNewToDone(firstId);

        log.info("before SecondRepository#save | first.id : {}", firstId);
        secondRepository.save(Second.builder().firstId(firstId).build());

        return System.currentTimeMillis();
    }

    public long executeV3(Long firstId, int sleep){
        if(firstRepository.countByIdAndStatusForUpdate(First.Status.NEW, firstId)==0)
            throw new IllegalArgumentException("first 테이블 중 상태가 NEW인 레코드가 없다. first.id : "+  firstId);

        sleep(sleep);

        log.info("before FirstRepository#updateStatusNewToDone | first.id : {}", firstId);
        firstRepository.updateStatusNewToDone(firstId);

        log.info("before SecondRepository#save | first.id : {}", firstId);
        secondRepository.save(Second.builder().firstId(firstId).build());

        return System.currentTimeMillis();
    }

    public long executeV4(Long firstId, int sleep){
        log.info("before FirstRepository#updateStatusNewToDone | first.id : {}", firstId);
        if(firstRepository.updateStatusNewToDone(firstId)==0)
            throw new IllegalArgumentException("first 테이블 중 상태가 NEW인 레코드가 없다. first.id : "+  firstId);

        sleep(sleep);

        log.info("before SecondRepository#save | first.id : {}", firstId);
        secondRepository.save(Second.builder().firstId(firstId).build());

        return System.currentTimeMillis();
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
            log.info("awaken after {} millis", millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
