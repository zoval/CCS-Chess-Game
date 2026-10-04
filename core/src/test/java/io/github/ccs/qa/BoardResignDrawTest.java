package io.github.ccs.qa;

import org.junit.Test;

import io.github.ccs.game_logic.Board;

public class BoardResignDrawTest {

    @Test
    public void whiteResignsAndBlackWins() {
        Board board = new Board();

        if (!board.resign()) {
            throw new AssertionError("Resigning in a live game should succeed");
        }
        if (!board.isGameOver()) {
            throw new AssertionError("The game should be over after resignation");
        }
        if (!board.getStatusText().equals("Resignation - Black wins")) {
            throw new AssertionError("Unexpected resignation text: " + board.getStatusText());
        }
    }

    @Test
    public void blackResignsAndWhiteWins() {
        Board board = new Board();

        if (!board.move(1, 4, 3, 4)) {
            throw new AssertionError("e4 should be legal");
        }
        if (!board.resign()) {
            throw new AssertionError("Resigning in a live game should succeed");
        }
        if (!board.getStatusText().equals("Resignation - White wins")) {
            throw new AssertionError("Unexpected resignation text: " + board.getStatusText());
        }
    }

    @Test
    public void resignationIsIgnoredOnceGameOver() {
        Board board = new Board();

        board.resign();

        if (board.resign()) {
            throw new AssertionError("Resigning twice should be ignored");
        }
        if (board.agreeDraw()) {
            throw new AssertionError("Agreeing to a draw after game over should be ignored");
        }
    }

    @Test
    public void agreedDrawEndsTheGame() {
        Board board = new Board();

        if (!board.agreeDraw()) {
            throw new AssertionError("Agreeing to a draw in a live game should succeed");
        }
        if (!board.isGameOver()) {
            throw new AssertionError("The game should be over after an agreed draw");
        }
        if (!board.getStatusText().equals("Draw - agreement")) {
            throw new AssertionError("Unexpected draw text: " + board.getStatusText());
        }
    }

    @Test
    public void timeoutEndsTheGameForTheFlaggedSide() {
        Board whiteFlag = new Board();
        whiteFlag.endByTimeout(true);

        if (!whiteFlag.isGameOver()) {
            throw new AssertionError("Timeout should end the game");
        }
        if (!whiteFlag.getStatusText().equals("Timeout - Black wins")) {
            throw new AssertionError("Unexpected timeout text: " + whiteFlag.getStatusText());
        }

        Board blackFlag = new Board();
        blackFlag.endByTimeout(false);

        if (!blackFlag.getStatusText().equals("Timeout - White wins")) {
            throw new AssertionError("Unexpected timeout text: " + blackFlag.getStatusText());
        }
    }

    @Test
    public void movesAreRejectedAfterGameIsOver() {
        Board board = new Board();

        board.agreeDraw();

        if (board.move(1, 4, 3, 4)) {
            throw new AssertionError("No move should be accepted after game over");
        }
    }
}
