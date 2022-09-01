package resource.resource.image;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ImageTest {

    @Test
    @DisplayName("resources에 있는 이미지 파일을 읽는다.")
    void read_image() throws IOException {
        // given
        File file = new File(getClass().getClassLoader().getResource("files/android.png").getFile());
        assert file.canRead();

        // when
        final BufferedImage bi = ImageIO.read(file);

        // then
        assertThat(file.length()).isEqualTo(90902L);
        assertThat(bi.getWidth()).isEqualTo(1745);
        assertThat(bi.getHeight()).isEqualTo(2048);
    }
}
