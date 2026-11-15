package be.kdg.prog6.loading.adapter.in;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.DayOfMonth;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.loading.adapter.LoadingRabbitTopology;
import be.kdg.prog6.loading.domain.UldCode;
import be.kdg.prog6.loading.domain.Weight;
import be.kdg.prog6.loading.domain.WeightUnit;
import be.kdg.prog6.loading.port.in.BookUldCommand;
import be.kdg.prog6.loading.port.in.BookUldUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
public class UldMessageListener {

    private static final Logger log = LoggerFactory.getLogger(UldMessageListener.class);

    private final BookUldUseCase bookUldUseCase;
    private final Clock clock;

    public UldMessageListener(BookUldUseCase bookUldUseCase, Clock clock) {
        this.bookUldUseCase = bookUldUseCase;
        this.clock = clock;
    }

    @RabbitListener(queues = LoadingRabbitTopology.ULD_QUEUE)
    public void receive(UldMessage message) {
        log.info("Received ULD message {} for ULD {} on flight {}",
                message.messageId(), message.uldCode(), message.flightNumber());
        try {
            bookUldUseCase.bookUld(toCommand(message));
        } catch (RuntimeException e) {
            log.warn("ULD message {} could not be processed: {}", message.messageId(), e.getMessage());
            throw e;
        }
    }

    private BookUldCommand toCommand(UldMessage message) {
        LocalDate departureDate = new DayOfMonth(message.departureDay()).nearestDateTo(LocalDate.now(clock));
        return new BookUldCommand(
                new FlightId(new FlightNumber(message.flightNumber()), departureDate,
                        new AirportCode(message.departureAirport())),
                new UldCode(message.uldCode()),
                new Weight(message.weight(), WeightUnit.valueOf(message.weightUnit())),
                new AirportCode(message.destination()));
    }
}
