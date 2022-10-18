package qoch.springjdbctemplate.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import qoch.springjdbctemplate.domain.First;
import qoch.springjdbctemplate.domain.FirstRepository;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MockMVCTest {
    @Autowired
    private WebApplicationContext ctx;

    @Autowired
    FirstRepository firstRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    @BeforeEach
    void init(){
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    @Test
    @DisplayName("@Transactional이 없으면 mockMVC는 트랜잭션을 사용하지 않는다.")
    void nonTransactional_main_thread() throws Exception {
        First first = getFirstWithMockMVC();

        // 쓰레드를 만들고 리포지토리에서 갓 삽입한 데이터를 찾는다.
        CompletableFuture<Optional<First>> future = CompletableFuture.supplyAsync(() -> firstRepository.findById(first.getId()));
        Optional<First> result = future.get();
        assertThat(future.isDone()).isTrue();

        // 다른 스레드에서 insert 및 commit 하여 이후 발생한 트랜잭션은 해당 데이터를 확인할 수 있다.
        assertThat(result).isPresent();
    }

    @Test
    @Transactional
    @DisplayName("@Transactional이 붙을 경우 mockMVC라도 트랜잭션으로 동작한다.")
    void transactional_main_thread() throws Exception {
        First first = getFirstWithMockMVC();

        // 쓰레드를 만들고 리포지토리에서 갓 삽입한 데이터를 찾는다.
        CompletableFuture<Optional<First>> future = CompletableFuture.supplyAsync(() -> firstRepository.findById(first.getId()));
        Optional<First> result = future.get();
        assertThat(future.isDone()).isTrue();

        // 다른 스레드에서 insert를 하였으나 해당 트랜잭션은 커밋하지 않아 다른 트랜잭션은 확인할 수 없다.
        assertThat(result).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("@Transactional을 붙이더라도 다른 스레드로는 트랜잭션이 전파되지 않는다.")
    void test() throws Exception {
        // 스레드가 분리된 상태에서 mockMVC를 사용하였다.
        First first = CompletableFuture.supplyAsync(() -> {
            try {
                return getFirstWithMockMVC();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).get();

        // 쓰레드를 만들고 리포지토리에서 갓 삽입한 데이터를 찾는다.
        CompletableFuture<Optional<First>> future = CompletableFuture.supplyAsync(() -> firstRepository.findById(first.getId()));
        Optional<First> result = future.get();
        assertThat(future.isDone()).isTrue();

        // 별도의 스레드에서 insert 및 commit 후 트랜잭션이 종료되었다.
        // 이후 새로 생성한 스레드에서 해당 데이터를 확인할 수 있다.
        assertThat(result).isPresent();
    }

    private First getFirstWithMockMVC() throws Exception {
        String jsonFirst = mockMvc.perform(MockMvcRequestBuilders.get("/save/first/new"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("status").value(equalTo("NEW")))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        First first = objectMapper.readValue(jsonFirst, First.class);
        return first;
    }
}