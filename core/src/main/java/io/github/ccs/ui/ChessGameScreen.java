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

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        animation.update(delta);
        renderer.render(board, animation);
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
