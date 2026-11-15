package be.kdg.prog6.loading.adapter.out;

import be.kdg.prog6.loading.domain.WeightUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
class UldJpaEmbeddable {

    @Column(nullable = false)
    private String uldCode;

    @Column(nullable = false)
    private double weight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WeightUnit weightUnit;

    @Column(nullable = false)
    private String destination;

    private String position;

    protected UldJpaEmbeddable() {
    }

    UldJpaEmbeddable(String uldCode, double weight, WeightUnit weightUnit, String destination, String position) {
        this.uldCode = uldCode;
        this.weight = weight;
        this.weightUnit = weightUnit;
        this.destination = destination;
        this.position = position;
    }

    String getUldCode() {
        return uldCode;
    }

    double getWeight() {
        return weight;
    }

    WeightUnit getWeightUnit() {
        return weightUnit;
    }

    String getDestination() {
        return destination;
    }

    String getPosition() {
        return position;
    }
}
