package qoch.springjdbctemplate.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import qoch.springjdbctemplate.model.First;
import qoch.springjdbctemplate.model.Second;
import qoch.springjdbctemplate.repository.FirstRepository;
import qoch.springjdbctemplate.repository.SecondRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static qoch.springjdbctemplate.util.SleepUtil.sleep;

@Slf4j
@Rollback(value = false)
@Transactional
@SpringBootTest
class MyServiceTest {
    @Autowired
    FirstRepository firstRepository;
    @Autowired
    SecondRepository secondRepository;
    @Autowired
    MyService service;

    Long firstId;

    @BeforeEach
    void setUp(){
        // service = new MyService(firstRepository, secondRepository);
        firstId = getaLong();
    }

    @Value("spring.datasource.url")
    String URL;
    @Value("spring.datasource.username")
    String USERNAME;
    @Value("spring.datasource.password")
    String PASSWORD;

    @Test
    void working_right(){
        // given
        First first = First.builder().status(First.Status.NEW).build();
        firstRepository.save(first);
        MyRequest request = new MyRequest(first.getId());

        // when
        service.issueIfNewV1(request, 0);

        // then
        final List<Second> savedSecond = secondRepository.findByFirstId(first.getId());
        assertThat(savedSecond).size().isEqualTo(1);
    }

    @Test
    void working_right_thread() throws InterruptedException, ExecutionException, SQLException {
        // given
        sleep(1000);

        MyRequest request = new MyRequest(firstId);

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        List<Callable<String>> taskList = new ArrayList<>();
        taskList.add(() -> {
            log.info("first all size in mTh: {}", firstRepository.findAll().size());
            final String s = service.issueIfNewV1(request, 0);
            log.info("s : {}", s);
            return s;
        });
        final List<Future<String>> futures = executorService.invokeAll(taskList);

        sleep(1000);

        log.info("futures.size() = {}", futures.size());
        for (Future<String> future : futures) {
            log.info("get : {}", future.get());
        }
        taskList.clear();



        // then
        final List<Second> savedSecond = secondRepository.findByFirstId(firstId);
        assertThat(savedSecond).size().isEqualTo(1);
    }

    private Long getaLong() {
        First first = First.builder().status(First.Status.NEW).build();
        firstRepository.save(first);
        log.info("first all size : {}", firstRepository.findAll().size());

        final Long firstId = first.getId();
        return firstId;
    }


    @Test
    void no_delay(){
        // given
        First first = First.builder().status(First.Status.NEW).build();
        firstRepository.save(first);
        MyRequest request = new MyRequest(first.getId());

        final Thread th1 = new Thread(() -> service.issueIfNewV1(request, 0));
        final Thread th2 = new Thread(() -> service.issueIfNewV1(request, 0));

        // when
        th1.start();
        sleep(500);
        th2.start();

        // then
    }

}