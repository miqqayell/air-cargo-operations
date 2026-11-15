package be.kdg.prog6.loading.domain;

import be.kdg.prog6.common.domain.AirportCode;

public class Uld {

    private final UldCode code;
    private final Weight weight;
    private final AirportCode destination;
    private Position position;

    Uld(UldCode code, Weight weight, AirportCode destination) {
        this.code = code;
        this.weight = weight;
        this.destination = destination;
    }

    public static Uld restore(UldCode code, Weight weight, AirportCode destination, Position position) {
        Uld uld = new Uld(code, weight, destination);
        uld.position = position;
        return uld;
    }

    void placeAt(Position position) {
        this.position = position;
    }

    void takeOffDeck() {
        this.position = null;
    }

    public boolean isPlaced() {
        return position != null;
    }

    public UldCode getCode() {
        return code;
    }

    public Weight getWeight() {
        return weight;
    }

    public AirportCode getDestination() {
        return destination;
    }

    public Position getPosition() {
        return position;
    }
}
