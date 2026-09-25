package io.github.ccs.qa;

import org.junit.Test;

import io.github.ccs.game_logic.Board;

public class BoardGameOverTest {
    @Test
    public void movesAreRejectedAfterCheckmate() {
        Board board = new Board();
        playFoolsMate(board);

        if (!board.isGameOver()) {
            throw new AssertionError("Expected Fool's Mate to end the game");
        }
        if (!"Checkmate - Black wins".equals(board.getStatusText())) {
            throw new AssertionError("Expected 'Checkmate - Black wins' but got: " + board.getStatusText());
        }
        if (board.move(1, 1, 2, 1)) {
            throw new AssertionError("Expected moves to be rejected after game over");
        }
    }

    @Test
    public void resetAllowsPlayAgain() {
        Board board = new Board();
        playFoolsMate(board);

        board.reset();

        if (board.isGameOver()) {
            throw new AssertionError("Expected reset to clear game over state");
        }
        if (!board.move(1, 5, 2, 5)) {
            throw new AssertionError("Expected a legal move after reset");
        }
    }

    private void playFoolsMate(Board board) {
        if (!board.move(1, 5, 2, 5)) throw new AssertionError("f2-f3 rejected"); // 1. f3
        if (!board.move(6, 4, 4, 4)) throw new AssertionError("e7-e5 rejected"); // 1... e5
        if (!board.move(1, 6, 3, 6)) throw new AssertionError("g2-g4 rejected"); // 2. g4
        if (!board.move(7, 3, 3, 7)) throw new AssertionError("Qd8-h4 rejected"); // 2... Qh4#
    }
}
