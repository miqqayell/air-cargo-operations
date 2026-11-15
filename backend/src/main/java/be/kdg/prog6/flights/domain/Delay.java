package be.kdg.prog6.flights.domain;

import java.util.UUID;

public class Delay {

    private final DelayId id;
    private final DelaySide side;
    private final int minutes;
    private final String delayCode;
    private final String description;

    Delay(DelaySide side, int minutes, String delayCode, String description) {
        this(new DelayId(UUID.randomUUID()), side, minutes, delayCode, description);
    }

    private Delay(DelayId id, DelaySide side, int minutes, String delayCode, String description) {
        if (minutes < 1 || minutes > 12 * 60) {
            throw new IllegalArgumentException("A delay must be between 1 minute and 12 hours");
        }
        if (!IataDelayCodes.isKnown(delayCode)) {
            throw new IllegalArgumentException("Unknown IATA delay code: " + delayCode);
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("A delay needs a description");
        }
        this.id = id;
        this.side = side;
        this.minutes = minutes;
        this.delayCode = delayCode;
        this.description = description;
    }

    public static Delay restore(DelayId id, DelaySide side, int minutes, String delayCode, String description) {
        return new Delay(id, side, minutes, delayCode, description);
    }

    Delay correctedTo(int minutes, String description) {
        return new Delay(id, side, minutes, delayCode, description);
    }

    public DelayId getId() {
        return id;
    }

    public DelaySide getSide() {
        return side;
    }

    public int getMinutes() {
        return minutes;
    }

    public String getDelayCode() {
        return delayCode;
    }

    public String getDescription() {
        return description;
    }
}
