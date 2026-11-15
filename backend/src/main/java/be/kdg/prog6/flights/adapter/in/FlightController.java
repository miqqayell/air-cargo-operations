package be.kdg.prog6.flights.adapter.in;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.flights.domain.DelayId;
import be.kdg.prog6.flights.port.in.ChangeDelayCommand;
import be.kdg.prog6.flights.port.in.ChangeDelayUseCase;
import be.kdg.prog6.flights.port.in.GetFlightQuery;
import be.kdg.prog6.flights.port.in.RecordActualTimeCommand;
import be.kdg.prog6.flights.port.in.RecordArrivalUseCase;
import be.kdg.prog6.flights.port.in.RecordDepartureUseCase;
import be.kdg.prog6.flights.port.in.RegisterDelayCommand;
import be.kdg.prog6.flights.port.in.RegisterDelayUseCase;
import be.kdg.prog6.flights.port.in.RemoveDelayCommand;
import be.kdg.prog6.flights.port.in.RemoveDelayUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/flights/{flightNumber}/{scheduledDepartureDate}/{departureAirport}")
public class FlightController {

    private final GetFlightQuery getFlightQuery;
    private final RegisterDelayUseCase registerDelayUseCase;
    private final ChangeDelayUseCase changeDelayUseCase;
    private final RemoveDelayUseCase removeDelayUseCase;
    private final RecordDepartureUseCase recordDepartureUseCase;
    private final RecordArrivalUseCase recordArrivalUseCase;

    public FlightController(GetFlightQuery getFlightQuery,
                            RegisterDelayUseCase registerDelayUseCase,
                            ChangeDelayUseCase changeDelayUseCase,
                            RemoveDelayUseCase removeDelayUseCase,
                            RecordDepartureUseCase recordDepartureUseCase,
                            RecordArrivalUseCase recordArrivalUseCase) {
        this.getFlightQuery = getFlightQuery;
        this.registerDelayUseCase = registerDelayUseCase;
        this.changeDelayUseCase = changeDelayUseCase;
        this.removeDelayUseCase = removeDelayUseCase;
        this.recordDepartureUseCase = recordDepartureUseCase;
        this.recordArrivalUseCase = recordArrivalUseCase;
    }

    @GetMapping
    public FlightDto getFlight(@PathVariable String flightNumber,
                               @PathVariable LocalDate scheduledDepartureDate,
                               @PathVariable String departureAirport) {
        return FlightDto.from(getFlightQuery.getFlight(flightId(flightNumber, scheduledDepartureDate, departureAirport)));
    }

    @PostMapping("/delays")
    public FlightDto registerDelay(@PathVariable String flightNumber,
                                   @PathVariable LocalDate scheduledDepartureDate,
                                   @PathVariable String departureAirport,
                                   @Valid @RequestBody RegisterDelayRequest request) {
        var command = new RegisterDelayCommand(
                flightId(flightNumber, scheduledDepartureDate, departureAirport),
                request.side(), request.minutes(), request.delayCode(), request.description());
        return FlightDto.from(registerDelayUseCase.registerDelay(command));
    }

    @PutMapping("/delays/{delayId}")
    public FlightDto changeDelay(@PathVariable String flightNumber,
                                 @PathVariable LocalDate scheduledDepartureDate,
                                 @PathVariable String departureAirport,
                                 @PathVariable UUID delayId,
                                 @Valid @RequestBody ChangeDelayRequest request) {
        var command = new ChangeDelayCommand(
                flightId(flightNumber, scheduledDepartureDate, departureAirport),
                new DelayId(delayId), request.minutes(), request.description());
        return FlightDto.from(changeDelayUseCase.changeDelay(command));
    }

    @DeleteMapping("/delays/{delayId}")
    public FlightDto removeDelay(@PathVariable String flightNumber,
                                 @PathVariable LocalDate scheduledDepartureDate,
                                 @PathVariable String departureAirport,
                                 @PathVariable UUID delayId) {
        var command = new RemoveDelayCommand(
                flightId(flightNumber, scheduledDepartureDate, departureAirport), new DelayId(delayId));
        return FlightDto.from(removeDelayUseCase.removeDelay(command));
    }

    @PostMapping("/departure")
    public FlightDto recordDeparture(@PathVariable String flightNumber,
                                     @PathVariable LocalDate scheduledDepartureDate,
                                     @PathVariable String departureAirport,
                                     @Valid @RequestBody ActualTimeRequest request) {
        var command = new RecordActualTimeCommand(
                flightId(flightNumber, scheduledDepartureDate, departureAirport), request.utc());
        return FlightDto.from(recordDepartureUseCase.recordDeparture(command));
    }

    @PostMapping("/arrival")
    public FlightDto recordArrival(@PathVariable String flightNumber,
                                   @PathVariable LocalDate scheduledDepartureDate,
                                   @PathVariable String departureAirport,
                                   @Valid @RequestBody ActualTimeRequest request) {
        var command = new RecordActualTimeCommand(
                flightId(flightNumber, scheduledDepartureDate, departureAirport), request.utc());
        return FlightDto.from(recordArrivalUseCase.recordArrival(command));
    }

    private static FlightId flightId(String flightNumber, LocalDate scheduledDepartureDate, String departureAirport) {
        return new FlightId(new FlightNumber(flightNumber), scheduledDepartureDate, new AirportCode(departureAirport));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleNotAllowed(IllegalStateException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalidInput(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
