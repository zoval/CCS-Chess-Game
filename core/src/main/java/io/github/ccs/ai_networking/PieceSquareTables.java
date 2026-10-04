package io.github.ccs.ai_networking;

import io.github.ccs.game_logic.Piece;

/**
 * Piece-square tables used by {@link HardAI}'s evaluation, on top of the raw
 * material values from {@link AIUtils#value(int)}. Tables are written from
 * White's point of view with row 0 = White's back rank and column 0 = file a;
 * Black reads the same table with the row mirrored. {@link AIUtils#evaluate}
 * stays untouched because AITest pins its symmetry.
 */
public final class PieceSquareTables {

    private static final int[] PAWN = {
        0, 0, 0, 0, 0, 0, 0, 0,
        5, 10, 10, -20, -20, 10, 10, 5,
        5, -5, -10, 0, 0, -10, -5, 5,
        0, 0, 0, 20, 20, 0, 0, 0,
        5, 5, 10, 25, 25, 10, 5, 5,
        10, 10, 20, 30, 30, 20, 10, 10,
        50, 50, 50, 50, 50, 50, 50, 50,
        0, 0, 0, 0, 0, 0, 0, 0
    };

    private static final int[] KNIGHT = {
        -50, -40, -30, -30, -30, -30, -40, -50,
        -40, -20, 0, 0, 0, 0, -20, -40,
        -30, 0, 10, 15, 15, 10, 0, -30,
        -30, 5, 15, 20, 20, 15, 5, -30,
        -30, 0, 15, 20, 20, 15, 0, -30,
        -30, 5, 10, 15, 15, 10, 5, -30,
        -40, -20, 0, 5, 5, 0, -20, -40,
        -50, -40, -30, -30, -30, -30, -40, -50
    };

    private static final int[] BISHOP = {
        -20, -10, -10, -10, -10, -10, -10, -20,
        -10, 0, 0, 0, 0, 0, 0, -10,
        -10, 0, 5, 10, 10, 5, 0, -10,
        -10, 5, 5, 10, 10, 5, 5, -10,
        -10, 0, 10, 10, 10, 10, 0, -10,
        -10, 10, 10, 10, 10, 10, 10, -10,
        -10, 5, 0, 0, 0, 0, 5, -10,
        -20, -10, -10, -10, -10, -10, -10, -20
    };

    private static final int[] ROOK = {
        0, 0, 0, 5, 5, 0, 0, 0,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        5, 10, 10, 10, 10, 10, 10, 5,
        0, 0, 0, 0, 0, 0, 0, 0
    };

    private static final int[] QUEEN = {
        -20, -10, -10, -5, -5, -10, -10, -20,
        -10, 0, 0, 0, 0, 0, 0, -10,
        -10, 0, 5, 5, 5, 5, 0, -10,
        -5, 0, 5, 5, 5, 5, 0, -5,
        0, 0, 5, 5, 5, 5, 0, -5,
        -10, 5, 5, 5, 5, 5, 0, -10,
        -10, 0, 5, 0, 0, 0, 0, -10,
        -20, -10, -10, -5, -5, -10, -10, -20
    };

    private static final int[] KING = {
        20, 30, 10, 0, 0, 10, 30, 20,
        20, 20, 0, 0, 0, 0, 20, 20,
        -10, -20, -20, -20, -20, -20, -20, -10,
        -20, -30, -30, -40, -40, -30, -30, -20,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30
    };

    private static final int[][] TABLES = {null, PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING};

    private PieceSquareTables() {
    }

    /**
     * @return the positional bonus of a piece on a square, always from the
     * piece's own color's point of view.
     */
    public static int bonus(int piece, int row, int column) {
        int[] table = TABLES[Piece.typeOf(piece)];
        if (table == null) {
            return 0;
        }

        int tableRow = Piece.isWhite(piece) ? row : 7 - row;
        return table[tableRow * 8 + column];
    }

    /**
     * Material plus positional evaluation from the AI's point of view.
     *
     * @param aiWhite the color the AI plays.
     * @return positive score favors the AI.
     */
    public static int evaluate(AIPosition position, boolean aiWhite) {
        int score = 0;

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                int piece = position.get(row, column);

                if (piece == Piece.EMPTY) {
                    continue;
                }

                int value = AIUtils.value(Piece.typeOf(piece)) + bonus(piece, row, column);

                if (Piece.isWhite(piece) == aiWhite) {
                    score += value;
                } else {
                    score -= value;
                }
            }
        }

        return score;
    }
}
