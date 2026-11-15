package be.kdg.prog6.loading.domain;

import java.util.List;
import java.util.stream.Collectors;

public final class AllCargoLoadedCheck implements LoadCheck {

    public static final String NAME = "Not all cargo loaded";

    @Override
    public CheckResult check(Load load, FloorPlan floorPlan) {
        List<Uld> leftBehind = load.getUlds().stream()
                .filter(uld -> !uld.isPlaced() || !floorPlan.contains(uld.getPosition()))
                .toList();
        if (leftBehind.isEmpty()) {
            return CheckResult.pass(NAME, CheckKind.SOFT);
        }
        return CheckResult.fail(NAME, CheckKind.SOFT, leftBehind.stream()
                .map(uld -> uld.getCode().value())
                .collect(Collectors.joining(", ", leftBehind.size() + " booked ULD(s) have no position and stay behind: ", "")));
    }
}
