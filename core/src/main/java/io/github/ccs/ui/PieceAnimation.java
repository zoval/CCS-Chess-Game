package io.github.ccs.ui;

import io.github.ccs.game_logic.Piece;

/** Tracks and updates animated piece movement across the board. */
public class PieceAnimation {
    private int piece = Piece.EMPTY;
    private int fromRow = -1;
    private int fromColumn = -1;
    private int toRow = -1;
    private int toColumn = -1;
    private float progress = 1f;

    public void start(int piece, int fromRow, int fromColumn, int toRow, int toColumn) {
        this.piece = piece;
        this.fromRow = fromRow;
        this.fromColumn = fromColumn;
        this.toRow = toRow;
        this.toColumn = toColumn;
        this.progress = 0f;
    }

    public void update(float delta) {
        if (!isAnimating()) return;
        progress = Math.min(1f, progress + delta * 5f);
        if (progress >= 1f) {
            piece = Piece.EMPTY;
        }
    }

    public boolean isAnimating() {
        return piece != Piece.EMPTY;
    }

    public int getPiece() {
        return piece;
    }

    public int getFromRow() {
        return fromRow;
    }

    public int getFromColumn() {
        return fromColumn;
    }

    public int getToRow() {
        return toRow;
    }

    public int getToColumn() {
        return toColumn;
    }

    public float getProgress() {
        return progress;
    }
}
