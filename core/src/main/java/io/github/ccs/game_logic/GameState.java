package io.github.ccs.game_logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything needed to restore a game in progress: the setup the player
 * chose (mode, difficulty, map, time control), the remaining clock times,
 * and the full snapshot history with the view index. Mode, difficulty, and
 * map are stored as strings so the game_logic package stays independent of
 * the UI and AI enums; callers convert with their own {@code valueOf}.
 */
public final class GameState {

    private String mode;
    private String difficulty;
    private String mapName;
    private int timeControlMinutes;
    private long whiteMillis;
    private long blackMillis;
    private List<MoveSnapshot> snapshots = new ArrayList<MoveSnapshot>();
    private int historyIndex;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public int getTimeControlMinutes() {
        return timeControlMinutes;
    }

    public void setTimeControlMinutes(int timeControlMinutes) {
        this.timeControlMinutes = timeControlMinutes;
    }

    /** @return milliseconds remaining on White's clock. */
    public long getWhiteMillis() {
        return whiteMillis;
    }

    public void setWhiteMillis(long whiteMillis) {
        this.whiteMillis = whiteMillis;
    }

    /** @return milliseconds remaining on Black's clock. */
    public long getBlackMillis() {
        return blackMillis;
    }

    public void setBlackMillis(long blackMillis) {
        this.blackMillis = blackMillis;
    }

    /** @return the full snapshot history, oldest first. */
    public List<MoveSnapshot> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(List<MoveSnapshot> snapshots) {
        this.snapshots = snapshots;
    }

    /** @return the history view index the game was saved at. */
    public int getHistoryIndex() {
        return historyIndex;
    }

    public void setHistoryIndex(int historyIndex) {
        this.historyIndex = historyIndex;
    }
}
