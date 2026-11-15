package be.kdg.prog6.loading.port.in;

import be.kdg.prog6.loading.domain.FloorPlan;
import be.kdg.prog6.loading.domain.Load;
import be.kdg.prog6.loading.domain.LoadingWindow;

public record LoadOverview(Load load, FloorPlan floorPlan, LoadingWindow loadingWindow, boolean loadingWindowOpen) {
}
