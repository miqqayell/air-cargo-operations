package be.kdg.prog6.loading.core;

import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.loading.domain.Load;
import be.kdg.prog6.loading.domain.LoadingWindow;
import be.kdg.prog6.loading.port.in.GetLoadQuery;
import be.kdg.prog6.loading.port.in.LoadOverview;
import be.kdg.prog6.loading.port.out.FloorPlanPort;
import be.kdg.prog6.loading.port.out.LoadLoadPort;
import be.kdg.prog6.loading.port.out.LoadingPolicyPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class DefaultGetLoadQuery implements GetLoadQuery {

    private final LoadLoadPort loadLoadPort;
    private final FloorPlanPort floorPlanPort;
    private final LoadingPolicyPort loadingPolicyPort;
    private final Clock clock;

    public DefaultGetLoadQuery(LoadLoadPort loadLoadPort, FloorPlanPort floorPlanPort,
                               LoadingPolicyPort loadingPolicyPort, Clock clock) {
        this.loadLoadPort = loadLoadPort;
        this.floorPlanPort = floorPlanPort;
        this.loadingPolicyPort = loadingPolicyPort;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public LoadOverview getLoad(FlightId flightId) {
        Load load = loadLoadPort.loadLoad(flightId)
                .orElseThrow(() -> new NoSuchElementException("No load for flight " + flightId));
        LoadingWindow loadingWindow = load.loadingWindow(loadingPolicyPort.loadingPolicy());
        return new LoadOverview(load, floorPlanPort.floorPlanFor(flightId), loadingWindow,
                loadingWindow.isOpenAt(LocalDateTime.now(clock)));
    }
}
