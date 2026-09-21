package io.github.ccs.qa;

import org.junit.Test;

import io.github.ccs.game_logic.FiftymoveRule;

public class FiftyMoveRuleTest {
    @Test
    public void fiftyMoveRuleTriggersAfterFiftyConsecutiveQuietMoves() {
        FiftymoveRule rule = new FiftymoveRule();

        for (int i = 0; i < 50; i++) {
            rule.recordMove(false, false);
        }

        if (!rule.isDraw()) {
            throw new AssertionError("Expected the fifty-move rule to trigger");
        }
    }

    @Test
    public void pawnMoveResetsHalfmoveClock() {
        FiftymoveRule rule = new FiftymoveRule();

        for (int i = 0; i < 49; i++) {
            rule.recordMove(false, false);
        }

        rule.recordMove(true, false);

        if (rule.isDraw()) {
            throw new AssertionError("Expected the pawn move to reset the halfmove clock");
        }
    }
}
