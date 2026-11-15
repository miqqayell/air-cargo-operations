package be.kdg.prog6.loading.adapter.in;

import be.kdg.prog6.common.events.EstimatedDepartureChangedEvent;
import be.kdg.prog6.loading.port.in.RecordEstimatedDepartureCommand;
import be.kdg.prog6.loading.port.in.RecordEstimatedDepartureUseCase;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class EstimatedDepartureChangedEventListener {

    private final RecordEstimatedDepartureUseCase recordEstimatedDepartureUseCase;

    public EstimatedDepartureChangedEventListener(RecordEstimatedDepartureUseCase recordEstimatedDepartureUseCase) {
        this.recordEstimatedDepartureUseCase = recordEstimatedDepartureUseCase;
    }

    @ApplicationModuleListener
    public void handle(EstimatedDepartureChangedEvent event) {
        recordEstimatedDepartureUseCase.recordEstimatedDeparture(
                new RecordEstimatedDepartureCommand(event.flightId(), event.estimatedDeparture()));
    }
}
