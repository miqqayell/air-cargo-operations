package be.kdg.prog6.loading.adapter.in;

import java.time.Instant;
import java.util.UUID;

public record UldMessage(UUID messageId, Instant producedAt, int formatVersion,
                         String flightNumber, int departureDay, String departureAirport,
                         String uldCode, double weight, String weightUnit, String destination) {
}
