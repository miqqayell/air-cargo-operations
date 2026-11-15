package be.kdg.prog6.loading.adapter.in;

import be.kdg.prog6.loading.port.in.GetLoadingPolicyQuery;
import be.kdg.prog6.loading.port.in.UpdateLoadingPolicyCommand;
import be.kdg.prog6.loading.port.in.UpdateLoadingPolicyUseCase;
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
@RequestMapping("/api/policy-settings/loading")
public class LoadingPolicyController {

    private final GetLoadingPolicyQuery getLoadingPolicyQuery;
    private final UpdateLoadingPolicyUseCase updateLoadingPolicyUseCase;

    public LoadingPolicyController(GetLoadingPolicyQuery getLoadingPolicyQuery,
                                   UpdateLoadingPolicyUseCase updateLoadingPolicyUseCase) {
        this.getLoadingPolicyQuery = getLoadingPolicyQuery;
        this.updateLoadingPolicyUseCase = updateLoadingPolicyUseCase;
    }

    @GetMapping
    public LoadingPolicyDto getLoadingPolicy() {
        return LoadingPolicyDto.from(getLoadingPolicyQuery.getLoadingPolicy());
    }

    @PutMapping
    public LoadingPolicyDto updateLoadingPolicy(@Valid @RequestBody LoadingPolicyRequest request) {
        var command = new UpdateLoadingPolicyCommand(request.toLoadingPolicy());
        return LoadingPolicyDto.from(updateLoadingPolicyUseCase.updateLoadingPolicy(command));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalidInput(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
