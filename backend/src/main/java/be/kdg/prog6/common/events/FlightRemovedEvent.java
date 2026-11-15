package be.kdg.prog6.common.events;

import be.kdg.prog6.common.domain.FlightId;

import java.util.UUID;

public record FlightRemovedEvent(UUID eventId, FlightId flightId) implements DomainEvent {
}
