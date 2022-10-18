package qoch.springjdbctemplate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import qoch.springjdbctemplate.domain.First;
import qoch.springjdbctemplate.domain.FirstRepository;

import java.util.Optional;


@Slf4j
@Service
@Transactional // JPA는 묵시적. 다른 프레임워크는 선언해야 한다.
@RequiredArgsConstructor
public class FirstService {
    private final FirstRepository firstRepository;

    public First save(First first){
        log.info("TransactionSynchronizationManager.isActualTransactionActive() : {}", TransactionSynchronizationManager.isActualTransactionActive());
        return firstRepository.save(first);
    }

    public Optional<First> findById(Long id) {
        return firstRepository.findById(id);
    }
}
