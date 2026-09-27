package io.github.ccs.ai_networking;

import java.util.Objects;

/**
 * A single chess move on the AI board, expressed in zero-based row/column
 * coordinates. Row 0 is White's back rank and row 7 is Black's back rank;
 * column 0 is file a and column 7 is file h.
 */
public class Move {

    public final int fromRow;
    public final int fromColumn;
    public final int toRow;
    public final int toColumn;

    private final boolean capture;

    /**
     * Creates a non-capturing move.
     */
    public Move(int fromRow, int fromColumn, int toRow, int toColumn) {
        this(fromRow, fromColumn, toRow, toColumn, false);
    }

    /**
     * Creates a move.
     *
     * @param capture true if the destination square holds an enemy piece.
     */
    public Move(int fromRow, int fromColumn, int toRow, int toColumn, boolean capture) {
        this.fromRow = fromRow;
        this.fromColumn = fromColumn;
        this.toRow = toRow;
        this.toColumn = toColumn;
        this.capture = capture;
    }

    /**
     * @return true if this move captures an enemy piece.
     */
    public boolean isCapture() {
        return capture;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Move)) {
            return false;
        }
        Move that = (Move) other;
        return fromRow == that.fromRow
                && fromColumn == that.fromColumn
                && toRow == that.toRow
                && toColumn == that.toColumn
                && capture == that.capture;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromRow, fromColumn, toRow, toColumn, capture);
    }

    @Override
    public String toString() {
        return "Move{(" + fromRow + "," + fromColumn + ")->("
                + toRow + "," + toColumn + "), capture=" + capture + "}";
    }
}
