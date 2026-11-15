package be.kdg.prog6.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Component
@Profile("!production")
class FeedPublisher {

    static final String FEEDS_EXCHANGE = "feeds";
    static final String SSM_ROUTING_KEY = "ssm";
    static final String ULD_ROUTING_KEY = "uld";
    static final int FORMAT_VERSION = 1;

    private static final Logger log = LoggerFactory.getLogger(FeedPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;

    FeedPublisher(RabbitTemplate rabbitTemplate, Clock clock) {
        this.rabbitTemplate = rabbitTemplate;
        this.clock = clock;
    }

    void sendSsm(FlightScheduleRequest request) {
        var message = new SsmMessage(UUID.randomUUID(), Instant.now(clock), FORMAT_VERSION,
                request.flightNumber(), request.departureAirport(), request.arrivalAirport(),
                request.aircraftRegistration(), request.firstDate(), request.lastDate(),
                request.weekdays(), request.departureTime(), request.arrivalTime());
        rabbitTemplate.convertAndSend(FEEDS_EXCHANGE, SSM_ROUTING_KEY, message);
        log.info("Sent SSM {} for flight {}", message.messageId(), message.flightNumber());
    }

    void sendUld(UldBookingRequest request) {
        var message = new UldMessage(UUID.randomUUID(), Instant.now(clock), FORMAT_VERSION,
                request.flightNumber(), request.scheduledDepartureDate().getDayOfMonth(),
                request.departureAirport(), request.uldCode(), request.weight(), request.weightUnit(),
                request.destination());
        rabbitTemplate.convertAndSend(FEEDS_EXCHANGE, ULD_ROUTING_KEY, message);
        log.info("Sent ULD message {} for ULD {}", message.messageId(), message.uldCode());
    }
}
