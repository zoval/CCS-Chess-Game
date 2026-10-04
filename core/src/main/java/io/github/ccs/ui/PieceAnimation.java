package io.github.ccs.ui;

import io.github.ccs.game_logic.Piece;

/**
 * Tracks and updates animated piece movement across the board, plus the
 * secondary castling rook slide, the fading en-passant ghost pawn, and the
 * nine-frame promotion lightning flash.
 */
public class PieceAnimation {
    private static final float FLASH_FRAME_SECONDS = 0.065f;
    private static final int FLASH_FRAMES = 9;

    private int piece = Piece.EMPTY;
    private int fromRow = -1;
    private int fromColumn = -1;
    private int toRow = -1;
    private int toColumn = -1;
    private float progress = 1f;

    private int secondaryPiece = Piece.EMPTY;
    private int secondaryFromRow = -1;
    private int secondaryFromColumn = -1;
    private int secondaryToRow = -1;
    private int secondaryToColumn = -1;

    private int ghostPiece = Piece.EMPTY;
    private int ghostRow = -1;
    private int ghostColumn = -1;

    private boolean flashActive;
    private float flashTimer;

    public void start(int piece, int fromRow, int fromColumn, int toRow, int toColumn) {
        this.piece = piece;
        this.fromRow = fromRow;
        this.fromColumn = fromColumn;
        this.toRow = toRow;
        this.toColumn = toColumn;
        this.progress = 0f;
        secondaryPiece = Piece.EMPTY;
        ghostPiece = Piece.EMPTY;
    }

    /** Slides the castling rook in sync with the king's main move. */
    public void startSecondary(int piece, int fromRow, int fromColumn, int toRow, int toColumn) {
        this.secondaryPiece = piece;
        this.secondaryFromRow = fromRow;
        this.secondaryFromColumn = fromColumn;
        this.secondaryToRow = toRow;
        this.secondaryToColumn = toColumn;
    }

    /** Leaves a fading ghost of the en-passant captured pawn on its square. */
    public void startRemoval(int piece, int row, int column) {
        this.ghostPiece = piece;
        this.ghostRow = row;
        this.ghostColumn = column;
    }

    public void startPromotionFlash() {
        flashActive = true;
        flashTimer = 0f;
    }

    public void update(float delta) {
        if (flashActive) {
            flashTimer += delta;
            if (flashTimer >= FLASH_FRAMES * FLASH_FRAME_SECONDS) {
                flashActive = false;
            }
        }
        if (!isAnimating()) return;
        progress = Math.min(1f, progress + delta * 5f);
        if (progress >= 1f) {
            piece = Piece.EMPTY;
            secondaryPiece = Piece.EMPTY;
            ghostPiece = Piece.EMPTY;
        }
    }

    public boolean isAnimating() {
        return piece != Piece.EMPTY;
    }

    public boolean hasSecondary() {
        return secondaryPiece != Piece.EMPTY;
    }

    public boolean hasGhost() {
        return ghostPiece != Piece.EMPTY;
    }

    public boolean isFlashing() {
        return flashActive;
    }

    /** @return the current lightning frame index, 0 through 8. */
    public int getFlashFrame() {
        int frame = (int) (flashTimer / FLASH_FRAME_SECONDS);
        return Math.min(FLASH_FRAMES - 1, Math.max(0, frame));
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

    public int getSecondaryPiece() {
        return secondaryPiece;
    }

    public int getSecondaryFromRow() {
        return secondaryFromRow;
    }

    public int getSecondaryFromColumn() {
        return secondaryFromColumn;
    }

    public int getSecondaryToRow() {
        return secondaryToRow;
    }

    public int getSecondaryToColumn() {
        return secondaryToColumn;
    }

    public int getGhostPiece() {
        return ghostPiece;
    }

    public int getGhostRow() {
        return ghostRow;
    }

    public int getGhostColumn() {
        return ghostColumn;
    }
}
