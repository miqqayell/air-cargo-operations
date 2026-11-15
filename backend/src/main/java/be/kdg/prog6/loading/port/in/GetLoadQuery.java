package be.kdg.prog6.loading.port.in;

import be.kdg.prog6.common.domain.FlightId;

public interface GetLoadQuery {

    LoadOverview getLoad(FlightId flightId);
}
