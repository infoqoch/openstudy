package baseball;

import java.util.ArrayList;
import java.util.List;

public class GameRecord {
    private final List<Integer> answer;
    private final int lastInning;

    private GameRecord(int lastInning, List<Integer> answer) {
        this.answer = new ArrayList<>(answer);
        this.lastInning = lastInning;

    }

    public static GameRecord report(int lastInning, List<Integer> answer) {
        return new GameRecord(lastInning, answer);
    }

    public List<Integer> answer() {
        return new ArrayList<>(answer);
    }

    public int lastInning() {
        return lastInning;
    }
}
