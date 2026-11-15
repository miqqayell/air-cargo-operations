package be.kdg.prog6.loading.domain;

import java.util.List;

public interface FloorPlan {

    String aircraftType();

    List<DeckPosition> positions();

    Position door();

    List<List<Position>> routesToDoor(Position from);

    default boolean contains(Position position) {
        return positions().stream().anyMatch(deckPosition -> deckPosition.position().equals(position));
    }

    default DeckSide sideOf(Position position) {
        return positions().stream()
                .filter(deckPosition -> deckPosition.position().equals(position))
                .map(DeckPosition::side)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Position " + position.code() + " does not exist on a " + aircraftType()));
    }
}
