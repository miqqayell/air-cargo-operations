package be.kdg.prog6.common.events;

import be.kdg.prog6.common.domain.FlightId;

import java.time.LocalDateTime;
import java.util.UUID;

public record EstimatedDepartureChangedEvent(UUID eventId, FlightId flightId, String aircraftRegistration,
                                             LocalDateTime estimatedDeparture) implements DomainEvent {
}
