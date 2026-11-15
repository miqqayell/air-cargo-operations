package be.kdg.prog6;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.flights.domain.AircraftRegistration;
import be.kdg.prog6.flights.domain.FlightSchedule;
import be.kdg.prog6.flights.port.in.RemoveExpiredFlightsUseCase;
import be.kdg.prog6.flights.port.in.ScheduleFlightsCommand;
import be.kdg.prog6.flights.port.in.ScheduleFlightsUseCase;
import be.kdg.prog6.flights.port.out.FlightLoadPort;
import be.kdg.prog6.loading.domain.UldCode;
import be.kdg.prog6.loading.domain.Weight;
import be.kdg.prog6.loading.domain.WeightUnit;
import be.kdg.prog6.loading.port.in.BookUldCommand;
import be.kdg.prog6.loading.port.in.BookUldUseCase;
import be.kdg.prog6.loading.port.out.LoadLoadPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Set;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class RetentionTest {

    private static final LocalDate TODAY = LocalDate.now(ZoneOffset.UTC);
    private static final AirportCode BRU = new AirportCode("BRU");
    private static final AirportCode ATH = new AirportCode("ATH");

    @Autowired
    private ScheduleFlightsUseCase scheduleFlightsUseCase;
    @Autowired
    private BookUldUseCase bookUldUseCase;
    @Autowired
    private RemoveExpiredFlightsUseCase removeExpiredFlightsUseCase;
    @Autowired
    private FlightLoadPort flightLoadPort;
    @Autowired
    private LoadLoadPort loadLoadPort;

    @Test
    void aFlightOlderThanTheRetentionDisappearsWithItsLoad() {
        FlightId expired = schedule("HJ801", TODAY.minusDays(8));
        FlightId retained = schedule("HJ802", TODAY.minusDays(7));
        bookUldUseCase.bookUld(new BookUldCommand(expired, new UldCode("AKE18001R7"),
                new Weight(1000, WeightUnit.KG), ATH));

        removeExpiredFlightsUseCase.removeExpiredFlights();

        assertFalse(flightLoadPort.loadFlight(expired).isPresent());
        await().atMost(Duration.ofSeconds(10)).until(() -> loadLoadPort.loadLoad(expired).isEmpty());
        assertTrue(flightLoadPort.loadFlight(retained).isPresent());
        assertTrue(loadLoadPort.loadLoad(retained).isPresent());
    }

    private FlightId schedule(String flightNumber, LocalDate date) {
        scheduleFlightsUseCase.scheduleFlights(new ScheduleFlightsCommand(new FlightSchedule(
                new FlightNumber(flightNumber), BRU, ATH, new AircraftRegistration("SXBAA"),
                date, date, Set.of(date.getDayOfWeek()), LocalTime.of(6, 30), LocalTime.of(10, 0))));
        FlightId flightId = new FlightId(new FlightNumber(flightNumber), date, BRU);
        await().atMost(Duration.ofSeconds(10)).until(() -> loadLoadPort.loadLoad(flightId).isPresent());
        return flightId;
    }
}
