package be.kdg.prog6.flights.adapter.in;

import be.kdg.prog6.flights.port.in.RemoveExpiredFlightsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FlightRetentionScheduler {

    private final RemoveExpiredFlightsUseCase removeExpiredFlightsUseCase;

    public FlightRetentionScheduler(RemoveExpiredFlightsUseCase removeExpiredFlightsUseCase) {
        this.removeExpiredFlightsUseCase = removeExpiredFlightsUseCase;
    }

    @Scheduled(cron = "${flights.retention.cron}", zone = "UTC")
    void removeExpiredFlights() {
        removeExpiredFlightsUseCase.removeExpiredFlights();
    }
}
