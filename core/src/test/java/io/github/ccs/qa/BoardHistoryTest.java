package io.github.ccs.qa;

import org.junit.Test;

import io.github.ccs.game_logic.Board;
import io.github.ccs.game_logic.Piece;

public class BoardHistoryTest {

    private boolean move(Board board, int fromRow, int fromColumn, int toRow, int toColumn) {
        return board.move(fromRow, fromColumn, toRow, toColumn);
    }

    @Test
    public void historyRecordsSnapshotAfterEachMove() {
        Board board = new Board();

        if (board.canStepBack()) {
            throw new AssertionError("No step back should be available at the start");
        }

        move(board, 1, 4, 3, 4);
        move(board, 6, 4, 4, 4);
        move(board, 0, 6, 2, 5);

        if (!board.canStepBack()) {
            throw new AssertionError("Step back should be available after moves");
        }
        if (board.canStepForward()) {
            throw new AssertionError("Step forward should not be available at the live state");
        }
        if (board.isViewingHistory()) {
            throw new AssertionError("Live state should not count as viewing history");
        }
        if (board.getHistory().size() != 4) {
            throw new AssertionError("Expected initial snapshot plus three moves");
        }
    }

    @Test
    public void stepBackRestoresPositionAndTurn() {
        Board board = new Board();

        move(board, 1, 4, 3, 4);
        move(board, 6, 4, 4, 4);
        move(board, 0, 6, 2, 5);

        if (!board.stepBack()) {
            throw new AssertionError("Step back should succeed");
        }
        if (!board.isWhiteTurn()) {
            throw new AssertionError("After two moves it should be White's turn");
        }
        if (board.getPiece(2, 5) != Piece.EMPTY || board.getPiece(0, 6) != Piece.KNIGHT) {
            throw new AssertionError("Knight must return to g1 after stepping back");
        }
        if (board.getPiece(3, 4) != Piece.PAWN) {
            throw new AssertionError("e4 pawn should still be present");
        }

        if (!board.stepBack()) {
            throw new AssertionError("Second step back should succeed");
        }
        if (board.isWhiteTurn()) {
            throw new AssertionError("After one move it should be Black's turn");
        }

        if (!board.stepBack()) {
            throw new AssertionError("Third step back should succeed");
        }
        if (board.getPiece(3, 4) != Piece.EMPTY) {
            throw new AssertionError("Initial position should have an empty e4");
        }
        if (board.canStepBack()) {
            throw new AssertionError("Cannot step back past the initial position");
        }
    }

    @Test
    public void stepForwardReturnsToLiveState() {
        Board board = new Board();

        move(board, 1, 4, 3, 4);
        move(board, 6, 4, 4, 4);

        board.stepBack();
        board.stepBack();

        if (!board.stepForward()) {
            throw new AssertionError("Step forward should succeed");
        }
        if (board.getPiece(3, 4) != Piece.PAWN) {
            throw new AssertionError("e4 pawn should reappear stepping forward");
        }
        if (!board.stepForward()) {
            throw new AssertionError("Second step forward should succeed");
        }
        if (board.isViewingHistory()) {
            throw new AssertionError("Should be back at the live state");
        }
        if (board.canStepForward()) {
            throw new AssertionError("Step forward should be unavailable at the live state");
        }
    }

    @Test
    public void movingAfterSteppingBackDiscardsRedoStates() {
        Board board = new Board();

        move(board, 1, 4, 3, 4);
        move(board, 6, 4, 4, 4);

        board.stepBack();
        board.stepBack(); // back to the initial position, White to move

        if (!move(board, 0, 1, 2, 2)) {
            throw new AssertionError("Knight to c3 should be legal after stepping back");
        }
        if (board.canStepForward()) {
            throw new AssertionError("New move must discard redo states");
        }
        if (board.getHistory().size() != 2) {
            throw new AssertionError("History should hold the initial snapshot plus the new move");
        }
        if (board.isWhiteTurn()) {
            throw new AssertionError("It should be Black's turn after the new move");
        }
    }

    @Test
    public void lastMoveTracksCommittedMove() {
        Board board = new Board();

        if (board.getLastMove() != null) {
            throw new AssertionError("No last move before any move is made");
        }

        move(board, 1, 4, 3, 4);

        int[] last = board.getLastMove();
        if (last == null || last[0] != 1 || last[1] != 4 || last[2] != 3 || last[3] != 4) {
            throw new AssertionError("Last move should be e2e4");
        }
    }

    @Test
    public void castlingRightsRestoreWhenSteppingBack() {
        Board board = new Board();

        move(board, 1, 4, 3, 4);   // e4
        move(board, 6, 4, 4, 4);   // e5
        move(board, 0, 6, 2, 5);   // Nf3
        move(board, 7, 6, 5, 5);   // Nf6
        move(board, 0, 5, 3, 2);   // Bc4
        move(board, 6, 0, 5, 0);   // a6
        if (!move(board, 0, 4, 0, 6)) { // O-O
            throw new AssertionError("White should be able to castle kingside");
        }

        if (board.getPiece(0, 6) != Piece.KING || board.getPiece(0, 5) != Piece.ROOK) {
            throw new AssertionError("King and rook should have moved by castling");
        }

        if (!board.stepBack()) {
            throw new AssertionError("Step back across the castle should succeed");
        }

        if (board.getPiece(0, 4) != Piece.KING || board.getPiece(0, 7) != Piece.ROOK) {
            throw new AssertionError("Castling must fully undo on step back");
        }
        if (!move(board, 0, 4, 0, 6)) {
            throw new AssertionError("Castling rights should be restored after stepping back");
        }
    }
}
