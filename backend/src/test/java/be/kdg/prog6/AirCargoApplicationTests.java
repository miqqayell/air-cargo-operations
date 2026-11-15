package be.kdg.prog6;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class AirCargoApplicationTests {

    @Test
    void contextLoads() {
    }
}
