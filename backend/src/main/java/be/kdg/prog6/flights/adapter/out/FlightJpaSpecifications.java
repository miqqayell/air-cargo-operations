package be.kdg.prog6.flights.adapter.out;

import be.kdg.prog6.flights.domain.FlightSearchCriteria;
import be.kdg.prog6.flights.domain.FlightStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

final class FlightJpaSpecifications {

    private FlightJpaSpecifications() {
    }

    static Specification<FlightJpaEntity> matching(FlightSearchCriteria criteria) {
        List<Specification<FlightJpaEntity>> specifications = new ArrayList<>();
        if (criteria.flightNumber() != null) {
            specifications.add((root, query, builder) ->
                    builder.equal(root.get("id").get("flightNumber"), criteria.flightNumber().value()));
        }
        if (criteria.scheduledDepartureDate() != null) {
            specifications.add((root, query, builder) ->
                    builder.equal(root.get("id").get("scheduledDepartureDate"), criteria.scheduledDepartureDate()));
        }
        if (criteria.departureAirport() != null) {
            specifications.add((root, query, builder) ->
                    builder.equal(root.get("id").get("departureAirport"), criteria.departureAirport().code()));
        }
        if (criteria.arrivalAirport() != null) {
            specifications.add((root, query, builder) ->
                    builder.equal(root.get("arrivalAirport"), criteria.arrivalAirport().code()));
        }
        if (criteria.status() != null) {
            specifications.add(withStatus(criteria.status()));
        }
        return Specification.allOf(specifications);
    }

    private static Specification<FlightJpaEntity> withStatus(FlightStatus status) {
        return (root, query, builder) -> switch (status) {
            case SCHEDULED -> builder.isNull(root.get("actualDeparture"));
            case DEPARTED -> builder.and(builder.isNotNull(root.get("actualDeparture")),
                    builder.isNull(root.get("actualArrival")));
            case LANDED -> builder.isNotNull(root.get("actualArrival"));
        };
    }
}
