package be.kdg.prog6.flights.adapter.out;

import be.kdg.prog6.TestcontainersConfiguration;
import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.flights.domain.AircraftRegistration;
import be.kdg.prog6.flights.domain.DelaySide;
import be.kdg.prog6.flights.domain.Flight;
import be.kdg.prog6.flights.domain.FlightStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class FlightPersistenceAdapterTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 3, 0, 0);
    private static final Duration TOLERANCE = Duration.ofMinutes(5);

    private static final LocalDateTime STD = LocalDateTime.of(2026, 11, 2, 6, 30);
    private static final LocalDateTime STA = LocalDateTime.of(2026, 11, 2, 10, 0);

    @Autowired
    private FlightPersistenceAdapter adapter;

    private static Flight scheduleFlight(String flightNumber) {
        return Flight.schedule(new FlightNumber(flightNumber), new AirportCode("BRU"), new AirportCode("ATH"),
                new AircraftRegistration("SXBAA"), STD, STA);
    }

    @Test
    void storedFlightIsLoadedWithItsDelaysAndTimes() {
        Flight flight = scheduleFlight("HJ901");
        flight.registerDelay(DelaySide.DEPARTURE, 30, "93", "Late inbound aircraft");
        flight.registerDelay(DelaySide.ARRIVAL, 15, "87", "Airport congestion");
        flight.receiveFinalLoad();
        flight.recordDeparture(STD.plusMinutes(32), NOW, TOLERANCE);
        adapter.updateFlight(flight);

        Flight loaded = adapter.loadFlight(flight.getId()).orElseThrow();

        assertEquals(flight.getId(), loaded.getId());
        assertEquals(FlightStatus.DEPARTED, loaded.getStatus());
        assertEquals(2, loaded.getDelays().size());
        assertEquals(STD.plusMinutes(30), loaded.getEstimatedDeparture());
        assertEquals(STA.plusMinutes(45), loaded.getEstimatedArrival());
        assertEquals(STD.plusMinutes(32), loaded.getActualDeparture());
        assertTrue(loaded.isFinalLoadReceived());
    }

    @Test
    void loadedFlightRecordsNoNewEvents() {
        Flight flight = scheduleFlight("HJ902");
        adapter.updateFlight(flight);

        Flight loaded = adapter.loadFlight(flight.getId()).orElseThrow();

        assertTrue(loaded.getDomainEvents().isEmpty());
    }

    @Test
    void updatingAStoredFlightKeepsEarlierDelays() {
        Flight flight = scheduleFlight("HJ903");
        flight.registerDelay(DelaySide.DEPARTURE, 10, "93", "Late inbound aircraft");
        adapter.updateFlight(flight);

        Flight loaded = adapter.loadFlight(flight.getId()).orElseThrow();
        loaded.registerDelay(DelaySide.DEPARTURE, 20, "64", "Crew late");
        adapter.updateFlight(loaded);

        Flight reloaded = adapter.loadFlight(flight.getId()).orElseThrow();
        assertEquals(2, reloaded.getDelays().size());
        assertEquals(STD.plusMinutes(30), reloaded.getEstimatedDeparture());
    }

    @Test
    void unknownFlightIsNotFound() {
        FlightId unknown = new FlightId(new FlightNumber("HJ999"), LocalDate.of(2026, 11, 2), new AirportCode("BRU"));

        assertTrue(adapter.loadFlight(unknown).isEmpty());
    }
}
