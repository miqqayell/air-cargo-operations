package be.kdg.prog6;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.common.events.FlightScheduledEvent;
import be.kdg.prog6.common.events.LoadFinalisedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class EventPublicationRegistryTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static FlightId flightId(String flightNumber) {
        return new FlightId(new FlightNumber(flightNumber), LocalDate.of(2026, 11, 16), new AirportCode("BRU"));
    }

    @Test
    void handledEventIsMarkedCompletedForEveryListener() {
        FlightId flightId = flightId("HJ701");
        transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(new FlightScheduledEvent(
                UUID.randomUUID(), flightId, new AirportCode("ATH"), LocalDate.of(2026, 11, 16).atTime(6, 30),
                LocalDate.of(2026, 11, 16).atTime(10, 0))));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertEquals(List.of("COMPLETED"), statusesOfEventsAbout("HJ701", "FlightScheduledEventListener"));
            assertEquals(List.of("COMPLETED"), statusesOfEventsAbout("HJ701", "FlightPartnerMessagePublisher"));
        });
    }

    @Test
    void eventWhoseListenerFailsIsKeptAsFailed() {
        FlightId unknownFlight = flightId("HJ702");
        transactionTemplate.executeWithoutResult(status ->
                eventPublisher.publishEvent(new LoadFinalisedEvent(UUID.randomUUID(), unknownFlight, List.of())));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertEquals(List.of("FAILED"), statusesOfEventsAbout("HJ702", "LoadFinalisedEventListener")));
    }

    @Test
    void eventPublishedInARolledBackTransactionIsNeverDelivered() {
        FlightId flightId = flightId("HJ703");
        transactionTemplate.executeWithoutResult(status -> {
            eventPublisher.publishEvent(new LoadFinalisedEvent(UUID.randomUUID(), flightId, List.of()));
            status.setRollbackOnly();
        });

        assertEquals(List.of(), statusesOfEventsAbout("HJ703", "Listener"));
        assertEquals(List.of(), statusesOfEventsAbout("HJ703", "Publisher"));
    }

    private List<String> statusesOfEventsAbout(String flightNumber, String listener) {
        return jdbcTemplate.queryForList(
                "select status from kdg_events.event_publication where serialized_event like ? and listener_id like ?",
                String.class, "%" + flightNumber + "%", "%" + listener + "%");
    }
}
