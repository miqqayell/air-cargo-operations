package be.kdg.prog6.simulator;

import java.time.LocalDate;

public record UldBookingRequest(String flightNumber, LocalDate scheduledDepartureDate, String departureAirport,
                                String uldCode, double weight, String weightUnit, String destination) {
}
