package be.kdg.prog6.flights.adapter.out;

import be.kdg.prog6.TestcontainersConfiguration;
import be.kdg.prog6.flights.domain.FlightPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class FlightPolicyPersistenceAdapterTest {

    @Autowired
    private FlightPolicyPersistenceAdapter adapter;
    @Autowired
    private FlightPolicyJpaRepository repository;

    @AfterEach
    void backToTheDefaults() {
        repository.deleteAll();
    }

    @Test
    void withoutSavedSettingsTheDefaultsOfTheConfigurationApply() {
        assertEquals(new FlightPolicy(Duration.ofMinutes(5), Duration.ofDays(7)), adapter.flightPolicy());
    }

    @Test
    void savedSettingsReplaceTheDefaults() {
        adapter.updateFlightPolicy(new FlightPolicy(Duration.ofMinutes(10), Duration.ofDays(30)));
        adapter.updateFlightPolicy(new FlightPolicy(Duration.ofMinutes(2), Duration.ofDays(14)));

        assertEquals(new FlightPolicy(Duration.ofMinutes(2), Duration.ofDays(14)), adapter.flightPolicy());
        assertEquals(1, repository.count());
    }
}
