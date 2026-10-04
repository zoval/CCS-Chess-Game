package io.github.ccs.ai_networking;

import io.github.ccs.game_logic.Piece;

/**
 * A lightweight, self-contained chess position used by the AI search.
 * Squares hold signed piece IDs (see {@link io.github.ccs.game_logic.Piece});
 * zero means empty.
 */
public class AIPosition {

    private final int[][] board = new int[8][8];

    private int lastFromRow;
    private int lastFromColumn;
    private int lastToRow;
    private int lastToColumn;

    /**
     * Creates an empty position.
     */
    public AIPosition() {
    }

    /**
     * Creates a deep copy of another position, including its last-move record.
     */
    public AIPosition(AIPosition other) {
        for (int row = 0; row < board.length; row++) {
            System.arraycopy(other.board[row], 0, board[row], 0, board[row].length);
        }

        lastFromRow = other.lastFromRow;
        lastFromColumn = other.lastFromColumn;
        lastToRow = other.lastToRow;
        lastToColumn = other.lastToColumn;
    }

    /**
     * @return the signed piece ID at the given square, or {@code Piece.EMPTY}.
     */
    public int get(int row, int column) {
        return board[row][column];
    }

    /**
     * Places a signed piece ID on the given square.
     */
    public void set(int row, int column, int piece) {
        board[row][column] = piece;
    }

    /**
     * Moves the piece on the from-square to the to-square without any legality
     * checks, records the move for later inspection, and auto-queens a pawn
     * reaching the last rank so the search always sees promotions.
     */
    public void apply(Move move) {
        int piece = board[move.fromRow][move.fromColumn];

        board[move.fromRow][move.fromColumn] = 0;

        if (Piece.typeOf(piece) == Piece.PAWN && (move.toRow == 0 || move.toRow == 7)) {
            piece = Piece.forColor(Piece.QUEEN, Piece.isWhite(piece));
        }

        board[move.toRow][move.toColumn] = piece;

        lastFromRow = move.fromRow;
        lastFromColumn = move.fromColumn;
        lastToRow = move.toRow;
        lastToColumn = move.toColumn;
    }
}
