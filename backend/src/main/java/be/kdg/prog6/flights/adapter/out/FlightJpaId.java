package be.kdg.prog6.flights.adapter.out;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.domain.FlightNumber;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
class FlightJpaId implements Serializable {

    @Column(name = "flight_number")
    private String flightNumber;

    @Column(name = "scheduled_departure_date")
    private LocalDate scheduledDepartureDate;

    @Column(name = "departure_airport")
    private String departureAirport;

    protected FlightJpaId() {
    }

    private FlightJpaId(String flightNumber, LocalDate scheduledDepartureDate, String departureAirport) {
        this.flightNumber = flightNumber;
        this.scheduledDepartureDate = scheduledDepartureDate;
        this.departureAirport = departureAirport;
    }

    static FlightJpaId from(FlightId flightId) {
        return new FlightJpaId(flightId.flightNumber().value(), flightId.scheduledDepartureDate(),
                flightId.departureAirport().code());
    }

    FlightId toDomain() {
        return new FlightId(new FlightNumber(flightNumber), scheduledDepartureDate, new AirportCode(departureAirport));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FlightJpaId other
                && flightNumber.equals(other.flightNumber)
                && scheduledDepartureDate.equals(other.scheduledDepartureDate)
                && departureAirport.equals(other.departureAirport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightNumber, scheduledDepartureDate, departureAirport);
    }
}
