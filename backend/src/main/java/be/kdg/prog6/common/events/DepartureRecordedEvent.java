package be.kdg.prog6.common.events;

import be.kdg.prog6.common.domain.FlightId;

import java.time.LocalDateTime;
import java.util.UUID;

public record DepartureRecordedEvent(UUID eventId, FlightId flightId, String aircraftRegistration,
                                     LocalDateTime actualDeparture) implements DomainEvent {
}
