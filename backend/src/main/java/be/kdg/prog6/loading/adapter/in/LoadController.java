package be.kdg.prog6.loading.adapter.in;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.loading.domain.Position;
import be.kdg.prog6.loading.domain.UldCode;
import be.kdg.prog6.loading.port.in.AssignPositionCommand;
import be.kdg.prog6.loading.port.in.AssignPositionUseCase;
import be.kdg.prog6.loading.port.in.CheckLoadQuery;
import be.kdg.prog6.loading.port.in.DeclareLoadFinalCommand;
import be.kdg.prog6.loading.port.in.DeclareLoadFinalUseCase;
import be.kdg.prog6.loading.port.in.GetLoadQuery;
import be.kdg.prog6.loading.port.in.TakeOffDeckCommand;
import be.kdg.prog6.loading.port.in.TakeOffDeckUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/loads/{flightNumber}/{scheduledDepartureDate}/{departureAirport}")
public class LoadController {

    private final GetLoadQuery getLoadQuery;
    private final CheckLoadQuery checkLoadQuery;
    private final AssignPositionUseCase assignPositionUseCase;
    private final TakeOffDeckUseCase takeOffDeckUseCase;
    private final DeclareLoadFinalUseCase declareLoadFinalUseCase;

    public LoadController(GetLoadQuery getLoadQuery,
                          CheckLoadQuery checkLoadQuery,
                          AssignPositionUseCase assignPositionUseCase,
                          TakeOffDeckUseCase takeOffDeckUseCase,
                          DeclareLoadFinalUseCase declareLoadFinalUseCase) {
        this.getLoadQuery = getLoadQuery;
        this.checkLoadQuery = checkLoadQuery;
        this.assignPositionUseCase = assignPositionUseCase;
        this.takeOffDeckUseCase = takeOffDeckUseCase;
        this.declareLoadFinalUseCase = declareLoadFinalUseCase;
    }

    @GetMapping
    public LoadDto getLoad(@PathVariable String flightNumber,
                           @PathVariable LocalDate scheduledDepartureDate,
                           @PathVariable String departureAirport) {
        return overview(flightId(flightNumber, scheduledDepartureDate, departureAirport));
    }

    @GetMapping("/checks")
    public LoadReportDto checkLoad(@PathVariable String flightNumber,
                                   @PathVariable LocalDate scheduledDepartureDate,
                                   @PathVariable String departureAirport) {
        return LoadReportDto.from(checkLoadQuery.checkLoad(flightId(flightNumber, scheduledDepartureDate, departureAirport)));
    }

    @PostMapping("/positions")
    public LoadDto assignPosition(@PathVariable String flightNumber,
                                  @PathVariable LocalDate scheduledDepartureDate,
                                  @PathVariable String departureAirport,
                                  @Valid @RequestBody AssignPositionRequest request) {
        FlightId flightId = flightId(flightNumber, scheduledDepartureDate, departureAirport);
        assignPositionUseCase.assignPosition(
                new AssignPositionCommand(flightId, new UldCode(request.uldCode()), new Position(request.position())));
        return overview(flightId);
    }

    @DeleteMapping("/positions/{uldCode}")
    public LoadDto takeOffDeck(@PathVariable String flightNumber,
                               @PathVariable LocalDate scheduledDepartureDate,
                               @PathVariable String departureAirport,
                               @PathVariable String uldCode) {
        FlightId flightId = flightId(flightNumber, scheduledDepartureDate, departureAirport);
        takeOffDeckUseCase.takeOffDeck(new TakeOffDeckCommand(flightId, new UldCode(uldCode)));
        return overview(flightId);
    }

    @PostMapping("/final")
    public LoadDto declareFinal(@PathVariable String flightNumber,
                                @PathVariable LocalDate scheduledDepartureDate,
                                @PathVariable String departureAirport,
                                @RequestParam(defaultValue = "false") boolean confirmSoftWarnings) {
        FlightId flightId = flightId(flightNumber, scheduledDepartureDate, departureAirport);
        declareLoadFinalUseCase.declareFinal(new DeclareLoadFinalCommand(flightId, confirmSoftWarnings));
        return overview(flightId);
    }

    private LoadDto overview(FlightId flightId) {
        return LoadDto.from(getLoadQuery.getLoad(flightId));
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
