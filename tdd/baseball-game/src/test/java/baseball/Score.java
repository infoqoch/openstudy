package baseball;

import java.util.ArrayList;
import java.util.List;

public class Score {
    private final List<Integer> query;
    private final int strike;
    private final int ball;

    private Score(List<Integer> answer, List<Integer> query) {
        this.query = new ArrayList<>(query);

        int strike = countStrike(answer, this.query);
        int ball = countBall(answer, this.query, strike);

        this.strike = strike;
        this.ball = ball;
    }

    private int countBall(List<Integer> answer, List<Integer> query, int strike) {
        return (int) (answer.stream().filter(i -> query.contains(i)).count() - strike);
    }

    private int countStrike(List<Integer> answer, List<Integer> query) {
        int strike = 0;
        for(int i=0; i<3; i++){
            if(answer.get(i)== query.get(i)){
                strike ++;
            }
        }
        return strike;
    }

    public static Score calculate(List<Integer> answer, List<Integer> query) {
        return new Score(answer, query);
    }

    public boolean isOut() {
        return ball == 0 && strike == 0;
    }

    public boolean isGameSet(){
        return strike==3;
    }

    public int strike() {
        return strike;
    }

    public int ball() {
        return ball;
    }
}