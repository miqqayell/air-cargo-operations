package be.kdg.prog6.flights.adapter.out;

import be.kdg.prog6.flights.domain.DelaySide;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.UUID;

@Embeddable
class DelayJpaEmbeddable {

    @Column(nullable = false)
    private UUID delayId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DelaySide side;

    @Column(nullable = false)
    private int minutes;

    @Column(nullable = false)
    private String delayCode;

    @Column(nullable = false)
    private String description;

    protected DelayJpaEmbeddable() {
    }

    DelayJpaEmbeddable(UUID delayId, DelaySide side, int minutes, String delayCode, String description) {
        this.delayId = delayId;
        this.side = side;
        this.minutes = minutes;
        this.delayCode = delayCode;
        this.description = description;
    }

    UUID getDelayId() {
        return delayId;
    }

    DelaySide getSide() {
        return side;
    }

    int getMinutes() {
        return minutes;
    }

    String getDelayCode() {
        return delayCode;
    }

    String getDescription() {
        return description;
    }
}
