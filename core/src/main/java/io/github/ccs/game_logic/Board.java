package io.github.ccs.game_logic;

/** Coordinates board state, move validation, turns, and game status. */
public final class Board {
    //initializes the piece constants from the Piece class for easy access
    public static final int EMPTY = Piece.EMPTY;
    public static final int PAWN = Piece.PAWN;
    public static final int KNIGHT = Piece.KNIGHT;
    public static final int BISHOP = Piece.BISHOP;
    public static final int ROOK = Piece.ROOK;
    public static final int QUEEN = Piece.QUEEN;
    public static final int KING = Piece.KING;

    //gets the position, validator and status classes
    private final Position position = new Position();
    private final MoveValidator moveValidator = new MoveValidator();
    private final SpecialMoves specialMoves = moveValidator.getSpecialMoves();
    private final FiftymoveRule fiftyMoveRule = new FiftymoveRule();
    private final GameStatus gameStatus = new GameStatus();
    private boolean whiteTurn = true;

    //getters from position to board class
    public int getPiece(int row, int column) {
        return position.getPiece(row, column);
    }

    //getters from gameStatus to board class
    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    //still a getter from gameStatus to board class
    public boolean isGameOver() {
        return gameStatus.isGameOver();
    }

    //same as above
    public String getStatusText() {
        return gameStatus.getText();
    }

    /** Attempts a move for the current player and advances the turn on success. */
    public boolean move(int fromRow, int fromColumn, int toRow, int toColumn) {
        if (!moveValidator.isLegalMove(position, fromRow, fromColumn, toRow, toColumn, whiteTurn)) {
            return false;
        }

        int piece = position.getPiece(fromRow, fromColumn);
        int target = position.getPiece(toRow, toColumn);

        boolean castling = specialMoves.isCastlingMove(position, fromRow, fromColumn,
            toRow, toColumn, whiteTurn, moveValidator);

        boolean enPassant = specialMoves.isEnPassantMove(position, fromRow, fromColumn,
            toRow, toColumn, whiteTurn);

        position.setPiece(fromRow, fromColumn, Piece.EMPTY);
        position.setPiece(toRow, toColumn, piece);
        
        if (enPassant) {
            position.setPiece(toRow + (whiteTurn ? -1 : 1), toColumn, Piece.EMPTY);
        }

        if (castling) {
            int rookFromColumn = toColumn == 6 ? 7 : 0;
            int rookToColumn = toColumn == 6 ? 5 : 3;
            position.setPiece(toRow, rookToColumn, position.getPiece(toRow, rookFromColumn));
            position.setPiece(toRow, rookFromColumn, Piece.EMPTY);
        }
        promotePawn(toRow, toColumn, piece);
        boolean pawnMoved = Piece.typeOf(piece) == Piece.PAWN;
        boolean captureMade = target != Piece.EMPTY || enPassant;
        fiftyMoveRule.recordMove(pawnMoved, captureMade);
        specialMoves.recordMove(fromRow, fromColumn, toRow, toColumn, piece);
        whiteTurn = !whiteTurn;
        gameStatus.update(position, moveValidator, whiteTurn, fiftyMoveRule.isDraw());
        return true;
    }

    public void reset() {
        position.reset();
        specialMoves.reset();
        fiftyMoveRule.reset();
        whiteTurn = true;
        gameStatus.reset();
    }

    private void promotePawn(int row, int column, int piece) {
        if (SpecialMoves.isPromotion(piece, row)) {
            position.setPiece(row, column, SpecialMoves.promotedPiece(piece, Piece.QUEEN));
        }
    }
}
