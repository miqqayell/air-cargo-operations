package be.kdg.prog6.loading.adapter.out;

import be.kdg.prog6.loading.domain.LoadStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(schema = "kdg_loading", name = "load")
class LoadJpaEntity {

    @EmbeddedId
    private LoadJpaId id;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private LocalDateTime scheduledDeparture;

    @Column(nullable = false)
    private LocalDateTime estimatedDeparture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoadStatus status;

    @Column(nullable = false)
    private boolean departed;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(schema = "kdg_loading", name = "uld", joinColumns = {
            @JoinColumn(name = "flight_number", referencedColumnName = "flight_number"),
            @JoinColumn(name = "scheduled_departure_date", referencedColumnName = "scheduled_departure_date"),
            @JoinColumn(name = "departure_airport", referencedColumnName = "departure_airport")})
    private List<UldJpaEmbeddable> ulds = new ArrayList<>();

    protected LoadJpaEntity() {
    }

    LoadJpaEntity(LoadJpaId id, String destination, LocalDateTime scheduledDeparture,
                  LocalDateTime estimatedDeparture, LoadStatus status, boolean departed,
                  List<UldJpaEmbeddable> ulds) {
        this.id = id;
        this.destination = destination;
        this.scheduledDeparture = scheduledDeparture;
        this.estimatedDeparture = estimatedDeparture;
        this.status = status;
        this.departed = departed;
        this.ulds = new ArrayList<>(ulds);
    }

    LoadJpaId getId() {
        return id;
    }

    String getDestination() {
        return destination;
    }

    LocalDateTime getScheduledDeparture() {
        return scheduledDeparture;
    }

    LocalDateTime getEstimatedDeparture() {
        return estimatedDeparture;
    }

    LoadStatus getStatus() {
        return status;
    }

    boolean isDeparted() {
        return departed;
    }

    List<UldJpaEmbeddable> getUlds() {
        return ulds;
    }
}
