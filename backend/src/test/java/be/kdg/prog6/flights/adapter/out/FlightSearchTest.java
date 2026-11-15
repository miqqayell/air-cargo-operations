package be.kdg.prog6.flights.adapter.out;

import be.kdg.prog6.TestcontainersConfiguration;
import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.flights.domain.AircraftRegistration;
import be.kdg.prog6.flights.domain.Flight;
import be.kdg.prog6.flights.domain.FlightSearchCriteria;
import be.kdg.prog6.flights.domain.FlightStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class FlightSearchTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2027, 2, 4, 0, 0);
    private static final Duration TOLERANCE = Duration.ofMinutes(5);

    private static final LocalDate DAY = LocalDate.of(2027, 2, 3);
    private static final AirportCode BRU = new AirportCode("BRU");
    private static final AirportCode ATH = new AirportCode("ATH");
    private static final AirportCode LGG = new AirportCode("LGG");

    @Autowired
    private FlightPersistenceAdapter adapter;

    @BeforeEach
    void storeFlights() {
        store("SR101", BRU, ATH, LocalTime.of(14, 0), false);
        store("SR102", BRU, ATH, LocalTime.of(6, 0), true);
        store("SR103", LGG, ATH, LocalTime.of(9, 0), false);
    }

    private void store(String flightNumber, AirportCode from, AirportCode to, LocalTime departure, boolean departed) {
        Flight flight = Flight.schedule(new FlightNumber(flightNumber), from, to, new AircraftRegistration("SXBAA"),
                DAY.atTime(departure), DAY.atTime(departure).plusHours(3));
        if (departed) {
            flight.receiveFinalLoad();
            flight.recordDeparture(DAY.atTime(departure), NOW, TOLERANCE);
        }
        adapter.updateFlight(flight);
    }

    private List<String> flightNumbers(FlightSearchCriteria criteria) {
        return adapter.searchFlights(criteria).stream().map(flight -> flight.getId().flightNumber().value()).toList();
    }

    @Test
    void flightsOfADayAreSortedByScheduledDeparture() {
        assertEquals(List.of("SR102", "SR103", "SR101"), flightNumbers(FlightSearchCriteria.onDate(DAY)));
    }

    @Test
    void criteriaCanBeCombined() {
        assertEquals(List.of("SR101"),
                flightNumbers(new FlightSearchCriteria(null, DAY, BRU, ATH, FlightStatus.SCHEDULED)));
    }

    @Test
    void departedFlightsAreFoundByStatus() {
        assertEquals(List.of("SR102"), flightNumbers(new FlightSearchCriteria(null, DAY, null, null, FlightStatus.DEPARTED)));
    }

    @Test
    void flightNumberAloneFindsThatFlight() {
        assertEquals(List.of("SR103"),
                flightNumbers(new FlightSearchCriteria(new FlightNumber("SR103"), null, null, null, null)));
    }
}
