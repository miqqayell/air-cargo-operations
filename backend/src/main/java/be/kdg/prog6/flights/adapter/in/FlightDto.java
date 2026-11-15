package be.kdg.prog6.flights.adapter.in;

import be.kdg.prog6.flights.domain.Delay;
import be.kdg.prog6.flights.domain.Flight;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

public record FlightDto(String flightNumber, LocalDate scheduledDepartureDate, String departureAirport,
                        String arrivalAirport, String aircraftRegistration, String status,
                        Instant std, Instant sta, Instant etd, Instant eta, Instant atd, Instant ata,
                        boolean finalLoadReceived, List<DelayDto> delays) {

    public record DelayDto(UUID id, String side, int minutes, String delayCode, String description) {

        static DelayDto from(Delay delay) {
            return new DelayDto(delay.getId().id(), delay.getSide().name(), delay.getMinutes(),
                    delay.getDelayCode(), delay.getDescription());
        }
    }

    public static FlightDto from(Flight flight) {
        return new FlightDto(
                flight.getId().flightNumber().value(),
                flight.getId().scheduledDepartureDate(),
                flight.getId().departureAirport().code(),
                flight.getArrivalAirport().code(),
                flight.getAircraftRegistration().value(),
                flight.getStatus().name(),
                utc(flight.getScheduledDeparture()),
                utc(flight.getScheduledArrival()),
                utc(flight.getEstimatedDeparture()),
                utc(flight.getEstimatedArrival()),
                utc(flight.getActualDeparture()),
                utc(flight.getActualArrival()),
                flight.isFinalLoadReceived(),
                flight.getDelays().stream().map(DelayDto::from).toList());
    }

    private static Instant utc(LocalDateTime time) {
        return time == null ? null : time.toInstant(ZoneOffset.UTC);
    }
}
