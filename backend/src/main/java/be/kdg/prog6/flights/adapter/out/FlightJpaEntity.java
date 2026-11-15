package be.kdg.prog6.flights.adapter.out;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(schema = "kdg_flights", name = "flight")
class FlightJpaEntity {

    @EmbeddedId
    private FlightJpaId id;

    @Column(nullable = false)
    private String arrivalAirport;

    @Column(nullable = false)
    private String aircraftRegistration;

    @Column(nullable = false)
    private LocalDateTime scheduledDeparture;

    @Column(nullable = false)
    private LocalDateTime scheduledArrival;

    private LocalDateTime actualDeparture;

    private LocalDateTime actualArrival;

    @Column(nullable = false)
    private boolean finalLoadReceived;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(schema = "kdg_flights", name = "delay", joinColumns = {
            @JoinColumn(name = "flight_number", referencedColumnName = "flight_number"),
            @JoinColumn(name = "scheduled_departure_date", referencedColumnName = "scheduled_departure_date"),
            @JoinColumn(name = "departure_airport", referencedColumnName = "departure_airport")})
    private List<DelayJpaEmbeddable> delays = new ArrayList<>();

    protected FlightJpaEntity() {
    }

    FlightJpaEntity(FlightJpaId id, String arrivalAirport, String aircraftRegistration,
                    LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival,
                    LocalDateTime actualDeparture, LocalDateTime actualArrival, boolean finalLoadReceived,
                    List<DelayJpaEmbeddable> delays) {
        this.id = id;
        this.arrivalAirport = arrivalAirport;
        this.aircraftRegistration = aircraftRegistration;
        this.scheduledDeparture = scheduledDeparture;
        this.scheduledArrival = scheduledArrival;
        this.actualDeparture = actualDeparture;
        this.actualArrival = actualArrival;
        this.finalLoadReceived = finalLoadReceived;
        this.delays = new ArrayList<>(delays);
    }

    FlightJpaId getId() {
        return id;
    }

    String getArrivalAirport() {
        return arrivalAirport;
    }

    String getAircraftRegistration() {
        return aircraftRegistration;
    }

    LocalDateTime getScheduledDeparture() {
        return scheduledDeparture;
    }

    LocalDateTime getScheduledArrival() {
        return scheduledArrival;
    }

    LocalDateTime getActualDeparture() {
        return actualDeparture;
    }

    LocalDateTime getActualArrival() {
        return actualArrival;
    }

    boolean isFinalLoadReceived() {
        return finalLoadReceived;
    }

    List<DelayJpaEmbeddable> getDelays() {
        return delays;
    }
}
