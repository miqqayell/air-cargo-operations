package be.kdg.prog6.flights.core;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.common.events.FlightRemovedEvent;
import be.kdg.prog6.flights.domain.AircraftRegistration;
import be.kdg.prog6.flights.domain.Flight;
import be.kdg.prog6.flights.domain.FlightPolicy;
import be.kdg.prog6.flights.port.out.FlightLoadPort;
import be.kdg.prog6.flights.port.out.FlightPolicyPort;
import be.kdg.prog6.flights.port.out.FlightRemovePort;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultRemoveExpiredFlightsUseCaseTest {

    private static final Clock OCTOBER_30 = Clock.fixed(Instant.parse("2026-10-30T03:00:00Z"), ZoneOffset.UTC);

    private final FlightPolicyPort flightPolicyPort = mock(FlightPolicyPort.class);
    private final FlightLoadPort flightLoadPort = mock(FlightLoadPort.class);
    private final FlightRemovePort flightRemovePort = mock(FlightRemovePort.class);
    private final DefaultRemoveExpiredFlightsUseCase useCase = new DefaultRemoveExpiredFlightsUseCase(
            flightPolicyPort, flightLoadPort, List.of(flightRemovePort), OCTOBER_30);

    @Test
    void flightsScheduledBeforeTheRetentionPeriodAreRemovedAndAnnounced() {
        when(flightPolicyPort.flightPolicy()).thenReturn(new FlightPolicy(Duration.ofMinutes(5), Duration.ofDays(7)));
        Flight expired = flightOn(LocalDate.of(2026, 10, 22));
        when(flightLoadPort.loadFlightsScheduledBefore(LocalDate.of(2026, 10, 23))).thenReturn(List.of(expired));

        assertEquals(1, useCase.removeExpiredFlights());

        verify(flightRemovePort).removeFlight(expired);
        assertInstanceOf(FlightRemovedEvent.class, expired.getDomainEvents().getLast());
        assertEquals(expired.getId(), ((FlightRemovedEvent) expired.getDomainEvents().getLast()).flightId());
    }

    @Test
    void aShorterRetentionRemovesMore() {
        when(flightPolicyPort.flightPolicy()).thenReturn(new FlightPolicy(Duration.ofMinutes(5), Duration.ofDays(1)));

        assertEquals(0, useCase.removeExpiredFlights());

        verify(flightLoadPort).loadFlightsScheduledBefore(LocalDate.of(2026, 10, 29));
        verify(flightRemovePort, never()).removeFlight(any());
    }

    private static Flight flightOn(LocalDate date) {
        LocalDateTime std = date.atTime(6, 30);
        return Flight.schedule(new FlightNumber("HJ221"), new AirportCode("BRU"), new AirportCode("ATH"),
                new AircraftRegistration("SXBAA"), std, std.plusHours(3));
    }
}
