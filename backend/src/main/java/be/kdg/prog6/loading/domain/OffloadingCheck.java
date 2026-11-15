package be.kdg.prog6.loading.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class OffloadingCheck implements LoadCheck {

    public static final String NAME = "Offloading";

    private final CheckKind kind;

    public OffloadingCheck(CheckKind kind) {
        this.kind = kind;
    }

    @Override
    public CheckResult check(Load load, FloorPlan floorPlan) {
        Map<Position, Uld> onDeck = load.getUlds().stream()
                .filter(uld -> uld.isPlaced() && floorPlan.contains(uld.getPosition()))
                .collect(Collectors.toMap(Uld::getPosition, Function.identity()));

        List<BlockedUld> blocked = new ArrayList<>();
        for (DeckPosition deckPosition : floorPlan.positions()) {
            Uld uld = onDeck.get(deckPosition.position());
            if (uld == null || !uld.getDestination().equals(load.getDestination())) {
                continue;
            }
            List<List<Uld>> blockersPerRoute = floorPlan.routesToDoor(uld.getPosition()).stream()
                    .map(route -> route.stream()
                            .map(onDeck::get)
                            .filter(Objects::nonNull)
                            .filter(other -> !other.getDestination().equals(load.getDestination()))
                            .toList())
                    .toList();
            if (blockersPerRoute.stream().noneMatch(List::isEmpty)) {
                blocked.add(new BlockedUld(uld.getCode(), blockersPerRoute.stream()
                        .flatMap(List::stream)
                        .map(Uld::getCode)
                        .distinct()
                        .toList()));
            }
        }

        if (blocked.isEmpty()) {
            return CheckResult.pass(NAME, kind);
        }
        return CheckResult.fail(NAME, kind, describe(blocked, load, onDeck), blocked);
    }

    private static String describe(List<BlockedUld> blocked, Load load, Map<Position, Uld> onDeck) {
        Map<UldCode, Position> positions = onDeck.values().stream()
                .collect(Collectors.toMap(Uld::getCode, Uld::getPosition));
        return blocked.stream()
                .map(blockedUld -> "%s on %s is blocked by %s".formatted(
                        blockedUld.uld().value(), positions.get(blockedUld.uld()).code(),
                        blockedUld.blockedBy().stream()
                                .map(blocker -> blocker.value() + " on " + positions.get(blocker).code())
                                .collect(Collectors.joining(", "))))
                .collect(Collectors.joining("; ", "Cargo for " + load.getDestination().code() + " cannot leave the aircraft: ",
                        ". Move the blocking ULDs off the way to the door at row R, or move the "
                                + load.getDestination().code() + " cargo closer to the door."));
    }
}
