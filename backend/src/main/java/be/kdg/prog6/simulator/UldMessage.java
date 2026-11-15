package be.kdg.prog6.simulator;

import java.time.Instant;
import java.util.UUID;

record UldMessage(UUID messageId, Instant producedAt, int formatVersion,
                  String flightNumber, int departureDay, String departureAirport,
                  String uldCode, double weight, String weightUnit, String destination) {
}
