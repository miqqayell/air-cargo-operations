package be.kdg.prog6.flights.adapter.in;

import be.kdg.prog6.common.events.LoadFinalisedEvent;
import be.kdg.prog6.flights.port.in.ReceiveFinalLoadCommand;
import be.kdg.prog6.flights.port.in.ReceiveFinalLoadUseCase;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class LoadFinalisedEventListener {

    private final ReceiveFinalLoadUseCase receiveFinalLoadUseCase;

    public LoadFinalisedEventListener(ReceiveFinalLoadUseCase receiveFinalLoadUseCase) {
        this.receiveFinalLoadUseCase = receiveFinalLoadUseCase;
    }

    @ApplicationModuleListener
    public void handle(LoadFinalisedEvent event) {
        receiveFinalLoadUseCase.receiveFinalLoad(new ReceiveFinalLoadCommand(event.flightId()));
    }
}
