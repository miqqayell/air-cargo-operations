package be.kdg.prog6.loading.domain;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.common.events.LoadFinalisedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadTest {

    private static final FloorPlan DECK = new Boeing747Freighter();
    private static final LoadingPolicy POLICY = new LoadingPolicy(Duration.ofHours(12), Duration.ofMinutes(20),
            BigDecimal.TEN, CheckKind.HARD);
    private static final LocalDateTime STD = LocalDateTime.of(2026, 10, 1, 6, 30);
    private static final LocalDateTime DURING_WINDOW = STD.minusHours(2);

    private static final AirportCode ATH = new AirportCode("ATH");
    private static final UldCode FIRST = new UldCode("AKE16689R7");
    private static final UldCode SECOND = new UldCode("PMC12345R7");

    private Load load;

    @BeforeEach
    void setUp() {
        FlightId flightId = new FlightId(new FlightNumber("HJ221"), STD.toLocalDate(), new AirportCode("BRU"));
        load = Load.openFor(flightId, ATH, STD);
        load.bookUld(FIRST, new Weight(1000, WeightUnit.KG), ATH);
        load.bookUld(SECOND, new Weight(800, WeightUnit.KG), ATH);
    }

    private void place(UldCode uld, String position) {
        load.assignPosition(uld, new Position(position), DECK, POLICY, DURING_WINDOW);
    }

    private void finalise() {
        load.declareFinal(DECK, POLICY, true, DURING_WINDOW);
    }

    @Test
    void secondBookingOfTheSameUldIsIgnored() {
        assertFalse(load.bookUld(FIRST, new Weight(1000, WeightUnit.KG), ATH));
        assertEquals(2, load.getUlds().size());
    }

    @Test
    void cargoCanStillBeBookedOnAFinalLoadButNotOnADepartedFlight() {
        finalise();
        assertTrue(load.bookUld(new UldCode("AKE99999R7"), new Weight(500, WeightUnit.KG), ATH));

        load.markDeparted();

        assertThrows(IllegalStateException.class,
                () -> load.bookUld(new UldCode("AKE88888R7"), new Weight(500, WeightUnit.KG), ATH));
    }

    @Test
    void positionMustBeOnTheFloorPlan() {
        assertThrows(IllegalArgumentException.class, () -> place(FIRST, "ZZ99"));
    }

    @Test
    void uldCanMoveToAnotherPosition() {
        place(FIRST, "CL");

        place(FIRST, "DR");

        assertEquals(new Position("DR"), load.getUlds().getFirst().getPosition());
    }

    @Test
    void uldCanBeTakenOffTheDeck() {
        place(FIRST, "CL");

        load.takeOffDeck(FIRST, POLICY, DURING_WINDOW);

        assertFalse(load.getUlds().getFirst().isPlaced());
    }

    @Test
    void positionHoldsAtMostOneUld() {
        place(FIRST, "CL");

        assertThrows(PositionOccupiedException.class, () -> place(SECOND, "CL"));
    }

    @Test
    void declaringTheLoadFinalRecordsAnEvent() {
        finalise();

        assertEquals(LoadStatus.FINAL, load.getStatus());
        assertInstanceOf(LoadFinalisedEvent.class, load.getDomainEvents().getFirst());
    }

    @Test
    void finalLoadEventListsOnlyTheUldsWithAPosition() {
        place(FIRST, "A1");

        finalise();

        var event = assertInstanceOf(LoadFinalisedEvent.class, load.getDomainEvents().getFirst());
        assertEquals(List.of(new LoadFinalisedEvent.PlacedUld("AKE16689R7", "A1", 1000, "KG", "ATH")),
                event.placedUlds());
    }

    @Test
    void finalLoadCannotChange() {
        finalise();

        assertThrows(IllegalStateException.class, () -> place(FIRST, "CL"));
        assertThrows(IllegalStateException.class, () -> load.takeOffDeck(FIRST, POLICY, DURING_WINDOW));
    }

    @Test
    void loadingWindowOpensTwelveHoursBeforeStdAndClosesTwentyMinutesBeforeEtd() {
        LoadingWindow window = load.loadingWindow(POLICY);

        assertEquals(STD.minusHours(12), window.opensAt());
        assertEquals(STD.minusMinutes(20), window.closesAt());
    }

    @Test
    void departureDelayPushesTheClosingButNotTheOpening() {
        load.estimateDeparture(STD.plusMinutes(45));

        LoadingWindow window = load.loadingWindow(POLICY);

        assertEquals(STD.minusHours(12), window.opensAt());
        assertEquals(STD.plusMinutes(25), window.closesAt());
    }

    @Test
    void windowIsOpenFromItsOpeningUntilJustBeforeItsClosing() {
        LoadingWindow window = load.loadingWindow(POLICY);

        assertFalse(window.isOpenAt(STD.minusHours(12).minusMinutes(1)));
        assertTrue(window.isOpenAt(STD.minusHours(12)));
        assertTrue(window.isOpenAt(STD.minusMinutes(21)));
        assertFalse(window.isOpenAt(STD.minusMinutes(20)));
    }

    @Test
    void loadCannotChangeBeforeTheWindowOpens() {
        assertThrows(IllegalStateException.class,
                () -> load.assignPosition(FIRST, new Position("CL"), DECK, POLICY, STD.minusHours(13)));
    }

    @Test
    void loadCannotChangeOrBecomeFinalAfterTheWindowCloses() {
        LocalDateTime afterClosing = STD.minusMinutes(10);

        assertThrows(IllegalStateException.class,
                () -> load.assignPosition(FIRST, new Position("CL"), DECK, POLICY, afterClosing));
        assertThrows(IllegalStateException.class, () -> load.declareFinal(DECK, POLICY, true, afterClosing));
    }

    @Test
    void failingHardCheckPreventsTheFinalLoad() {
        place(FIRST, "CL");

        var refusal = assertThrows(LoadChecksFailedException.class,
                () -> load.declareFinal(DECK, POLICY, true, DURING_WINDOW));

        assertTrue(refusal.getMessage().startsWith("The load cannot be declared final while a hard check fails. "
                + "Lateral balance: "));
        assertEquals(LoadStatus.OPEN, load.getStatus());
    }

    @Test
    void softWarningsMustBeConfirmed() {
        var refusal = assertThrows(LoadChecksFailedException.class,
                () -> load.declareFinal(DECK, POLICY, false, DURING_WINDOW));

        assertTrue(refusal.getMessage().contains("Not all cargo loaded"));
        assertEquals(LoadStatus.OPEN, load.getStatus());
    }

    @Test
    void confirmedSoftWarningsDoNotPreventTheFinalLoad() {
        load.declareFinal(DECK, POLICY, true, DURING_WINDOW);

        assertEquals(LoadStatus.FINAL, load.getStatus());
    }

    @Test
    void bookingIsPossibleBeforeTheWindowOpens() {
        assertTrue(load.bookUld(new UldCode("AKE20011R7"), new Weight(500, WeightUnit.KG), ATH));
    }
}
