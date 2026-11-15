package be.kdg.prog6.flights.port.in;

import be.kdg.prog6.flights.domain.Flight;

public interface ReceiveFinalLoadUseCase {

    Flight receiveFinalLoad(ReceiveFinalLoadCommand command);
}
