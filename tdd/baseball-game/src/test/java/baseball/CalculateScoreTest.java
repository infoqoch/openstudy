package baseball;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class CalculateScoreTest {

    @Test
    @DisplayName("아웃")
    void out(){
        // given
        final List<Integer> query = Arrays.asList(5, 8, 9);

        // when
        final Score score = Score.calculate(Arrays.asList(1, 2, 3), query);

        // then
        assertThat(score.strike()).isEqualTo(0);
        assertThat(score.ball()).isEqualTo(0);
        assertThat(score.isOut()).isEqualTo(true);
    }

    @Test
    @DisplayName("스트라이크 1 볼 1")
    void valid_strike1_ball1(){
        // given
        final List<Integer> answer = Arrays.asList(1, 2, 3);
        final List<Integer> query = Arrays.asList(1, 3, 8);

        // when
        final Score score = Score.calculate(answer, query);

        // then
        assertThat(score.strike()).isEqualTo(1);
        assertThat(score.ball()).isEqualTo(1);
        assertThat(score.isOut()).isFalse();
    }


    @Test
    @DisplayName("볼 2")
    void valid_ball2(){
        // given
        final List<Integer> answer = Arrays.asList(1, 2, 3);
        final List<Integer> query = Arrays.asList(2, 1, 7);

        // when
        final Score score = Score.calculate(answer, query);

        // then
        assertThat(score.strike()).isEqualTo(0);
        assertThat(score.ball()).isEqualTo(2);
        assertThat(score.isOut()).isFalse();
    }


    @Test
    @DisplayName("스트라이크 1")
    void valid_strike1(){
        // given
        final List<Integer> answer = Arrays.asList(1, 2, 3);
        final List<Integer> query = Arrays.asList(6, 2, 8);

        // when
        final Score score = Score.calculate(answer, query);

        // then
        assertThat(score.strike()).isEqualTo(1);
        assertThat(score.ball()).isEqualTo(0);
        assertThat(score.isOut()).isFalse();
    }


    @Test
    @DisplayName("스트라이크 3, 게임끝")
    void valid_strike3(){
        // given
        final List<Integer> answer = Arrays.asList(1, 2, 3);

        // when
        final Score score = Score.calculate(answer, answer);

        // then
        assertThat(score.strike()).isEqualTo(3);
        assertThat(score.ball()).isEqualTo(0);
        assertThat(score.isOut()).isFalse();
    }


    @Test
    void refactoring_test_cases(){
        validCalculate(asList(1, 2, 3), asList(5, 8, 9), 0, 0, true);
    }

    private void validCalculate(List<Integer> answer, List<Integer> query, int strike, int ball, boolean isOut) {
        final Score score = Score.calculate(answer, query);

        assertThat(score.strike()).isEqualTo(strike);
        assertThat(score.ball()).isEqualTo(ball);
        assertThat(score.isOut()).isEqualTo(isOut);
    }
}
