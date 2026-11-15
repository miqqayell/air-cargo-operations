package be.kdg.prog6.flights.domain;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.events.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record EstimatedArrivalChangedEvent(UUID eventId, FlightId flightId,
                                           AircraftRegistration aircraftRegistration, AirportCode arrivalAirport,
                                           LocalDateTime estimatedArrival) implements DomainEvent {
}
