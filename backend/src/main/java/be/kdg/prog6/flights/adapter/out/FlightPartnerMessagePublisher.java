package be.kdg.prog6.flights.adapter.out;

import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.events.DepartureRecordedEvent;
import be.kdg.prog6.common.events.EstimatedDepartureChangedEvent;
import be.kdg.prog6.common.events.FlightScheduledEvent;
import be.kdg.prog6.flights.adapter.FlightsRabbitTopology;
import be.kdg.prog6.flights.domain.AircraftRegistration;
import be.kdg.prog6.flights.domain.ArrivalRecordedEvent;
import be.kdg.prog6.flights.domain.EstimatedArrivalChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class FlightPartnerMessagePublisher {

    static final int FORMAT_VERSION = 1;

    private static final Logger log = LoggerFactory.getLogger(FlightPartnerMessagePublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;
    private final String partnerExchange;

    public FlightPartnerMessagePublisher(RabbitTemplate rabbitTemplate, Clock clock,
                                   @Value("${partners.exchange}") String partnerExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.clock = clock;
        this.partnerExchange = partnerExchange;
    }

    @ApplicationModuleListener
    public void onFlightScheduled(FlightScheduledEvent event) {
        FlightId flightId = event.flightId();
        var flm = new FlmMessage(event.eventId(), Instant.now(clock), FORMAT_VERSION,
                flightId.flightNumber().value(), flightId.scheduledDepartureDate(),
                event.scheduledDeparture(), event.scheduledArrival(),
                flightId.departureAirport().code(), event.arrivalAirport().code());
        try {
            rabbitTemplate.convertAndSend(partnerExchange, FlightsRabbitTopology.FLM_ROUTING_KEY,
                    flm, message -> {
                        message.getMessageProperties().setMessageId(event.eventId().toString());
                        return message;
                    });
        } catch (AmqpException e) {
            logNotSent("FLM", event.eventId(), e);
            throw e;
        }
        log.info("Sent FLM {} for flight {}", event.eventId(), flightId);
    }

    @ApplicationModuleListener
    public void onEstimatedDepartureChanged(EstimatedDepartureChangedEvent event) {
        send(event.eventId(), new MvtTelegram(MvtType.ED, event.flightId(),
                new AircraftRegistration(event.aircraftRegistration()), event.flightId().departureAirport(),
                event.estimatedDeparture()));
    }

    @ApplicationModuleListener
    public void onEstimatedArrivalChanged(EstimatedArrivalChangedEvent event) {
        send(event.eventId(), new MvtTelegram(MvtType.EA, event.flightId(), event.aircraftRegistration(),
                event.arrivalAirport(), event.estimatedArrival()));
    }

    @ApplicationModuleListener
    public void onDepartureRecorded(DepartureRecordedEvent event) {
        send(event.eventId(), new MvtTelegram(MvtType.AD, event.flightId(),
                new AircraftRegistration(event.aircraftRegistration()), event.flightId().departureAirport(),
                event.actualDeparture()));
    }

    @ApplicationModuleListener
    public void onArrivalRecorded(ArrivalRecordedEvent event) {
        send(event.eventId(), new MvtTelegram(MvtType.AA, event.flightId(), event.aircraftRegistration(),
                event.arrivalAirport(), event.actualArrival()));
    }

    private void send(UUID messageId, MvtTelegram telegram) {
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
        properties.setMessageId(messageId.toString());
        properties.setTimestamp(Date.from(Instant.now(clock)));
        properties.setHeader("formatVersion", FORMAT_VERSION);
        Message message = new Message(telegram.toText().getBytes(StandardCharsets.US_ASCII), properties);
        try {
            rabbitTemplate.send(partnerExchange,
                    FlightsRabbitTopology.MVT_ROUTING_KEY_PREFIX + telegram.type().name().toLowerCase(), message);
        } catch (AmqpException e) {
            logNotSent("MVT-" + telegram.type(), messageId, e);
            throw e;
        }
        log.info("Sent MVT-{} {} for flight {}", telegram.type(), messageId, telegram.flightId());
    }

    private static void logNotSent(String type, UUID messageId, AmqpException e) {
        log.error("{} {} could not be sent, it is sent again once the connection is back: {}",
                type, messageId, e.getMessage());
    }
}
