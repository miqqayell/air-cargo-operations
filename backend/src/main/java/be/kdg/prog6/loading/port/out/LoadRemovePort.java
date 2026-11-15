package be.kdg.prog6.loading.port.out;

import be.kdg.prog6.common.domain.FlightId;

public interface LoadRemovePort {

    void removeLoad(FlightId flightId);
}
