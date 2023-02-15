package baseball;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FakeInputCommand implements InputCommand {
    private final Queue<List<Integer>> queue = new LinkedList<>();

    @Override
    public List<Integer> next() {
        return queue.poll();
    }

    public void add(List<Integer> command) {
        queue.add(command);

    }
}
