package resource.resource.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;

public class TextTest {
    @Test
    @DisplayName("resources에 있는 텍스트 파일을 읽는다.")
    void read_text(){
        // given
        File file = new File(getClass().getClassLoader().getResource("files/text.txt").getFile());
        assert file.canRead();

        // when
        int idx = 0;
        String[] text = new String[2];
        try(BufferedReader br = new BufferedReader(new FileReader(file))){
            String str;
            while((str = br.readLine())!=null){
                text[idx++] = str;
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        // then
        assertThat(text[0]).isEqualTo("hello,");
        assertThat(text[1]).isEqualTo("text!");
    }
}
