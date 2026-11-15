package be.kdg.prog6.flights.domain;

public class InvalidFlightStateException extends IllegalStateException {

    public InvalidFlightStateException(String message) {
        super(message);
    }
}
