package be.kdg.prog6.flights.core;

import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.flights.domain.FlightSearchCriteria;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultListFlightsQueryTest {

    private static final Clock OCTOBER_20 = Clock.fixed(Instant.parse("2026-10-20T09:00:00Z"), ZoneOffset.UTC);

    private final List<FlightSearchCriteria> searches = new ArrayList<>();
    private final DefaultListFlightsQuery query = new DefaultListFlightsQuery(criteria -> {
        searches.add(criteria);
        return List.of();
    }, OCTOBER_20);

    @Test
    void withoutCriteriaItSearchesTodaysFlights() {
        query.listFlights(new FlightSearchCriteria(null, null, null, null, null));

        assertEquals(List.of(FlightSearchCriteria.onDate(LocalDate.of(2026, 10, 20))), searches);
    }

    @Test
    void withCriteriaItSearchesExactlyThose() {
        var criteria = new FlightSearchCriteria(new FlightNumber("HJ221"), null, null, null, null);

        query.listFlights(criteria);

        assertEquals(List.of(criteria), searches);
    }
}
