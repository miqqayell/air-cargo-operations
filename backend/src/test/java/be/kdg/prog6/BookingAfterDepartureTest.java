package be.kdg.prog6;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.flights.domain.AircraftRegistration;
import be.kdg.prog6.flights.domain.FlightSchedule;
import be.kdg.prog6.flights.port.in.RecordActualTimeCommand;
import be.kdg.prog6.flights.port.in.RecordDepartureUseCase;
import be.kdg.prog6.flights.port.in.ScheduleFlightsCommand;
import be.kdg.prog6.flights.port.in.ScheduleFlightsUseCase;
import be.kdg.prog6.flights.port.out.FlightLoadPort;
import be.kdg.prog6.loading.domain.UldCode;
import be.kdg.prog6.loading.domain.Weight;
import be.kdg.prog6.loading.domain.WeightUnit;
import be.kdg.prog6.loading.port.in.BookUldCommand;
import be.kdg.prog6.loading.port.in.BookUldUseCase;
import be.kdg.prog6.loading.port.in.DeclareLoadFinalCommand;
import be.kdg.prog6.loading.port.in.DeclareLoadFinalUseCase;
import be.kdg.prog6.loading.port.out.LoadLoadPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class BookingAfterDepartureTest {

    private static final LocalDateTime NOW = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES);
    private static final LocalDateTime STD = NOW.plusHours(1);
    private static final AirportCode BRU = new AirportCode("BRU");
    private static final AirportCode ATH = new AirportCode("ATH");
    private static final FlightId FLIGHT = new FlightId(new FlightNumber("HJ901"), STD.toLocalDate(), BRU);

    @Autowired
    private ScheduleFlightsUseCase scheduleFlightsUseCase;
    @Autowired
    private DeclareLoadFinalUseCase declareLoadFinalUseCase;
    @Autowired
    private RecordDepartureUseCase recordDepartureUseCase;
    @Autowired
    private BookUldUseCase bookUldUseCase;
    @Autowired
    private FlightLoadPort flightLoadPort;
    @Autowired
    private LoadLoadPort loadLoadPort;

    @Test
    void cargoCannotBeBookedOnceTheFlightHasDeparted() {
        scheduleFlightsUseCase.scheduleFlights(new ScheduleFlightsCommand(new FlightSchedule(
                new FlightNumber("HJ901"), BRU, ATH, new AircraftRegistration("SXBAA"),
                STD.toLocalDate(), STD.toLocalDate(), Set.of(STD.getDayOfWeek()),
                STD.toLocalTime(), STD.plusHours(3).toLocalTime())));
        await().atMost(Duration.ofSeconds(10)).until(() -> loadLoadPort.loadLoad(FLIGHT).isPresent());

        declareLoadFinalUseCase.declareFinal(new DeclareLoadFinalCommand(FLIGHT, true));
        await().atMost(Duration.ofSeconds(10))
                .until(() -> flightLoadPort.loadFlight(FLIGHT).orElseThrow().isFinalLoadReceived());

        recordDepartureUseCase.recordDeparture(new RecordActualTimeCommand(FLIGHT, NOW));
        await().atMost(Duration.ofSeconds(10)).until(() -> loadLoadPort.loadLoad(FLIGHT).orElseThrow().isDeparted());

        assertThrows(IllegalStateException.class, () -> bookUldUseCase.bookUld(new BookUldCommand(FLIGHT,
                new UldCode("AKE19001R7"), new Weight(1000, WeightUnit.KG), ATH)));
    }
}
