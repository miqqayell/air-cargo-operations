package be.kdg.prog6;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

    @Test
    void modulesRespectTheirBoundaries() {
        ApplicationModules.of(AirCargoApplication.class).verify();
    }
}
