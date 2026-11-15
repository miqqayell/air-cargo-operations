package be.kdg.prog6.loading.adapter.out;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

record CpmMessage(UUID messageId, Instant producedAt, int formatVersion,
                  String flightNumber, LocalDate scheduledDepartureDate, String departureAirport,
                  List<CpmUld> ulds) {

    record CpmUld(String uldCode, String position, double weight, String weightUnit, String destination) {
    }
}
