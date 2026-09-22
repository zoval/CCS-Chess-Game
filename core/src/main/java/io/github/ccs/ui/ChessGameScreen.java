package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;

import io.github.ccs.MainGame;
import io.github.ccs.game_logic.Board;

/** Screen managing the chess game session and coordinating renderer and input components. */
public class ChessGameScreen extends ScreenAdapter {
    private final Board board = new Board();
    private final PieceAnimation animation = new PieceAnimation();
    private BoardRenderer renderer;

    public ChessGameScreen() {
    }

    public ChessGameScreen(MainGame game) {
        this();
    }

    @Override
    public void show() {
        renderer = new BoardRenderer();
        Gdx.input.setInputProcessor(new BoardInputHandler(board, renderer, animation));
    }

    /**
     * Renders active animations, board state, and UI.
     * Skips drawing when window dimensions are non-positive (e.g. minimized).
     *
     * @param delta time elapsed in seconds since last frame
     */
    @Override
    public void render(float delta) {
        if (Gdx.graphics.getWidth() <= 0 || Gdx.graphics.getHeight() <= 0) {
            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        animation.update(delta);
        if (renderer != null) {
            renderer.render(board, animation);
        }
    }

    /**
     * Propagates resize events to the board renderer, guarding against minimized state.
     *
     * @param width  new screen width in pixels
     * @param height new screen height in pixels
     */
    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        if (renderer != null) {
            renderer.resize(width, height);
        }
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (renderer != null) {
            renderer.dispose();
        }
    }
}
