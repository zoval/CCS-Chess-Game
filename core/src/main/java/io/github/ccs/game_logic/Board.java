package io.github.ccs.game_logic;

/** Coordinates board state, move validation, turns, and game status. */
public final class Board {
    public static final int EMPTY = Piece.EMPTY;
    public static final int PAWN = Piece.PAWN;
    public static final int KNIGHT = Piece.KNIGHT;
    public static final int BISHOP = Piece.BISHOP;
    public static final int ROOK = Piece.ROOK;
    public static final int QUEEN = Piece.QUEEN;
    public static final int KING = Piece.KING;

    private final Position position = new Position();
    private final MoveValidator moveValidator = new MoveValidator();
    private final GameStatus gameStatus = new GameStatus();
    private boolean whiteTurn = true;

    public int getPiece(int row, int column) {
        return position.getPiece(row, column);
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public boolean isGameOver() {
        return gameStatus.isGameOver();
    }

    public String getStatusText() {
        return gameStatus.getText();
    }

    /** Attempts a move for the current player and advances the turn on success. */
    public boolean move(int fromRow, int fromColumn, int toRow, int toColumn) {
        if (!moveValidator.isLegalMove(position, fromRow, fromColumn, toRow, toColumn, whiteTurn)) {
            return false;
        }

        int piece = position.getPiece(fromRow, fromColumn);
        position.setPiece(fromRow, fromColumn, Piece.EMPTY);
        position.setPiece(toRow, toColumn, piece);
        promotePawn(toRow, toColumn, piece);
        whiteTurn = !whiteTurn;
        gameStatus.update(position, moveValidator, whiteTurn);
        return true;
    }

    public void reset() {
        position.reset();
        whiteTurn = true;
        gameStatus.reset();
    }

    private void promotePawn(int row, int column, int piece) {
        if (Piece.typeOf(piece) == Piece.PAWN && (row == 0 || row == 7)) {
            position.setPiece(row, column, Piece.forColor(Piece.QUEEN, Piece.isWhite(piece)));
        }
    }
}
