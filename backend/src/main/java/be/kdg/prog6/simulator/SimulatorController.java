package be.kdg.prog6.simulator;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/simulator")
@Profile("!production")
public class SimulatorController {

    private final FeedPublisher feedPublisher;

    SimulatorController(FeedPublisher feedPublisher) {
        this.feedPublisher = feedPublisher;
    }

    @PostMapping("/schedules")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void simulateSchedules(@RequestBody List<FlightScheduleRequest> schedules) {
        schedules.forEach(feedPublisher::sendSsm);
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void simulateBookings(@RequestBody List<UldBookingRequest> bookings) {
        if (bookings.stream().anyMatch(booking -> booking.scheduledDepartureDate() == null)) {
            throw new IllegalArgumentException("Every booking needs a scheduled departure date");
        }
        bookings.forEach(feedPublisher::sendUld);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalidInput(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
