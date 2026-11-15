package be.kdg.prog6.loading.adapter.out;

import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.loading.domain.Boeing747Freighter;
import be.kdg.prog6.loading.domain.FloorPlan;
import be.kdg.prog6.loading.port.out.FloorPlanPort;
import org.springframework.stereotype.Component;

@Component
public class FleetFloorPlanAdapter implements FloorPlanPort {

    private static final FloorPlan ONLY_AIRCRAFT_TYPE = new Boeing747Freighter();

    @Override
    public FloorPlan floorPlanFor(FlightId flightId) {
        return ONLY_AIRCRAFT_TYPE;
    }
}
