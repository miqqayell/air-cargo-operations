package be.kdg.prog6.loading.domain;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.domain.FlightNumber;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

final class LoadChecksFixture {

    static final FloorPlan DECK = new Boeing747Freighter();
    static final AirportCode ATH = new AirportCode("ATH");
    static final AirportCode FRA = new AirportCode("FRA");
    private static final LocalDateTime STD = LocalDateTime.of(2026, 10, 1, 6, 30);

    private final List<Uld> ulds = new ArrayList<>();
    private int serial = 10000;

    LoadChecksFixture on(String position, AirportCode destination) {
        return on(position, destination, new Weight(1000, WeightUnit.KG));
    }

    LoadChecksFixture on(String position, AirportCode destination, Weight weight) {
        ulds.add(Uld.restore(nextCode(), weight, destination, new Position(position)));
        return this;
    }

    LoadChecksFixture booked(AirportCode destination) {
        ulds.add(Uld.restore(nextCode(), new Weight(1000, WeightUnit.KG), destination, null));
        return this;
    }

    Load load() {
        FlightId flightId = new FlightId(new FlightNumber("HJ221"), STD.toLocalDate(), new AirportCode("BRU"));
        return Load.restore(flightId, ATH, STD, STD, LoadStatus.OPEN, false, ulds);
    }

    UldCode at(String position) {
        return ulds.stream()
                .filter(uld -> new Position(position).equals(uld.getPosition()))
                .map(Uld::getCode)
                .findFirst()
                .orElseThrow();
    }

    private UldCode nextCode() {
        return new UldCode("AKE" + serial++ + "R7");
    }
}
