package qoch.springexceptionhandlerscopetest.t1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test1")
public class WithRestAdviceTest {

    @BeforeEach
    void init(WebApplicationContext ctx){
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    private MockMvc mockMvc;

    @Test
    void rest_api() throws Exception {
        mockMvc.perform(get("/rest/api"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("{\"message\":\"api error 발생! for test!~!\"}"));
    }

    @Test
    @DisplayName("ControllerAdvice가 동작하기를 기대하였으나 RestControllerAdvice가 동작한다. 테스트에 실패한다. (1)")
    void view_page() throws Exception {
        mockMvc.perform(get("/view/page"))
                .andDo(print())
                .andExpect(status().is(444))
                .andExpect(content().string(containsString("<p style=\"color:red\">에러 발생 페이지!!</p>")));
    }

    @Test
    @DisplayName("ControllerAdvice가 동작하기를 기대하였으나 RestControllerAdvice가 동작한다. 테스트에 실패한다. (2)")
    void view_api() throws Exception {
        mockMvc.perform(get("/view/api"))
                .andDo(print())
                .andExpect(status().is(444))
                .andExpect(content().string(containsString("<p style=\"color:red\">에러 발생 페이지!!</p>")));
    }
}
