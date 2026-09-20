package io.github.ccs.qa;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.github.ccs.game_logic.FiftymoveRule;

public class FiftyMoveRuleTest {
    @Test
    public void fiftyMoveRuleTriggersAfterFiftyConsecutiveQuietMoves() {
        FiftymoveRule rule = new FiftymoveRule();

        for (int i = 0; i < 50; i++) {
            rule.recordMove(false, false);
        }

        assertTrue(rule.isDraw());
    }

    @Test
    public void pawnMoveResetsHalfmoveClock() {
        FiftymoveRule rule = new FiftymoveRule();

        for (int i = 0; i < 49; i++) {
            rule.recordMove(false, false);
        }

        rule.recordMove(true, false);

        assertFalse(rule.isDraw());
    }
}
