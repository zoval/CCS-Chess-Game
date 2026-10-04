package io.github.ccs.qa;

import org.junit.Test;

import io.github.ccs.game_logic.GameClock;

public class GameClockTest {

    @Test
    public void updateReducesOnlyTheActiveSide() {
        GameClock clock = new GameClock(600000);

        clock.update(1.5f, true);
        clock.update(0.5f, false);

        if (clock.getWhiteMillis() != 598500) {
            throw new AssertionError("White should have lost 1.5 seconds");
        }
        if (clock.getBlackMillis() != 599500) {
            throw new AssertionError("Black should have lost 0.5 seconds");
        }
    }

    @Test
    public void clockNeverGoesNegative() {
        GameClock clock = new GameClock(500);

        clock.update(2f, true);

        if (clock.getWhiteMillis() != 0) {
            throw new AssertionError("White's clock should clamp at zero");
        }
        if (!clock.isWhiteFlagged()) {
            throw new AssertionError("White should have flagged");
        }
        if (clock.isBlackFlagged()) {
            throw new AssertionError("Black should not have flagged");
        }
    }

    @Test
    public void resetRestoresBothSides() {
        GameClock clock = new GameClock(600000);

        clock.update(10f, true);
        clock.reset(300000);

        if (clock.getWhiteMillis() != 300000 || clock.getBlackMillis() != 300000) {
            throw new AssertionError("Reset should restore both clocks");
        }
    }

    @Test
    public void formatsMinutesAndSeconds() {
        if (!GameClock.format(600000).equals("10:00")) {
            throw new AssertionError("600000ms should format as 10:00");
        }
        if (!GameClock.format(59000).equals("0:59")) {
            throw new AssertionError("59000ms should format as 0:59");
        }
        if (!GameClock.format(0).equals("0:00")) {
            throw new AssertionError("0ms should format as 0:00");
        }
    }
}
