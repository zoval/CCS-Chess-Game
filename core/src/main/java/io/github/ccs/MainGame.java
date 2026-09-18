package io.github.ccs;

import com.badlogic.gdx.Game;

import io.github.ccs.ui.FirstScreen;

/** Shared LibGDX application entry point. */
//main method for the game, initializes the game and sets the first screen to be displayed.
public class MainGame extends Game {
    @Override
    public void create() {
        setScreen(new FirstScreen(this));
    }
}
