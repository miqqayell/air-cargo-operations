package be.kdg.prog6.loading.adapter.in;

import be.kdg.prog6.common.events.FlightScheduledEvent;
import be.kdg.prog6.loading.port.in.OpenLoadCommand;
import be.kdg.prog6.loading.port.in.OpenLoadUseCase;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class FlightScheduledEventListener {

    private final OpenLoadUseCase openLoadUseCase;

    public FlightScheduledEventListener(OpenLoadUseCase openLoadUseCase) {
        this.openLoadUseCase = openLoadUseCase;
    }

    @ApplicationModuleListener
    public void handle(FlightScheduledEvent event) {
        openLoadUseCase.openLoad(
                new OpenLoadCommand(event.flightId(), event.arrivalAirport(), event.scheduledDeparture()));
    }
}
