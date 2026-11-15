package be.kdg.prog6.flights.adapter.in;

import be.kdg.prog6.flights.port.in.GetFlightPolicyQuery;
import be.kdg.prog6.flights.port.in.UpdateFlightPolicyCommand;
import be.kdg.prog6.flights.port.in.UpdateFlightPolicyUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/policy-settings/flights")
public class FlightPolicyController {

    private final GetFlightPolicyQuery getFlightPolicyQuery;
    private final UpdateFlightPolicyUseCase updateFlightPolicyUseCase;

    public FlightPolicyController(GetFlightPolicyQuery getFlightPolicyQuery,
                                  UpdateFlightPolicyUseCase updateFlightPolicyUseCase) {
        this.getFlightPolicyQuery = getFlightPolicyQuery;
        this.updateFlightPolicyUseCase = updateFlightPolicyUseCase;
    }

    @GetMapping
    public FlightPolicyDto getFlightPolicy() {
        return FlightPolicyDto.from(getFlightPolicyQuery.getFlightPolicy());
    }

    @PutMapping
    public FlightPolicyDto updateFlightPolicy(@Valid @RequestBody FlightPolicyRequest request) {
        var command = new UpdateFlightPolicyCommand(request.toFlightPolicy());
        return FlightPolicyDto.from(updateFlightPolicyUseCase.updateFlightPolicy(command));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalidInput(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
