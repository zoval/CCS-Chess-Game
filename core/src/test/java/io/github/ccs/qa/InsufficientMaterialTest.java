package io.github.ccs.qa;

import org.junit.Test;

import io.github.ccs.game_logic.InsufficientMaterial;
import io.github.ccs.game_logic.Piece;
import io.github.ccs.game_logic.Position;

public class InsufficientMaterialTest {

    private static Position empty() {
        Position position = new Position();
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                position.setPiece(row, column, Piece.EMPTY);
            }
        }
        return position;
    }

    private static void placeKings(Position position) {
        position.setPiece(0, 0, Piece.forColor(Piece.KING, true));
        position.setPiece(7, 7, Piece.forColor(Piece.KING, false));
    }

    @Test
    public void kingVersusKingIsDraw() {
        Position position = empty();
        placeKings(position);

        if (!InsufficientMaterial.isInsufficient(position)) {
            throw new AssertionError("K vs K should be insufficient material");
        }
    }

    @Test
    public void singleMinorPieceCannotMate() {
        Position knight = empty();
        placeKings(knight);
        knight.setPiece(4, 4, Piece.forColor(Piece.KNIGHT, true));
        if (!InsufficientMaterial.isInsufficient(knight)) {
            throw new AssertionError("K+N vs K should be insufficient material");
        }

        Position bishop = empty();
        placeKings(bishop);
        bishop.setPiece(4, 4, Piece.forColor(Piece.BISHOP, false));
        if (!InsufficientMaterial.isInsufficient(bishop)) {
            throw new AssertionError("K+B vs K should be insufficient material");
        }
    }

    @Test
    public void sameColoredBishopsCannotMate() {
        Position position = empty();
        placeKings(position);
        position.setPiece(4, 4, Piece.forColor(Piece.BISHOP, true));  // light square
        position.setPiece(3, 3, Piece.forColor(Piece.BISHOP, false)); // light square

        if (!InsufficientMaterial.isInsufficient(position)) {
            throw new AssertionError("K+B vs K+B with same-colored bishops should be a draw");
        }
    }

    @Test
    public void oppositeColoredBishopsCanMate() {
        Position position = empty();
        placeKings(position);
        position.setPiece(4, 4, Piece.forColor(Piece.BISHOP, true));  // light square
        position.setPiece(3, 4, Piece.forColor(Piece.BISHOP, false)); // dark square

        if (InsufficientMaterial.isInsufficient(position)) {
            throw new AssertionError("Opposite-colored bishops should not be an automatic draw");
        }
    }

    @Test
    public void rookAndTwoKnightsCanMate() {
        Position rook = empty();
        placeKings(rook);
        rook.setPiece(4, 4, Piece.forColor(Piece.ROOK, true));
        if (InsufficientMaterial.isInsufficient(rook)) {
            throw new AssertionError("K+R vs K can mate");
        }

        Position knights = empty();
        placeKings(knights);
        knights.setPiece(4, 4, Piece.forColor(Piece.KNIGHT, true));
        knights.setPiece(4, 5, Piece.forColor(Piece.KNIGHT, true));
        if (InsufficientMaterial.isInsufficient(knights)) {
            throw new AssertionError("K+N+N vs K should not be an automatic draw");
        }
    }

    @Test
    public void startingPositionHasSufficientMaterial() {
        if (InsufficientMaterial.isInsufficient(new Position())) {
            throw new AssertionError("The starting position has plenty of material");
        }
    }
}
