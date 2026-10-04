package io.github.ccs.qa;

import org.junit.Test;

import io.github.ccs.game_logic.Board;
import io.github.ccs.game_logic.RepetitionRule;

public class RepetitionRuleTest {

    @Test
    public void knightShuffleTriggersThreefoldRepetition() {
        Board board = new Board();

        // Ng1-f3 Ng8-f6 Ng1... : the initial position recurs.
        int[][] shuffle = {
            {0, 6, 2, 5}, {7, 6, 5, 5}, // Nf3, Nf6
            {2, 5, 0, 6}, {5, 5, 7, 6}, // Ng1, Ng8 (initial position 2nd time)
            {0, 6, 2, 5}, {7, 6, 5, 5}, // Nf3, Nf6
            {2, 5, 0, 6}, {5, 5, 7, 6}, // Ng1, Ng8 (initial position 3rd time)
        };

        for (int i = 0; i < shuffle.length; i++) {
            int[] m = shuffle[i];
            if (!board.move(m[0], m[1], m[2], m[3])) {
                throw new AssertionError("Shuffle move " + i + " should be legal");
            }

            boolean expectGameOver = i == shuffle.length - 1;
            if (board.isGameOver() != expectGameOver) {
                throw new AssertionError("Threefold repetition should trigger exactly on the last move");
            }
        }

        if (!board.getStatusText().equals("Draw - threefold repetition")) {
            throw new AssertionError("Expected a threefold repetition draw, got: "
                    + board.getStatusText());
        }
    }

    @Test
    public void twiceIsNotThreefold() {
        Board board = new Board();

        int[][] twice = {
            {0, 6, 2, 5}, {7, 6, 5, 5},
            {2, 5, 0, 6}, {5, 5, 7, 6},
        };

        for (int[] m : twice) {
            if (!board.move(m[0], m[1], m[2], m[3])) {
                throw new AssertionError("Shuffle move should be legal");
            }
        }

        if (board.isGameOver()) {
            throw new AssertionError("Two occurrences must not end the game");
        }
    }

    @Test
    public void directRuleCountsPositions() {
        RepetitionRule rule = new RepetitionRule();
        io.github.ccs.game_logic.Position position = new io.github.ccs.game_logic.Position();
        io.github.ccs.game_logic.SpecialMoves special = new io.github.ccs.game_logic.SpecialMoves();

        rule.record(position, true, special);
        rule.record(position, true, special);

        if (rule.isThreefoldRepetition()) {
            throw new AssertionError("Two records are not a threefold repetition");
        }

        rule.record(position, true, special);

        if (!rule.isThreefoldRepetition()) {
            throw new AssertionError("Three records should be a threefold repetition");
        }

        rule.truncate(2);

        if (rule.isThreefoldRepetition()) {
            throw new AssertionError("Truncated history should not be a repetition");
        }
        if (rule.size() != 2) {
            throw new AssertionError("Truncate should shrink the history");
        }
    }
}
