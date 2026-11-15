package be.kdg.prog6.flights.adapter.out;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.flights.domain.AircraftRegistration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

record MvtTelegram(MvtType type, FlightId flightId, AircraftRegistration aircraftRegistration, AirportCode airport,
                   LocalDateTime time) {

    private static final DateTimeFormatter HOURS_AND_MINUTES = DateTimeFormatter.ofPattern("HHmm");

    String toText() {
        return String.join("\n",
                "MVT",
                "%s/%02d.%s.%s".formatted(flightId.flightNumber().value(),
                        flightId.scheduledDepartureDate().getDayOfMonth(),
                        aircraftRegistration.value(), airport.code()),
                type.name() + time.format(HOURS_AND_MINUTES));
    }
}
