package be.kdg.prog6.loading.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class Boeing747Freighter implements FloorPlan {

    private static final List<String> NOSE = List.of("A1", "A2", "B1");
    private static final List<String> ROWS = List.of("C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "P", "R", "S", "T", "U");
    private static final String TAIL = "X";
    private static final String DOOR_ROW = "R";

    private static final List<DeckPosition> POSITIONS = buildPositions();

    private static List<DeckPosition> buildPositions() {
        List<DeckPosition> positions = new ArrayList<>();
        int column = 0;
        for (String nose : NOSE) {
            positions.add(new DeckPosition(new Position(nose), DeckSide.CENTRE, column++));
        }
        for (String row : ROWS) {
            positions.add(new DeckPosition(left(row), DeckSide.LEFT, column));
            positions.add(new DeckPosition(right(row), DeckSide.RIGHT, column));
            column++;
        }
        positions.add(new DeckPosition(new Position(TAIL), DeckSide.CENTRE, column));
        return List.copyOf(positions);
    }

    @Override
    public String aircraftType() {
        return "Boeing 747-400 freighter";
    }

    @Override
    public List<DeckPosition> positions() {
        return POSITIONS;
    }

    @Override
    public Position door() {
        return left(DOOR_ROW);
    }

    @Override
    public List<List<Position>> routesToDoor(Position from) {
        DeckSide side = sideOf(from);
        if (NOSE.contains(from.code())) {
            List<Position> manoeuvring = List.of(left(ROWS.getFirst()), right(ROWS.getFirst()));
            return List.of(
                    concat(manoeuvring, alongSide(DeckSide.LEFT, ROWS.getFirst())),
                    concat(manoeuvring, alongSide(DeckSide.RIGHT, ROWS.getFirst())));
        }
        if (from.code().equals(TAIL)) {
            List<Position> manoeuvring = List.of(left(ROWS.getLast()), right(ROWS.getLast()));
            return List.of(
                    concat(manoeuvring, alongSide(DeckSide.LEFT, ROWS.getLast())),
                    concat(manoeuvring, alongSide(DeckSide.RIGHT, ROWS.getLast())));
        }
        String row = from.code().substring(0, 1);
        List<Position> route = new ArrayList<>(alongSide(side, row));
        route.remove(from);
        return List.of(List.copyOf(route));
    }

    private static List<Position> alongSide(DeckSide side, String fromRow) {
        int from = ROWS.indexOf(fromRow);
        int door = ROWS.indexOf(DOOR_ROW);
        int step = from <= door ? 1 : -1;
        List<Position> route = new ArrayList<>();
        for (int index = from; index != door + step; index += step) {
            route.add(side == DeckSide.LEFT ? left(ROWS.get(index)) : right(ROWS.get(index)));
        }
        if (side == DeckSide.RIGHT) {
            route.add(left(DOOR_ROW));
        }
        return route;
    }

    private static List<Position> concat(List<Position> first, List<Position> second) {
        return Stream.concat(first.stream(), second.stream()).distinct().toList();
    }

    private static Position left(String row) {
        return new Position(row + "L");
    }

    private static Position right(String row) {
        return new Position(row + "R");
    }
}
