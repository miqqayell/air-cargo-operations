package be.kdg.prog6.common.events;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;

import java.time.LocalDateTime;
import java.util.UUID;

public record FlightScheduledEvent(UUID eventId, FlightId flightId, AirportCode arrivalAirport,
                                   LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival)
        implements DomainEvent {
}
