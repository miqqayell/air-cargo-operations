package be.kdg.prog6.loading.domain;

public class PositionOccupiedException extends IllegalStateException {

    public PositionOccupiedException(Position position) {
        super("Position " + position.code() + " already holds a ULD");
    }
}
