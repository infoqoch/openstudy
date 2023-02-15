package baseball;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class FakeInputCommandTest {
    @Test
    void test(){
        FakeInputCommand input = new FakeInputCommand();
        input.add(asList(1,2,3));
        input.add(asList(3,4,5));
        input.add(asList(6,7,9));

        assertThat(input.next()).isEqualTo(asList(1,2,3));
        assertThat(input.next()).isEqualTo(asList(3,4,5));
        assertThat(input.next()).isEqualTo(asList(6,7,9));
    }
}