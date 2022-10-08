package qoch.springjdbctemplate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import qoch.springjdbctemplate.model.First;
import qoch.springjdbctemplate.repository.FirstRepository;
import qoch.springjdbctemplate.repository.SecondRepository;

import static qoch.springjdbctemplate.util.SleepUtil.sleep;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyService {
    private final FirstRepository firstRepository;
    private final SecondRepository secondRepository;

    public String issueIfNewV1(MyRequest request, int delay){
        log.info("issueIfNew, request : {}", request);
        final int result = firstRepository.countByIdAndStatus(request.getFirstId(), First.Status.NEW);
        System.out.println("result = " + result);
        if(result ==0)
            throw new IllegalArgumentException("!!");
        sleep(delay);
        secondRepository.save(request.toSecond());
        firstRepository.updateStatus(request.toFirst(First.Status.DONE));
        return "good";
    }
}
