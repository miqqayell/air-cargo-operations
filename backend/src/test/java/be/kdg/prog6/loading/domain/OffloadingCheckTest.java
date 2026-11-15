package be.kdg.prog6.loading.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static be.kdg.prog6.loading.domain.LoadChecksFixture.ATH;
import static be.kdg.prog6.loading.domain.LoadChecksFixture.DECK;
import static be.kdg.prog6.loading.domain.LoadChecksFixture.FRA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OffloadingCheckTest {

    private final OffloadingCheck hardCheck = new OffloadingCheck(CheckKind.HARD);

    private CheckResult check(LoadChecksFixture fixture) {
        return hardCheck.check(fixture.load(), DECK);
    }

    @Test
    void scenario1BlockingCargoOnTheOwnSideAndOnTheDoorPosition() {
        var fixture = new LoadChecksFixture().on("MR", ATH).on("ML", ATH).on("PL", FRA).on("RL", FRA);

        CheckResult result = check(fixture);

        assertFalse(result.passed());
        assertEquals(List.of(
                new BlockedUld(fixture.at("ML"), List.of(fixture.at("PL"), fixture.at("RL"))),
                new BlockedUld(fixture.at("MR"), List.of(fixture.at("RL")))), result.blockedUlds());
    }

    @Test
    void scenario2CargoBehindTheDoorMovesTowardsTheNoseAlongItsOwnSide() {
        var fixture = new LoadChecksFixture().on("RR", ATH).on("UR", ATH).on("UL", FRA);

        assertTrue(check(fixture).passed());
    }

    @Test
    void scenario3NoseCargoNeedsBothClAndCrClear() {
        var fixture = new LoadChecksFixture().on("B1", ATH).on("CR", FRA);

        CheckResult result = check(fixture);

        assertFalse(result.passed());
        assertEquals(List.of(new BlockedUld(fixture.at("B1"), List.of(fixture.at("CR")))), result.blockedUlds());
    }

    @Test
    void scenario4NoseCargoChoosesTheClearSide() {
        var fixture = new LoadChecksFixture().on("B1", ATH).on("DL", FRA);

        assertTrue(check(fixture).passed());
    }

    @Test
    void scenario5TailCargoNeedsBothUlAndUrClear() {
        var fixture = new LoadChecksFixture().on("X", ATH).on("UL", FRA);

        CheckResult result = check(fixture);

        assertFalse(result.passed());
        assertEquals(List.of(new BlockedUld(fixture.at("X"), List.of(fixture.at("UL")))), result.blockedUlds());
    }

    @Test
    void scenario6TailCargoCanChooseTheLeftSide() {
        var fixture = new LoadChecksFixture().on("X", ATH).on("TR", FRA);

        assertTrue(check(fixture).passed());
    }

    @Test
    void cargoForOtherDestinationsDoesNotHaveToLeave() {
        var fixture = new LoadChecksFixture().on("CL", FRA).on("DL", FRA).on("RL", ATH);

        assertTrue(check(fixture).passed());
    }

    @Test
    void reasonNamesTheBlockedUldAndWhatBlocksIt() {
        var fixture = new LoadChecksFixture().on("B1", ATH).on("CR", FRA);

        assertEquals("Cargo for ATH cannot leave the aircraft: " + fixture.at("B1").value() + " on B1 is blocked by "
                + fixture.at("CR").value() + " on CR. Move the blocking ULDs off the way to the door at row R, "
                + "or move the ATH cargo closer to the door.", check(fixture).reason());
    }

    @Test
    void offloadingCanBeConfiguredAsASoftCheck() {
        var fixture = new LoadChecksFixture().on("B1", ATH).on("CR", FRA);

        CheckResult result = new OffloadingCheck(CheckKind.SOFT).check(fixture.load(), DECK);

        assertFalse(result.passed());
        assertEquals(CheckKind.SOFT, result.kind());
    }
}
