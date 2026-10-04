package io.github.ccs.game_logic;

/**
 * Insufficient-material draw rule: king vs king, king + single minor vs
 * king, and bishop vs bishop with both bishops on same-colored squares.
 * (King + two knights vs king is deliberately not treated as a draw.)
 */
public final class InsufficientMaterial {

    private InsufficientMaterial() {
    }

    /** @return true if neither side has enough material to force checkmate. */
    public static boolean isInsufficient(Position position) {
        int minorCount = 0;
        int bishopCount = 0;
        int firstBishopColor = -1;
        int secondBishopColor = -1;
        int bishopSides = 0;

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                int piece = position.getPiece(row, column);
                int type = Piece.typeOf(piece);

                if (piece == Piece.EMPTY || type == Piece.KING) {
                    continue;
                }

                if (type != Piece.BISHOP && type != Piece.KNIGHT) {
                    return false;
                }

                minorCount++;

                if (type == Piece.BISHOP) {
                    int squareColor = (row + column) % 2;
                    if (bishopCount == 0) {
                        firstBishopColor = squareColor;
                    } else if (bishopCount == 1) {
                        secondBishopColor = squareColor;
                    }
                    bishopCount++;
                    bishopSides += Piece.isWhite(piece) ? 1 : -1;
                }
            }
        }

        if (minorCount == 0) {
            return true;
        }

        if (minorCount == 1) {
            return true;
        }

        // Only two same-colored bishops (one per side) can never mate.
        return minorCount == 2 && bishopCount == 2
                && firstBishopColor == secondBishopColor && bishopSides == 0;
    }
}
