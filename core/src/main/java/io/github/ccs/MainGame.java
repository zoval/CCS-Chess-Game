package io.github.ccs;

import com.badlogic.gdx.Game;

import io.github.ccs.ai_networking.Difficulty;
import io.github.ccs.game_logic.GameState;
import io.github.ccs.sound.SoundManager;
import io.github.ccs.ui.BoardTheme;
import io.github.ccs.ui.FirstScreen;
import io.github.ccs.ui.Fonts;

/** Shared LibGDX application entry point. */
//main method for the game, initializes the game and sets the first screen to be displayed.
public class MainGame extends Game {

    /** Opponent configuration chosen on the opponent select screen. */
    public enum GameMode { P_V_P, P_V_AI }

    /** Menu window size; matches the 1024x572 menu background (menu/BG.jpg). */
    public static final int MENU_WINDOW_WIDTH = 1024;
    public static final int MENU_WINDOW_HEIGHT = 572;

    /** Game window size; matches the square 750x750 board background (board/new bg.png). */
    public static final int GAME_WINDOW_WIDTH = 750;
    public static final int GAME_WINDOW_HEIGHT = 750;

    private GameMode gameMode = GameMode.P_V_P;
    private Difficulty difficulty = Difficulty.MEDIUM;
    private int timeControlMinutes = 10;
    private BoardTheme selectedMap = BoardTheme.CLASSIC;
    private GameState pendingLoad;
    private boolean visualAidsEnabled = true;

    @Override
    public void create() {
        setScreen(new FirstScreen(this));
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public int getTimeControlMinutes() {
        return timeControlMinutes;
    }

    public void setTimeControlMinutes(int timeControlMinutes) {
        this.timeControlMinutes = timeControlMinutes;
    }

    /**
     * Stores a loaded save to be resumed by the next started match, replacing
     * any previous pending load.
     */
    public void setPendingLoad(GameState pendingLoad) {
        this.pendingLoad = pendingLoad;
    }

    /** @return the pending loaded save, or null when starting fresh. */
    public GameState getPendingLoad() {
        return pendingLoad;
    }

    /** @return the pending loaded save and clears it (a load is consumed once). */
    public GameState consumePendingLoad() {
        GameState state = pendingLoad;
        pendingLoad = null;
        return state;
    }

    public BoardTheme getSelectedMap() {
        return selectedMap;
    }

    public void setSelectedMap(BoardTheme selectedMap) {
        this.selectedMap = selectedMap;
    }

    /** @return true when in-game visual aids (move hints, highlights) should be rendered. */
    public boolean isVisualAidsEnabled() {
        return visualAidsEnabled;
    }

    public void setVisualAidsEnabled(boolean visualAidsEnabled) {
        this.visualAidsEnabled = visualAidsEnabled;
    }

    @Override
    public void dispose() {
        super.dispose();
        Fonts.dispose();
        SoundManager.getInstance().dispose();
    }
}
