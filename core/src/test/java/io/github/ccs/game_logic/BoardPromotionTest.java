package io.github.ccs.game_logic;

import org.junit.Test;

public class BoardPromotionTest {

    private static Position kingsAndPawns() {
        Position position = new Position();
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                position.setPiece(row, column, Piece.EMPTY);
            }
        }
        position.setPiece(0, 0, Piece.forColor(Piece.KING, true));
        position.setPiece(7, 7, Piece.forColor(Piece.KING, false));
        position.setPiece(6, 4, Piece.forColor(Piece.PAWN, true));
        position.setPiece(6, 5, Piece.forColor(Piece.PAWN, true));
        return position;
    }

    @Test
    public void promotionMoveIsDetected() {
        Board board = new Board(kingsAndPawns());

        if (!board.isPromotionMove(6, 4, 7, 4)) {
            throw new AssertionError("Pawn moving to the last rank should be a promotion");
        }
        if (board.isPromotionMove(6, 4, 6, 5)) {
            throw new AssertionError("Sideways destination is not a promotion");
        }
    }

    @Test
    public void pawnPromotesToChosenType() {
        Board board = new Board(kingsAndPawns());

        if (!board.move(6, 4, 7, 4, Piece.ROOK)) {
            throw new AssertionError("Promotion move should be legal");
        }
        if (board.getPiece(7, 4) != Piece.forColor(Piece.ROOK, true)) {
            throw new AssertionError("Pawn should have promoted to a rook");
        }
        if (board.isWhiteTurn()) {
            throw new AssertionError("Black should move after White's promotion");
        }
        if (board.getMaterialBalance() != 6) {
            throw new AssertionError("White should be up rook (5) plus pawn (1) after promoting");
        }
    }

    @Test
    public void defaultMovePromotesToQueen() {
        Board board = new Board(kingsAndPawns());

        if (!board.move(6, 5, 7, 5)) {
            throw new AssertionError("Promotion move should be legal");
        }
        if (board.getPiece(7, 5) != Piece.forColor(Piece.QUEEN, true)) {
            throw new AssertionError("Default promotion should yield a queen");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidPromotionTypeIsRejected() {
        Board board = new Board(kingsAndPawns());
        board.move(6, 4, 7, 4, Piece.PAWN);
    }
}
