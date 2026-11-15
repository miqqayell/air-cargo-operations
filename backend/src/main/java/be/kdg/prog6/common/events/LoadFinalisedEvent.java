package be.kdg.prog6.common.events;

import be.kdg.prog6.common.domain.FlightId;

import java.util.List;
import java.util.UUID;

public record LoadFinalisedEvent(UUID eventId, FlightId flightId, List<PlacedUld> placedUlds) implements DomainEvent {

    public record PlacedUld(String uldCode, String position, double weight, String weightUnit, String destination) {
    }
}
