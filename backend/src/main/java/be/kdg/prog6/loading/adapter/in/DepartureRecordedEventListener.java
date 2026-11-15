package be.kdg.prog6.loading.adapter.in;

import be.kdg.prog6.common.events.DepartureRecordedEvent;
import be.kdg.prog6.loading.port.in.MarkLoadDepartedCommand;
import be.kdg.prog6.loading.port.in.MarkLoadDepartedUseCase;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class DepartureRecordedEventListener {

    private final MarkLoadDepartedUseCase markLoadDepartedUseCase;

    public DepartureRecordedEventListener(MarkLoadDepartedUseCase markLoadDepartedUseCase) {
        this.markLoadDepartedUseCase = markLoadDepartedUseCase;
    }

    @ApplicationModuleListener
    public void handle(DepartureRecordedEvent event) {
        markLoadDepartedUseCase.markLoadDeparted(new MarkLoadDepartedCommand(event.flightId()));
    }
}
