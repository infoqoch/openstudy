package qoch.springexceptionhandlerscopetest.t2;

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
@ActiveProfiles("test2")
public class WithoutRestAdviceTest {

    @BeforeEach
    void init(WebApplicationContext ctx){
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    private MockMvc mockMvc;

    @Test
    void rest_api() throws Exception {
        mockMvc.perform(get("/rest/api"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("{\"message\":\"api error 발생!\"}"));
    }

    @Test
    @DisplayName("RestController가 아닌 경우 ViewExceptionHandler 어드바이스가 정상 동작한다. 성공한다. (1)")
    void view_page() throws Exception {
        mockMvc.perform(get("/view/page"))
                .andDo(print())
                .andExpect(status().is(444)) // 그냥 임의의 status를 넣어봤음!
                .andExpect(content().string(containsString("<p style=\"color:red\">에러 발생 페이지!!</p>")));
    }

    @Test
    @DisplayName("RestController가 아닌 경우 ViewExceptionHandler 어드바이스가 정상 동작한다. 성공한다. (2)")
    void view_api() throws Exception {
        mockMvc.perform(get("/view/api"))
                .andDo(print())
                .andExpect(status().is(444)) // 그냥 임의의 status를 넣어봤음!
                .andExpect(content().string(containsString("<p style=\"color:red\">에러 발생 페이지!!</p>")));
    }

}
