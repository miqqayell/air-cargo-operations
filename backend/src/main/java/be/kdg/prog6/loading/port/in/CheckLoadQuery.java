package be.kdg.prog6.loading.port.in;

import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.loading.domain.LoadReport;

public interface CheckLoadQuery {

    LoadReport checkLoad(FlightId flightId);
}
