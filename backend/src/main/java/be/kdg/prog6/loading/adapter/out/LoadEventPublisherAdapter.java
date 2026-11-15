package be.kdg.prog6.loading.adapter.out;

import be.kdg.prog6.loading.domain.Load;
import be.kdg.prog6.loading.port.out.LoadUpdatePort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class LoadEventPublisherAdapter implements LoadUpdatePort {

    private final ApplicationEventPublisher eventPublisher;

    public LoadEventPublisherAdapter(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void updateLoad(Load load) {
        load.getDomainEvents().forEach(eventPublisher::publishEvent);
        load.clearDomainEvents();
    }
}
