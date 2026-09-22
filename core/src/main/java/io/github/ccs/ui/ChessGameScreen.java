package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;

import io.github.ccs.MainGame;
import io.github.ccs.game_logic.Board;

/** Screen managing the chess game session and coordinating renderer and input components. */
public class ChessGameScreen extends ScreenAdapter {
    private final Board board;
    private final PieceAnimation animation = new PieceAnimation();
    private BoardRenderer renderer;

    public ChessGameScreen(MainGame game, Board board) {
        this.board = board;
    }

    @Override
    public void show() {
        renderer = new BoardRenderer(board);
        BoardInputHandler boardInputHandler = new BoardInputHandler(board, renderer, animation);

        // Chain the processors so UI clicks register before board clicks
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(renderer.getStage()); // UI first
        multiplexer.addProcessor(boardInputHandler);   // Board input second

        Gdx.input.setInputProcessor(multiplexer);
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
