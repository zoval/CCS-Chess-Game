package io.github.ccs.game_logic;

/**
 * Per-side countdown clock in milliseconds. The active side's time is
 * reduced by {@link #update}; a side whose clock reaches zero has flagged.
 */
public class GameClock {

    private long whiteMillis;
    private long blackMillis;

    /** Creates a clock with the given time per side. */
    public GameClock(long millisPerSide) {
        reset(millisPerSide);
    }

    /** Restores both clocks to the given time per side. */
    public void reset(long millisPerSide) {
        whiteMillis = millisPerSide;
        blackMillis = millisPerSide;
    }

    /**
     * Reduces the active side's clock by the elapsed frame time. Call once
     * per frame; the inactive side is untouched.
     */
    public void update(float delta, boolean whiteActive) {
        long elapsed = (long) (delta * 1000f);
        if (whiteActive) {
            whiteMillis = Math.max(0, whiteMillis - elapsed);
        } else {
            blackMillis = Math.max(0, blackMillis - elapsed);
        }
    }

    /** @return White's remaining time in milliseconds. */
    public long getWhiteMillis() {
        return whiteMillis;
    }

    /** @return Black's remaining time in milliseconds. */
    public long getBlackMillis() {
        return blackMillis;
    }

    /** Restores White's clock (used when loading a saved game). */
    public void setWhiteMillis(long millis) {
        whiteMillis = Math.max(0, millis);
    }

    /** Restores Black's clock (used when loading a saved game). */
    public void setBlackMillis(long millis) {
        blackMillis = Math.max(0, millis);
    }

    /** @return true if White's clock has reached zero. */
    public boolean isWhiteFlagged() {
        return whiteMillis <= 0;
    }

    /** @return true if Black's clock has reached zero. */
    public boolean isBlackFlagged() {
        return blackMillis <= 0;
    }

    /** @return the time formatted as {@code M:SS} (seconds rounded up). */
    public static String format(long millis) {
        long seconds = (millis + 999) / 1000;
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }
}
