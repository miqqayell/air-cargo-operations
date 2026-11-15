package be.kdg.prog6;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.flights.domain.AircraftRegistration;
import be.kdg.prog6.flights.domain.DelaySide;
import be.kdg.prog6.flights.domain.FlightSchedule;
import be.kdg.prog6.flights.domain.FlightStatus;
import be.kdg.prog6.flights.port.in.RecordActualTimeCommand;
import be.kdg.prog6.flights.port.in.RecordDepartureUseCase;
import be.kdg.prog6.flights.port.in.RegisterDelayCommand;
import be.kdg.prog6.flights.port.in.RegisterDelayUseCase;
import be.kdg.prog6.flights.port.in.ScheduleFlightsCommand;
import be.kdg.prog6.flights.port.in.ScheduleFlightsUseCase;
import be.kdg.prog6.flights.port.out.FlightLoadPort;
import be.kdg.prog6.loading.domain.LoadStatus;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class FlightAndLoadPersistenceFlowTest {

    private static final LocalDateTime STD = LocalDateTime.now(ZoneOffset.UTC).plusHours(1).truncatedTo(ChronoUnit.MINUTES);
    private static final AirportCode BRU = new AirportCode("BRU");
    private static final AirportCode ATH = new AirportCode("ATH");
    private static final FlightId FLIGHT = new FlightId(new FlightNumber("3V11"), STD.toLocalDate(), BRU);

    @Autowired
    private ScheduleFlightsUseCase scheduleFlightsUseCase;
    @Autowired
    private BookUldUseCase bookUldUseCase;
    @Autowired
    private RegisterDelayUseCase registerDelayUseCase;
    @Autowired
    private DeclareLoadFinalUseCase declareLoadFinalUseCase;
    @Autowired
    private RecordDepartureUseCase recordDepartureUseCase;
    @Autowired
    private FlightLoadPort flightLoadPort;
    @Autowired
    private LoadLoadPort loadLoadPort;

    @Test
    void flightAndLoadAreStoredAndFollowEachOther() {
        scheduleFlightsUseCase.scheduleFlights(new ScheduleFlightsCommand(new FlightSchedule(
                new FlightNumber("3V11"), BRU, ATH, new AircraftRegistration("SXBAA"),
                STD.toLocalDate(), STD.toLocalDate(), Set.of(STD.getDayOfWeek()),
                STD.toLocalTime(), STD.plusHours(3).toLocalTime())));

        assertTrue(flightLoadPort.loadFlight(FLIGHT).isPresent());
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertEquals(LoadStatus.OPEN, loadLoadPort.loadLoad(FLIGHT).orElseThrow().getStatus()));

        registerDelayUseCase.registerDelay(new RegisterDelayCommand(FLIGHT, DelaySide.DEPARTURE, 15, "93",
                "Late inbound aircraft"));
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertEquals(STD.plusMinutes(15),
                loadLoadPort.loadLoad(FLIGHT).orElseThrow().getEstimatedDeparture()));

        bookUldUseCase.bookUld(new BookUldCommand(FLIGHT, new UldCode("AKE16689R7"),
                new Weight(1000, WeightUnit.KG), ATH));
        declareLoadFinalUseCase.declareFinal(new DeclareLoadFinalCommand(FLIGHT, true));

        assertEquals(1, loadLoadPort.loadLoad(FLIGHT).orElseThrow().getUlds().size());
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertTrue(flightLoadPort.loadFlight(FLIGHT).orElseThrow().isFinalLoadReceived()));

        recordDepartureUseCase.recordDeparture(
                new RecordActualTimeCommand(FLIGHT, LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES)));

        assertEquals(FlightStatus.DEPARTED, flightLoadPort.loadFlight(FLIGHT).orElseThrow().getStatus());
    }
}
