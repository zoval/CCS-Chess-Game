package io.github.ccs.qa;

import org.junit.Test;

import io.github.ccs.game_logic.FiftymoveRule;

public class FiftyMoveRuleTest {
    @Test
    public void fiftyMoveRuleTriggersAfterFiftyFullMoves() {
        FiftymoveRule rule = new FiftymoveRule();

        for (int i = 0; i < 100; i++) {
            rule.recordMove(false, false);
        }

        if (!rule.isDraw()) {
            throw new AssertionError("Expected the fifty-move rule to trigger after 100 halfmoves");
        }
    }

    @Test
    public void fiftyMoveRuleDoesNotTriggerBeforeFiftyFullMoves() {
        FiftymoveRule rule = new FiftymoveRule();

        for (int i = 0; i < 99; i++) {
            rule.recordMove(false, false);
        }

        if (rule.isDraw()) {
            throw new AssertionError("Expected no draw after 99 halfmoves");
        }
    }

    @Test
    public void pawnMoveResetsHalfmoveClock() {
        FiftymoveRule rule = new FiftymoveRule();

        for (int i = 0; i < 99; i++) {
            rule.recordMove(false, false);
        }

        rule.recordMove(true, false);

        if (rule.isDraw()) {
            throw new AssertionError("Expected the pawn move to reset the halfmove clock");
        }
    }

    @Test
    public void captureResetsHalfmoveClock() {
        FiftymoveRule rule = new FiftymoveRule();

        for (int i = 0; i < 99; i++) {
            rule.recordMove(false, false);
        }

        rule.recordMove(false, true);

        if (rule.isDraw()) {
            throw new AssertionError("Expected the capture to reset the halfmove clock");
        }
    }
}
