package be.kdg.prog6.flights.adapter.out;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "kdg_flights", name = "flight_policy")
class FlightPolicyJpaEntity {

    static final int SINGLE_ROW = 1;

    @Id
    private Integer id;

    @Column(nullable = false)
    private int actualTimeToleranceMinutes;

    @Column(nullable = false)
    private int retentionDays;

    protected FlightPolicyJpaEntity() {
    }

    FlightPolicyJpaEntity(int actualTimeToleranceMinutes, int retentionDays) {
        this.id = SINGLE_ROW;
        this.actualTimeToleranceMinutes = actualTimeToleranceMinutes;
        this.retentionDays = retentionDays;
    }

    int getActualTimeToleranceMinutes() {
        return actualTimeToleranceMinutes;
    }

    int getRetentionDays() {
        return retentionDays;
    }
}
