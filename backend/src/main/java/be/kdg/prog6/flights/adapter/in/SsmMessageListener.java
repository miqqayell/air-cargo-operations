package be.kdg.prog6.flights.adapter.in;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.flights.adapter.FlightsRabbitTopology;
import be.kdg.prog6.flights.domain.AircraftRegistration;
import be.kdg.prog6.flights.domain.FlightSchedule;
import be.kdg.prog6.flights.port.in.ScheduleFlightsCommand;
import be.kdg.prog6.flights.port.in.ScheduleFlightsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SsmMessageListener {

    private static final Logger log = LoggerFactory.getLogger(SsmMessageListener.class);

    private final ScheduleFlightsUseCase scheduleFlightsUseCase;

    public SsmMessageListener(ScheduleFlightsUseCase scheduleFlightsUseCase) {
        this.scheduleFlightsUseCase = scheduleFlightsUseCase;
    }

    @RabbitListener(queues = FlightsRabbitTopology.SSM_QUEUE)
    public void receive(SsmMessage message) {
        log.info("Received SSM {} for flight {}", message.messageId(), message.flightNumber());
        try {
            scheduleFlightsUseCase.scheduleFlights(new ScheduleFlightsCommand(toSchedule(message)));
        } catch (RuntimeException e) {
            log.warn("SSM {} could not be processed: {}", message.messageId(), e.getMessage());
            throw e;
        }
    }

    private static FlightSchedule toSchedule(SsmMessage message) {
        return new FlightSchedule(
                new FlightNumber(message.flightNumber()),
                new AirportCode(message.departureAirport()),
                new AirportCode(message.arrivalAirport()),
                new AircraftRegistration(message.aircraftRegistration()),
                Objects.requireNonNull(message.firstDate(), "First date is required"),
                Objects.requireNonNull(message.lastDate(), "Last date is required"),
                Objects.requireNonNull(message.weekdays(), "Weekdays are required"),
                Objects.requireNonNull(message.departureTime(), "Departure time is required"),
                Objects.requireNonNull(message.arrivalTime(), "Arrival time is required"));
    }
}
