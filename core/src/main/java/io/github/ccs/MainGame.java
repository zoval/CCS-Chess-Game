package io.github.ccs;

import com.badlogic.gdx.Game;

import io.github.ccs.sound.SoundManager;
import io.github.ccs.ui.BoardTheme;
import io.github.ccs.ui.FirstScreen;

/** Shared LibGDX application entry point. */
//main method for the game, initializes the game and sets the first screen to be displayed.
public class MainGame extends Game {
    /** Menu window size; matches the 1024x572 menu background (menu/BG.jpg). */
    public static final int MENU_WINDOW_WIDTH = 1024;
    public static final int MENU_WINDOW_HEIGHT = 572;

    /** Game window size; matches the square 750x750 board background (board/new bg.png). */
    public static final int GAME_WINDOW_WIDTH = 750;
    public static final int GAME_WINDOW_HEIGHT = 750;

    private BoardTheme selectedTheme = BoardTheme.MINECRAFT;
    
    @Override
    public void create() {
        setScreen(new FirstScreen(this));
    }

    public BoardTheme getSelectedTheme() {
        return selectedTheme;
    }

    public void setSelectedTheme(BoardTheme selectedTheme) {
        this.selectedTheme = selectedTheme;
    }

    @Override
    public void dispose() {
        super.dispose();
        SoundManager.getInstance().dispose();
    }
}
