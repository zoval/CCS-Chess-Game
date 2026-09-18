package io.github.ccs.game_logic;

/** Piece type constants and color helpers used by the chess model. */
public final class Piece {
    public static final int EMPTY = 0;
    public static final int PAWN = 1;
    public static final int KNIGHT = 2;
    public static final int BISHOP = 3;
    public static final int ROOK = 4;
    public static final int QUEEN = 5;
    public static final int KING = 6;

    private Piece() {
    }

    public static boolean isWhite(int piece) {
        return piece > EMPTY;
    }

    public static int typeOf(int piece) {
        return Math.abs(piece);
    }

    public static int forColor(int type, boolean white) {
        return white ? type : -type;
    }
}
