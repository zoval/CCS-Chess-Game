package io.github.ccs.game_logic;

/** 
 * Piece type constants and utility methods for piece manipulation.
 * Positive values represent White pieces, negative values represent Black pieces. 
 */
public final class Piece {
    public static final int EMPTY = 0;
    public static final int PAWN = 1;
    public static final int KNIGHT = 2;
    public static final int BISHOP = 3;
    public static final int ROOK = 4;
    public static final int QUEEN = 5;
    public static final int KING = 6;

    private Piece() {
        // Prevent instantiation
    }

    /** @return true if the piece ID represents a White piece. */
    public static boolean isWhite(int piece) {
        return piece > EMPTY;
    }

    /** @return The absolute type value (removes color information). */
    public static int typeOf(int piece) {
        return Math.abs(piece);
    }

    /** 
     * Constructs a piece ID based on type and color.                
     * @param white true for white piece, false for black.
     * @return signed piece ID.                
     */
    public static int forColor(int type, boolean white) {
        return white ? type : -type;
    }
}
