package baseball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class BaseballGameTest {

    private FakeInputCommand input;

    @BeforeEach
    void setUp(){
        input = new FakeInputCommand();
    }

    @Test
    @DisplayName("초기화 때 answer를 삽입한다.")
    void set_answer_when_init(){
        // given
        input.add(asList(1,2,3)); // 첫 번째 이닝

        BaseballGame game = new BaseballGame(input, asList(1,2,3)); // 초기화 때 정답을 삽입한다.

        // when
        GameRecord gameRecord = game.play();

        // then
        assertThat(gameRecord.answer()).containsExactlyElementsOf(asList(1,2,3));
        assertThat(gameRecord.lastInning()).isEqualTo(1);
    }

    @Test
    @DisplayName("1이닝 게임 종료")
    void game_set_first_inning(){
        // given
        input.add(asList(1,2,3)); // answer
        input.add(asList(1,2,3)); // 첫 번째 이닝

        assertAnswerAndLastInning(asList(1, 2, 3), 1);
    }
    
    @Test
    @DisplayName("2이닝 게임 종료")
    void game_set_2_inning(){
        // given
        input.add(asList(5,7,3)); // answer
        input.add(asList(1,2,3)); // 첫 번째 이닝
        input.add(asList(5,7,3)); // 두 번째 이닝

        assertAnswerAndLastInning(asList(5,7,3), 2);
    }

    @Test
    @DisplayName("9이닝 게임 종료")
    void game_set_9_inning(){
        // given
        input.add(asList(5,7,3)); // answer
        input.add(asList(1,2,3)); // 1
        input.add(asList(1,2,3)); // 2
        input.add(asList(1,2,3)); // 3
        input.add(asList(1,2,3)); // 4
        input.add(asList(1,2,3)); // 5
        input.add(asList(1,2,3)); // 6
        input.add(asList(1,2,3)); // 7
        input.add(asList(1,2,3)); // 8
        input.add(asList(5,7,3)); // 마지막 이닝

        assertAnswerAndLastInning(asList(5,7,3), 9);
    }

    private void assertAnswerAndLastInning(List<Integer> answer, int lastInning) {
        BaseballGame game = new BaseballGame(input);
        GameRecord gameRecord = game.play();

        // then
        assertThat(gameRecord.answer()).containsExactlyElementsOf(answer);
        assertThat(gameRecord.lastInning()).isEqualTo(lastInning);
    }
}
