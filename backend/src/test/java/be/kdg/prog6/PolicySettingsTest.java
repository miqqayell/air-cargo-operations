package be.kdg.prog6;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.flights.domain.AircraftRegistration;
import be.kdg.prog6.flights.domain.FlightSchedule;
import be.kdg.prog6.flights.port.in.ScheduleFlightsCommand;
import be.kdg.prog6.flights.port.in.ScheduleFlightsUseCase;
import be.kdg.prog6.loading.domain.CheckKind;
import be.kdg.prog6.loading.domain.LoadingPolicy;
import be.kdg.prog6.loading.domain.Position;
import be.kdg.prog6.loading.domain.UldCode;
import be.kdg.prog6.loading.domain.Weight;
import be.kdg.prog6.loading.domain.WeightUnit;
import be.kdg.prog6.loading.port.in.AssignPositionCommand;
import be.kdg.prog6.loading.port.in.AssignPositionUseCase;
import be.kdg.prog6.loading.port.in.BookUldCommand;
import be.kdg.prog6.loading.port.in.BookUldUseCase;
import be.kdg.prog6.loading.port.in.CheckLoadQuery;
import be.kdg.prog6.loading.port.in.UpdateLoadingPolicyCommand;
import be.kdg.prog6.loading.port.in.UpdateLoadingPolicyUseCase;
import be.kdg.prog6.loading.port.out.LoadLoadPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
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
class PolicySettingsTest {

    private static final LocalDateTime STD = LocalDateTime.now(ZoneOffset.UTC).plusHours(1).truncatedTo(ChronoUnit.MINUTES);
    private static final AirportCode BRU = new AirportCode("BRU");
    private static final AirportCode ATH = new AirportCode("ATH");
    private static final FlightId FLIGHT = new FlightId(new FlightNumber("HJ701"), STD.toLocalDate(), BRU);
    private static final LoadingPolicy DEFAULTS =
            new LoadingPolicy(Duration.ofHours(12), Duration.ofMinutes(20), BigDecimal.TEN, CheckKind.HARD);

    @Autowired
    private ScheduleFlightsUseCase scheduleFlightsUseCase;
    @Autowired
    private BookUldUseCase bookUldUseCase;
    @Autowired
    private AssignPositionUseCase assignPositionUseCase;
    @Autowired
    private CheckLoadQuery checkLoadQuery;
    @Autowired
    private UpdateLoadingPolicyUseCase updateLoadingPolicyUseCase;
    @Autowired
    private LoadLoadPort loadLoadPort;

    @AfterEach
    void backToTheDefaults() {
        updateLoadingPolicyUseCase.updateLoadingPolicy(new UpdateLoadingPolicyCommand(DEFAULTS));
    }

    @Test
    void aChangedLateralLimitAppliesToALoadThatAlreadyExists() {
        scheduleFlightsUseCase.scheduleFlights(new ScheduleFlightsCommand(new FlightSchedule(
                new FlightNumber("HJ701"), BRU, ATH, new AircraftRegistration("SXBAA"),
                STD.toLocalDate(), STD.toLocalDate(), Set.of(STD.getDayOfWeek()),
                STD.toLocalTime(), STD.plusHours(3).toLocalTime())));
        await().atMost(Duration.ofSeconds(10)).until(() -> loadLoadPort.loadLoad(FLIGHT).isPresent());
        place("AKE10001R7", 1000, "CL");
        place("AKE10002R7", 950, "CR");

        assertTrue(checkLoadQuery.checkLoad(FLIGHT).hardChecksPass());

        updateLoadingPolicyUseCase.updateLoadingPolicy(new UpdateLoadingPolicyCommand(
                new LoadingPolicy(Duration.ofHours(12), Duration.ofMinutes(20), new BigDecimal("5"), CheckKind.HARD)));

        var report = checkLoadQuery.checkLoad(FLIGHT);
        assertEquals(1, report.failedHardChecks().size());
        assertTrue(report.failedHardChecks().getFirst().reason().contains("5"));
    }

    private void place(String uldCode, double kilograms, String position) {
        bookUldUseCase.bookUld(new BookUldCommand(FLIGHT, new UldCode(uldCode), new Weight(kilograms, WeightUnit.KG), ATH));
        assignPositionUseCase.assignPosition(new AssignPositionCommand(FLIGHT, new UldCode(uldCode), new Position(position)));
    }
}
