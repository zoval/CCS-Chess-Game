package io.github.ccs.game_logic;

import java.util.Arrays;

/** Stores and initializes the pieces on the 8x8 chess board. */
public final class Position {
    private final int[][] pieces = new int[8][8];

    public Position() {
        reset();
    }

    public int getPiece(int row, int column) {
        return pieces[row][column];
    }

    public void setPiece(int row, int column, int piece) {
        pieces[row][column] = piece;
    }

    public void reset() {
        for (int row = 0; row < 8; row++) Arrays.fill(pieces[row], Piece.EMPTY);
        for (int column = 0; column < 8; column++) {
            pieces[1][column] = Piece.PAWN;
            pieces[6][column] = -Piece.PAWN;
        }
        int[] backRank = {
            Piece.ROOK, Piece.KNIGHT, Piece.BISHOP, Piece.QUEEN,
            Piece.KING, Piece.BISHOP, Piece.KNIGHT, Piece.ROOK
        };
        for (int column = 0; column < 8; column++) {
            pieces[0][column] = backRank[column];
            pieces[7][column] = -backRank[column];
        }
    }

    public boolean contains(int row, int column) {
        return row >= 0 && row < 8 && column >= 0 && column < 8;
    }
}
