package be.kdg.prog6.flights.adapter.out;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

record FlmMessage(UUID messageId, Instant producedAt, int formatVersion,
                  String flightNumber, LocalDate scheduledDepartureDate,
                  LocalDateTime std, LocalDateTime sta, String departureAirport, String arrivalAirport) {
}
