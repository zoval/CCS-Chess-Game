package io.github.ccs.game_logic;

/** Tracks the 50-move draw rule: 50 consecutive full moves (100 halfmoves) without a pawn move or capture. */
public class FiftymoveRule {
    private static final int HALFMOVE_LIMIT = 100;

    private int halfmoveClock;

    public void recordMove(boolean pawnMoved, boolean captureMade) {
        if (pawnMoved || captureMade) {
            halfmoveClock = 0;
        } else {
            halfmoveClock++;
        }
    }

    public boolean isDraw() {
        return halfmoveClock >= HALFMOVE_LIMIT;
    }

    public int getHalfmoveClock() {
        return halfmoveClock;
    }

    public void reset() {
        halfmoveClock = 0;
    }
}
