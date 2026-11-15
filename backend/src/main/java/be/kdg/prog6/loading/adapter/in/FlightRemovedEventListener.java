package be.kdg.prog6.loading.adapter.in;

import be.kdg.prog6.common.events.FlightRemovedEvent;
import be.kdg.prog6.loading.port.in.RemoveLoadCommand;
import be.kdg.prog6.loading.port.in.RemoveLoadUseCase;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class FlightRemovedEventListener {

    private final RemoveLoadUseCase removeLoadUseCase;

    public FlightRemovedEventListener(RemoveLoadUseCase removeLoadUseCase) {
        this.removeLoadUseCase = removeLoadUseCase;
    }

    @ApplicationModuleListener
    public void handle(FlightRemovedEvent event) {
        removeLoadUseCase.removeLoad(new RemoveLoadCommand(event.flightId()));
    }
}
