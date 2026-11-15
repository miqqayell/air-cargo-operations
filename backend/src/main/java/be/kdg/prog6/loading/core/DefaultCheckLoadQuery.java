package be.kdg.prog6.loading.core;

import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.loading.domain.Load;
import be.kdg.prog6.loading.domain.LoadReport;
import be.kdg.prog6.loading.port.in.CheckLoadQuery;
import be.kdg.prog6.loading.port.out.FloorPlanPort;
import be.kdg.prog6.loading.port.out.LoadLoadPort;
import be.kdg.prog6.loading.port.out.LoadingPolicyPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class DefaultCheckLoadQuery implements CheckLoadQuery {

    private final LoadLoadPort loadLoadPort;
    private final FloorPlanPort floorPlanPort;
    private final LoadingPolicyPort loadingPolicyPort;

    public DefaultCheckLoadQuery(LoadLoadPort loadLoadPort, FloorPlanPort floorPlanPort,
                                 LoadingPolicyPort loadingPolicyPort) {
        this.loadLoadPort = loadLoadPort;
        this.floorPlanPort = floorPlanPort;
        this.loadingPolicyPort = loadingPolicyPort;
    }

    @Override
    @Transactional(readOnly = true)
    public LoadReport checkLoad(FlightId flightId) {
        Load load = loadLoadPort.loadLoad(flightId)
                .orElseThrow(() -> new NoSuchElementException("No load for flight " + flightId));
        return load.check(loadingPolicyPort.loadingPolicy().loadChecks(), floorPlanPort.floorPlanFor(flightId));
    }
}
