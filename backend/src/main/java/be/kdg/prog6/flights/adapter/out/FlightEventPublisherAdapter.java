package be.kdg.prog6.flights.adapter.out;

import be.kdg.prog6.flights.domain.Flight;
import be.kdg.prog6.flights.port.out.FlightRemovePort;
import be.kdg.prog6.flights.port.out.FlightUpdatePort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class FlightEventPublisherAdapter implements FlightUpdatePort, FlightRemovePort {

    private final ApplicationEventPublisher eventPublisher;

    public FlightEventPublisherAdapter(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void updateFlight(Flight flight) {
        publishEventsOf(flight);
    }

    @Override
    public void removeFlight(Flight flight) {
        publishEventsOf(flight);
    }

    private void publishEventsOf(Flight flight) {
        flight.getDomainEvents().forEach(eventPublisher::publishEvent);
        flight.clearDomainEvents();
    }
}
