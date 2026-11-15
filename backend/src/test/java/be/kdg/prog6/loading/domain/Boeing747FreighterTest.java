package be.kdg.prog6.loading.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Boeing747FreighterTest {

    private final FloorPlan deck = new Boeing747Freighter();

    private static List<Position> positions(String codes) {
        return Arrays.stream(codes.split(" ")).map(Position::new).toList();
    }

    @Test
    void mainDeckHasThreeNosePositionsFifteenRowsOfTwoAndOneTailPosition() {
        assertEquals(34, deck.positions().size());
        assertTrue(deck.contains(new Position("B1")));
        assertTrue(deck.contains(new Position("UR")));
        assertFalse(deck.contains(new Position("NL")));
        assertFalse(deck.contains(new Position("Y1")));
    }

    @Test
    void sidesFollowTheLetterOfThePosition() {
        assertEquals(DeckSide.LEFT, deck.sideOf(new Position("CL")));
        assertEquals(DeckSide.RIGHT, deck.sideOf(new Position("RR")));
        assertEquals(DeckSide.CENTRE, deck.sideOf(new Position("A2")));
        assertEquals(DeckSide.CENTRE, deck.sideOf(new Position("X")));
    }

    @Test
    void theDoorIsNextToRl() {
        assertEquals(new Position("RL"), deck.door());
    }

    @Test
    void leftPositionsFollowTheirOwnSideToTheDoor() {
        assertEquals(List.of(positions("PL RL")), deck.routesToDoor(new Position("ML")));
    }

    @Test
    void rightPositionsCrossFromRrToRl() {
        assertEquals(List.of(positions("PR RR RL")), deck.routesToDoor(new Position("MR")));
    }

    @Test
    void cargoBehindTheDoorMovesTowardsTheNose() {
        assertEquals(List.of(positions("TR SR RR RL")), deck.routesToDoor(new Position("UR")));
    }

    @Test
    void cargoAtTheDoorNeedsNoRoute() {
        assertEquals(List.of(List.of()), deck.routesToDoor(new Position("RL")));
    }

    @Test
    void nosePositionsManoeuvreOnCrAndClThenChooseASide() {
        assertEquals(List.of(
                positions("CL CR DL EL FL GL HL JL KL LL ML PL RL"),
                positions("CL CR DR ER FR GR HR JR KR LR MR PR RR RL")),
                deck.routesToDoor(new Position("B1")));
    }

    @Test
    void theTailManoeuvresOnUlAndUrThenChoosesASide() {
        assertEquals(List.of(positions("UL UR TL SL RL"), positions("UL UR TR SR RR RL")),
                deck.routesToDoor(new Position("X")));
    }

    @Test
    void unknownPositionHasNoSide() {
        assertThrows(IllegalArgumentException.class, () -> deck.sideOf(new Position("ZZ")));
    }
}
