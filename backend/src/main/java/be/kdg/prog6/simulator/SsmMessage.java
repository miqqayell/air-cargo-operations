package be.kdg.prog6.simulator;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

record SsmMessage(UUID messageId, Instant producedAt, int formatVersion,
                  String flightNumber, String departureAirport, String arrivalAirport,
                  String aircraftRegistration, LocalDate firstDate, LocalDate lastDate,
                  Set<DayOfWeek> weekdays, LocalTime departureTime, LocalTime arrivalTime) {
}
