package be.kdg.prog6.loading.port.out;

import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.loading.domain.FloorPlan;

public interface FloorPlanPort {

    FloorPlan floorPlanFor(FlightId flightId);
}
