package io.github.ccs.qa;

import org.junit.Test;

import io.github.ccs.game_logic.Board;

public class BoardRepetitionHistoryTest {

    @Test
    public void viewingHistoryDoesNotCorruptThreefoldDetection() {
        Board board = new Board();
        playKnightCycle(board);

        if (!board.stepBack() || !board.stepForward()) {
            throw new AssertionError("Stepping back and forward must succeed");
        }

        playKnightCycle(board);

        if (!board.isGameOver()) {
            throw new AssertionError("Threefold repetition must be detected after reviewing history");
        }
        if (!board.getStatusText().contains("threefold")) {
            throw new AssertionError("Expected a threefold-repetition draw, got: " + board.getStatusText());
        }
    }

    @Test
    public void replayingFromARewoundStateDoesNotReuseAbandonedKeys() {
        Board board = new Board();
        playKnightCycle(board);

        if (!board.stepBack() || !board.stepBack()) {
            throw new AssertionError("Stepping back twice must succeed");
        }

        if (!board.move(2, 5, 0, 6)) {
            throw new AssertionError("Ng1 should be legal from the rewound position");
        }
        if (!board.move(5, 5, 7, 6)) {
            throw new AssertionError("Ng8 should be legal after Ng1");
        }

        if (board.isGameOver()) {
            throw new AssertionError("Replayed moves must not trigger a false threefold draw");
        }
        if (board.canStepForward()) {
            throw new AssertionError("New moves must discard redo states");
        }
    }

    /** Nf3 Nf6 Ng1 Ng8: returns to the initial position with White to move. */
    private void playKnightCycle(Board board) {
        if (!board.move(0, 6, 2, 5)) throw new AssertionError("Nf3 rejected");
        if (!board.move(7, 6, 5, 5)) throw new AssertionError("Nf6 rejected");
        if (!board.move(2, 5, 0, 6)) throw new AssertionError("Ng1 rejected");
        if (!board.move(5, 5, 7, 6)) throw new AssertionError("Ng8 rejected");
    }
}
