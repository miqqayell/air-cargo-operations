package be.kdg.prog6.flights.adapter.in;

import be.kdg.prog6.flights.port.in.ListDelayCodesQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/delay-codes")
public class DelayCodeController {

    private final ListDelayCodesQuery listDelayCodesQuery;

    public DelayCodeController(ListDelayCodesQuery listDelayCodesQuery) {
        this.listDelayCodesQuery = listDelayCodesQuery;
    }

    @GetMapping
    public List<DelayCodeDto> listDelayCodes() {
        return listDelayCodesQuery.listDelayCodes().stream()
                .map(delayCode -> new DelayCodeDto(delayCode.code(), delayCode.description()))
                .toList();
    }

    public record DelayCodeDto(String code, String description) {
    }
}
