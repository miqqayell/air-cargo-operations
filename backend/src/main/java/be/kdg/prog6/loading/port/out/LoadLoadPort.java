package be.kdg.prog6.loading.port.out;

import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.loading.domain.Load;

import java.util.Optional;

public interface LoadLoadPort {

    Optional<Load> loadLoad(FlightId flightId);
}
