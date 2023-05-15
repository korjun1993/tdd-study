import org.assertj.core.api.Assertions;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AssertionsTest {

    static class Tv {
        String name;
        String color;
        String maker;

        Tv(String name, String color, String maker) {
            this.name = name;
            this.color = color;
            this.maker = maker;
        }
    }

    static class Computer {
        String name;
        String color;
        String maker;

        Computer(String name, String color, String maker) {
            this.name = name;
            this.color = color;
            this.maker = maker;
        }
    }

    @Test
    @DisplayName("서로 다른 객체를 비교한다")
    void 서로다른객체비교() {
        // given
        Tv tv = new Tv("올레드 TV", "white", "samsung");
        Computer computer = new Computer("갤럭시북", "white", "samsung");

        // when
        RecursiveComparisonConfiguration configuration = RecursiveComparisonConfiguration.builder().withIgnoredFields("name").build();
        Assertions.assertThat(tv).usingRecursiveComparison(configuration).isEqualTo(computer);
    }
}
