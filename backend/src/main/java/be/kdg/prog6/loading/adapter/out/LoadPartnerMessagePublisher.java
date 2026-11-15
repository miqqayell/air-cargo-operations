package be.kdg.prog6.loading.adapter.out;

import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.events.LoadFinalisedEvent;
import be.kdg.prog6.loading.adapter.LoadingRabbitTopology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
public class LoadPartnerMessagePublisher {

    static final int FORMAT_VERSION = 1;

    private static final Logger log = LoggerFactory.getLogger(LoadPartnerMessagePublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;
    private final String partnerExchange;

    public LoadPartnerMessagePublisher(RabbitTemplate rabbitTemplate, Clock clock,
                                 @Value("${partners.exchange}") String partnerExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.clock = clock;
        this.partnerExchange = partnerExchange;
    }

    @ApplicationModuleListener
    public void onLoadFinalised(LoadFinalisedEvent event) {
        FlightId flightId = event.flightId();
        var cpm = new CpmMessage(event.eventId(), Instant.now(clock), FORMAT_VERSION,
                flightId.flightNumber().value(), flightId.scheduledDepartureDate(), flightId.departureAirport().code(),
                event.placedUlds().stream()
                        .map(uld -> new CpmMessage.CpmUld(uld.uldCode(), uld.position(), uld.weight(),
                                uld.weightUnit(), uld.destination()))
                        .toList());
        try {
            rabbitTemplate.convertAndSend(partnerExchange, LoadingRabbitTopology.CPM_ROUTING_KEY,
                    cpm, message -> {
                        message.getMessageProperties().setMessageId(event.eventId().toString());
                        return message;
                    });
        } catch (AmqpException e) {
            log.error("CPM {} could not be sent, it is sent again once the connection is back: {}",
                    event.eventId(), e.getMessage());
            throw e;
        }
        log.info("Sent CPM {} for flight {} with {} ULDs", event.eventId(), flightId, cpm.ulds().size());
    }
}
