package io.github.ccs.qa;

import org.junit.Test;

import io.github.ccs.game_logic.Board;
import io.github.ccs.game_logic.Piece;

public class CapturedPiecesTest {

    @Test
    public void normalCaptureIsRecorded() {
        Board board = new Board();

        if (!board.move(1, 4, 3, 4)) { // e4
            throw new AssertionError("e4 should be legal");
        }
        if (!board.move(6, 3, 4, 3)) { // d5
            throw new AssertionError("d5 should be legal");
        }
        if (!board.move(3, 4, 4, 3)) { // exd5
            throw new AssertionError("exd5 should be legal");
        }

        if (board.getCapturedPieces(true).size() != 1
                || board.getCapturedPieces(true).get(0) != -Piece.PAWN) {
            throw new AssertionError("White should have captured a black pawn");
        }
        if (!board.getCapturedPieces(false).isEmpty()) {
            throw new AssertionError("Black should have captured nothing");
        }
        if (board.getMaterialBalance() != 1) {
            throw new AssertionError("White should be up one pawn after exd5");
        }
    }

    @Test
    public void enPassantCaptureRecordsThePawn() {
        Board board = new Board();

        if (!board.move(1, 4, 3, 4)) { // e4
            throw new AssertionError("e4 should be legal");
        }
        if (!board.move(6, 0, 5, 0)) { // a6
            throw new AssertionError("a6 should be legal");
        }
        if (!board.move(3, 4, 4, 4)) { // e5
            throw new AssertionError("e5 should be legal");
        }
        if (!board.move(6, 3, 4, 3)) { // d5
            throw new AssertionError("d5 should be legal");
        }
        if (!board.move(4, 4, 5, 3)) { // exd6 en passant
            throw new AssertionError("En passant should be legal");
        }

        if (board.getPiece(4, 3) != Piece.EMPTY) {
            throw new AssertionError("The d5 pawn should be removed by en passant");
        }
        if (board.getPiece(5, 3) != Piece.PAWN) {
            throw new AssertionError("The white pawn should sit on d6");
        }
        if (board.getCapturedPieces(true).size() != 1
                || board.getCapturedPieces(true).get(0) != -Piece.PAWN) {
            throw new AssertionError("En passant should record the captured pawn");
        }
        if (board.getMaterialBalance() != 1) {
            throw new AssertionError("White should be up one pawn after en passant");
        }
    }

    @Test
    public void blackCaptureIsRecordedSeparately() {
        Board board = new Board();

        if (!board.move(1, 4, 3, 4)) { // e4
            throw new AssertionError("e4 should be legal");
        }
        if (!board.move(6, 3, 4, 3)) { // d5
            throw new AssertionError("d5 should be legal");
        }
        if (!board.move(3, 4, 4, 3)) { // exd5
            throw new AssertionError("exd5 should be legal");
        }
        if (!board.move(7, 3, 4, 3)) { // Qxd5
            throw new AssertionError("Qxd5 should be legal");
        }

        if (board.getCapturedPieces(false).size() != 1
                || board.getCapturedPieces(false).get(0) != Piece.PAWN) {
            throw new AssertionError("Black should have captured a white pawn");
        }
        if (board.getMaterialBalance() != 0) {
            throw new AssertionError("Material should be balanced after the pawn trade");
        }
    }
}
