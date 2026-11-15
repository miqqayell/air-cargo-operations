package be.kdg.prog6.flights.adapter.in;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.flights.domain.FlightSearchCriteria;
import be.kdg.prog6.flights.domain.FlightStatus;
import be.kdg.prog6.flights.port.in.ListFlightsQuery;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
public class FlightSearchController {

    private final ListFlightsQuery listFlightsQuery;

    public FlightSearchController(ListFlightsQuery listFlightsQuery) {
        this.listFlightsQuery = listFlightsQuery;
    }

    @GetMapping
    public List<FlightDto> searchFlights(
            @RequestParam(required = false) String flightNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String departureAirport,
            @RequestParam(required = false) String arrivalAirport,
            @RequestParam(required = false) FlightStatus status) {
        var criteria = new FlightSearchCriteria(
                isBlank(flightNumber) ? null : new FlightNumber(flightNumber.trim().toUpperCase()),
                date,
                isBlank(departureAirport) ? null : new AirportCode(departureAirport.trim().toUpperCase()),
                isBlank(arrivalAirport) ? null : new AirportCode(arrivalAirport.trim().toUpperCase()),
                status);
        return listFlightsQuery.listFlights(criteria).stream().map(FlightDto::from).toList();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalidInput(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
