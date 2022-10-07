package qoch.springjdbctemplate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import qoch.springjdbctemplate.model.First;
import qoch.springjdbctemplate.repository.FirstRepository;
import qoch.springjdbctemplate.repository.SecondRepository;

@Service
@RequiredArgsConstructor
public class MyService {
    private final FirstRepository firstRepository;
    private final SecondRepository secondRepository;

    public void issueIfNewV1(MyRequest request){
        if(firstRepository.countByIdAndStatus(request.getFirstId(), First.Status.NEW)==0)
            throw new IllegalArgumentException("!!");
        secondRepository.save(request.toSecond());
        firstRepository.updateStatus(request.toFirst(First.Status.DONE));
    }
}
