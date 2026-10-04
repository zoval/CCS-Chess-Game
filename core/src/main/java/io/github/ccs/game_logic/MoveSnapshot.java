package io.github.ccs.game_logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable restorable snapshot of the full game state at one point in a
 * game. Used by {@link MoveHistory} for view-only review and by the save
 * system to serialize a game.
 */
public final class MoveSnapshot {

    private final int[] pieces = new int[64];
    private final boolean whiteTurn;
    private final String statusText;
    private final boolean gameOver;
    private final int halfmoveClock;
    private final SpecialMoves special;
    private final List<Integer> capturedByWhite;
    private final List<Integer> capturedByBlack;

    /**
     * Creates a snapshot. All inputs are copied; later changes to the
     * callers' arrays, lists, or {@link SpecialMoves} state do not affect
     * the snapshot.
     */
    public MoveSnapshot(int[][] pieces, boolean whiteTurn, String statusText, boolean gameOver,
            int halfmoveClock, SpecialMoves special,
            List<Integer> capturedByWhite, List<Integer> capturedByBlack) {

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                this.pieces[row * 8 + column] = pieces[row][column];
            }
        }

        this.whiteTurn = whiteTurn;
        this.statusText = statusText;
        this.gameOver = gameOver;
        this.halfmoveClock = halfmoveClock;

        this.special = special.copy();

        this.capturedByWhite = Collections.unmodifiableList(new ArrayList<Integer>(capturedByWhite));
        this.capturedByBlack = Collections.unmodifiableList(new ArrayList<Integer>(capturedByBlack));
    }

    /** @return the signed piece ID at the given square. */
    public int getPiece(int row, int column) {
        return pieces[row * 8 + column];
    }

    /** @return the 64-cell flat board array (row-major, row 0 first). */
    public int[] getPieces() {
        return pieces.clone();
    }

    /** @return true if it is White's turn in this snapshot. */
    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    /** @return the user-facing status text at this point. */
    public String getStatusText() {
        return statusText;
    }

    /** @return true if the game was over at this point. */
    public boolean isGameOver() {
        return gameOver;
    }

    /** @return the halfmove clock at this point. */
    public int getHalfmoveClock() {
        return halfmoveClock;
    }

    /** @return a copy of the castling / en-passant state at this point. */
    public SpecialMoves getSpecialMoves() {
        return special.copy();
    }

    /** @return signed piece IDs of all pieces White has captured so far. */
    public List<Integer> getCapturedByWhite() {
        return capturedByWhite;
    }

    /** @return signed piece IDs of all pieces Black has captured so far. */
    public List<Integer> getCapturedByBlack() {
        return capturedByBlack;
    }
}
