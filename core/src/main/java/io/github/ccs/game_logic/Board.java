package io.github.ccs.game_logic;

import java.util.ArrayList;
import java.util.List;

/** Coordinates board state, move validation, turns, and game status. */
public final class Board {
    /** Piece type definitions mapped from {@link Piece}. */
    public static final int EMPTY = Piece.EMPTY;
    public static final int PAWN = Piece.PAWN;
    public static final int KNIGHT = Piece.KNIGHT;
    public static final int BISHOP = Piece.BISHOP;
    public static final int ROOK = Piece.ROOK;
    public static final int QUEEN = Piece.QUEEN;
    public static final int KING = Piece.KING;

    /** Internal game state logic managers. */
    private final Position position = new Position();
    private final MoveValidator moveValidator = new MoveValidator();
    private final SpecialMoves specialMoves = moveValidator.getSpecialMoves();
    private final FiftymoveRule fiftyMoveRule = new FiftymoveRule();
    private final GameStatus gameStatus = new GameStatus();
    private boolean whiteTurn = true;

    /** @return piece ID at the specified coordinates. */
    public int getPiece(int row, int column) {
        return position.getPiece(row, column);
    }

    /** @return true if it is currently White's turn, false for Black. */
    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    /** @return true if checkmate or stalemate has been reached. */
    public boolean isGameOver() {
        return gameStatus.isGameOver();
    }

    /** @return A human-readable string indicating the game's current status (e.g., Turn info, Check, Mate). */
    public String getStatusText() {
        return gameStatus.getText();
    }

    /** Attempts a move for the current player and advances the turn on success. */
    public boolean move(int fromRow, int fromColumn, int toRow, int toColumn) {
        if (gameStatus.isGameOver()) return false;
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

    /**
     * Checks whether moving the piece at the source square to the destination square is legal
     * for the side to move, without applying the move.
     */
    public boolean isLegalMove(int fromRow, int fromColumn, int toRow, int toColumn) {
        return moveValidator.isLegalMove(position, fromRow, fromColumn, toRow, toColumn, whiteTurn);
    }

    /** @return all legal destination squares for the piece at the given square (empty if none). */
    public List<int[]> getLegalMoves(int fromRow, int fromColumn) {
        List<int[]> moves = new ArrayList<>();
        if (position.getPiece(fromRow, fromColumn) == Piece.EMPTY) {
            return moves;
        }
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                if (isLegalMove(fromRow, fromColumn, row, column)) {
                    moves.add(new int[]{row, column});
                }
            }
        }
        return moves;
    }

    /** @return true if the king of the given color is currently attacked. */
    public boolean isKingInCheck(boolean white) {
        return moveValidator.isKingAttacked(position, white);
    }

    /** @return true if the side to move's king is currently attacked. */
    public boolean isCurrentKingInCheck() {
        return moveValidator.isKingAttacked(position, whiteTurn);
    }

    /** @return the {@code [row, column]} of the given color's king, or null if not found. */
    public int[] getKingPosition(boolean white) {
        int king = Piece.forColor(Piece.KING, white);
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                if (position.getPiece(row, column) == king) {
                    return new int[]{row, column};
                }
            }
        }
        return null;
    }

    private void promotePawn(int row, int column, int piece) {
        if (SpecialMoves.isPromotion(piece, row)) {
            position.setPiece(row, column, SpecialMoves.promotedPiece(piece, Piece.QUEEN));
        }
    }
}
