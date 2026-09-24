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
    private final MainGame game;
    private BoardRenderer renderer;
    private float boardX, boardY, boardSize;

    public ChessGameScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        renderer = new BoardRenderer(game.getSelectedTheme());
        calculateLayout();
        Gdx.input.setInputProcessor(new BoardInputHandler(board, animation, this));
    }

    private void calculateLayout() {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        boardSize = Math.min(width, height - 100);
        boardX = (width - boardSize) / 2f;
        boardY = (height - boardSize) / 2f;
    }

    @Override
    public void render(float delta) {
        if (Gdx.graphics.getWidth() <= 0 || Gdx.graphics.getHeight() <= 0) {
            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        animation.update(delta);
        if (renderer != null) {
            renderer.render(board, animation, boardX, boardY, boardSize);
        }
    }

    @Override
    public void resize(int width, int height) {
        calculateLayout();
    }

    public float getBoardX() { return boardX; }
    public float getBoardY() { return boardY; }
    public float getBoardSize() { return boardSize; }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }
// ...existing code...


    @Override
    public void dispose() {
        if (renderer != null) {
            renderer.dispose();
        }
    }
}
