package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.ccs.MainGame;
import io.github.ccs.game_logic.Board;
import io.github.ccs.sound.SoundManager;

/** Screen managing the chess game session and coordinating renderer and input components. */
public class ChessGameScreen extends ScreenAdapter {
    private final Board board = new Board();
    private final PieceAnimation animation = new PieceAnimation();
    private final MainGame game;
    private BoardRenderer renderer;
    private BoardInputHandler inputHandler;
    private Stage hudStage;
    private SettingsDialog settingsDialog;
    private Texture gearTexture;
    private float boardX, boardY, boardSize;

    public ChessGameScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        renderer = new BoardRenderer(game.getSelectedTheme());
        renderer.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        calculateLayout();
        SoundManager.getInstance().initialize();
        SoundManager.getInstance().playGameMusic();

        hudStage = new Stage(new ScreenViewport());
        settingsDialog = new SettingsDialog(game);
        hudStage.addActor(settingsDialog);
        inputHandler = new BoardInputHandler(board, animation, this);
        buildSettingsButton();

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hudStage);
        multiplexer.addProcessor(inputHandler);
        Gdx.input.setInputProcessor(multiplexer);
    }

    private void buildSettingsButton() {
        gearTexture = ProceduralTextures.gearIcon(64);
        ImageButton settingsButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(gearTexture)));
        float buttonSize = 44f;
        settingsButton.setSize(buttonSize, buttonSize);
        settingsButton.setPosition(Gdx.graphics.getWidth() - buttonSize - 10f,
            Gdx.graphics.getHeight() - buttonSize - 10f);
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (settingsDialog.isOpen()) {
                    settingsDialog.hide();
                } else {
                    settingsDialog.open();
                }
            }
        });
        hudStage.addActor(settingsButton);
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
            inputHandler.setInputBlocked(settingsDialog != null && settingsDialog.isOpen());
            renderer.render(board, animation, boardX, boardY, boardSize,
                game.isVisualAidsEnabled(),
                inputHandler.getSelectedRow(), inputHandler.getSelectedColumn(),
                inputHandler.getHoverRow(), inputHandler.getHoverColumn(), delta);
        }
        if (hudStage != null) {
            hudStage.act(delta);
            hudStage.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        calculateLayout();
        if (renderer != null) {
            renderer.resize(width, height);
        }
    }

    public float getBoardX() { return boardX; }
    public float getBoardY() { return boardY; }
    public float getBoardSize() { return boardSize; }

    public float getGridX() { return renderer.getGridX(); }
    public float getGridY() { return renderer.getGridY(); }
    public float getSquareW() { return renderer.getSquareW(); }
    public float getSquareH() { return renderer.getSquareH(); }

    /** Restores the menu window size when windowed and returns to the main menu. */
    public void backToMenu() {
        if (!Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(MainGame.MENU_WINDOW_WIDTH, MainGame.MENU_WINDOW_HEIGHT);
        }
        game.setScreen(new FirstScreen(game));
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        SoundManager.getInstance().stopMusic();
    }

    @Override
    public void dispose() {
        SoundManager.getInstance().stopMusic();
        if (renderer != null) {
            renderer.dispose();
        }
        if (settingsDialog != null) {
            settingsDialog.dispose();
        }
        if (gearTexture != null) {
            gearTexture.dispose();
        }
        if (hudStage != null) {
            hudStage.dispose();
        }
    }
}
