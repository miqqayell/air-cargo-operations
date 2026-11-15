package be.kdg.prog6.loading.adapter.out;

import be.kdg.prog6.TestcontainersConfiguration;
import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.loading.domain.Boeing747Freighter;
import be.kdg.prog6.loading.domain.CheckKind;
import be.kdg.prog6.loading.domain.FloorPlan;
import be.kdg.prog6.loading.domain.Load;
import be.kdg.prog6.loading.domain.LoadStatus;
import be.kdg.prog6.loading.domain.LoadingPolicy;
import be.kdg.prog6.loading.domain.Position;
import be.kdg.prog6.loading.domain.Uld;
import be.kdg.prog6.loading.domain.UldCode;
import be.kdg.prog6.loading.domain.Weight;
import be.kdg.prog6.loading.domain.WeightUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class LoadPersistenceAdapterTest {

    private static final FloorPlan DECK = new Boeing747Freighter();
    private static final LoadingPolicy POLICY = new LoadingPolicy(Duration.ofHours(12), Duration.ofMinutes(20),
            BigDecimal.TEN, CheckKind.HARD);
    private static final LocalDateTime STD = LocalDateTime.of(2026, 11, 2, 6, 30);
    private static final LocalDateTime DURING_WINDOW = STD.minusHours(2);

    private static final AirportCode ATH = new AirportCode("ATH");
    private static final UldCode PLACED = new UldCode("AKE16689R7");
    private static final UldCode NOT_PLACED = new UldCode("PMC12345R7");

    @Autowired
    private LoadPersistenceAdapter adapter;

    private static FlightId flightId(String flightNumber) {
        return new FlightId(new FlightNumber(flightNumber), STD.toLocalDate(), new AirportCode("BRU"));
    }

    @Test
    void storedLoadIsLoadedWithItsUldsAndPositions() {
        Load load = Load.openFor(flightId("HJ801"), ATH, STD);
        load.estimateDeparture(STD.plusMinutes(30));
        load.bookUld(PLACED, new Weight(1000, WeightUnit.KG), ATH);
        load.bookUld(NOT_PLACED, new Weight(1500, WeightUnit.LB), ATH);
        load.assignPosition(PLACED, new Position("A1"), DECK, POLICY, DURING_WINDOW);
        load.declareFinal(DECK, POLICY, true, DURING_WINDOW);
        load.markDeparted();
        adapter.updateLoad(load);

        Load loaded = adapter.loadLoad(load.getFlightId()).orElseThrow();

        assertEquals(LoadStatus.FINAL, loaded.getStatus());
        assertTrue(loaded.isDeparted());
        assertEquals(ATH, loaded.getDestination());
        assertEquals(STD, loaded.getScheduledDeparture());
        assertEquals(STD.plusMinutes(30), loaded.getEstimatedDeparture());
        assertEquals(2, loaded.getUlds().size());
        Uld placed = uld(loaded, PLACED);
        assertEquals(new Position("A1"), placed.getPosition());
        assertEquals(new Weight(1000, WeightUnit.KG), placed.getWeight());
        Uld notPlaced = uld(loaded, NOT_PLACED);
        assertFalse(notPlaced.isPlaced());
        assertNull(notPlaced.getPosition());
        assertEquals(new Weight(1500, WeightUnit.LB), notPlaced.getWeight());
    }

    @Test
    void loadedLoadRecordsNoNewEvents() {
        Load load = Load.openFor(flightId("HJ802"), ATH, STD);
        load.declareFinal(DECK, POLICY, true, DURING_WINDOW);
        adapter.updateLoad(load);

        Load loaded = adapter.loadLoad(load.getFlightId()).orElseThrow();

        assertTrue(loaded.getDomainEvents().isEmpty());
    }

    @Test
    void unknownLoadIsNotFound() {
        assertTrue(adapter.loadLoad(flightId("HJ899")).isEmpty());
    }

    private static Uld uld(Load load, UldCode code) {
        return load.getUlds().stream().filter(uld -> uld.getCode().equals(code)).findFirst().orElseThrow();
    }
}
