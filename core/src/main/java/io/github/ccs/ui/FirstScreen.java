package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.ccs.MainGame;

/** Main menu displaying the supplied menu artwork and navigation actions. */
public class FirstScreen extends ScreenAdapter {
    private static final float PANEL_WIDTH = 400f;
    private static final float PANEL_HEIGHT = 484f;
    private static final float PANEL_RIGHT_MARGIN = 48f;
    private static final float PANEL_FRAME_PAD = 20f;
    private static final float BUTTON_WIDTH = 330f;
    private static final float BUTTON_GAP = 16f;
    private static final float TITLE_WIDTH = 500f;
    private static final float TITLE_TOP_MARGIN = 24f;

    private final MainGame game;
    private Stage stage;
    private Texture backgroundTexture;
    private Texture panelTexture;
    private Texture titleTexture;
    private Texture playTexture;
    private Texture settingsTexture;
    private Texture collectionTexture;
    private Texture quitTexture;
    private TextureRegion panelRegion;
    private TextureRegion titleRegion;
    private TextureRegion playRegion;
    private TextureRegion settingsRegion;
    private TextureRegion collectionRegion;
    private TextureRegion quitRegion;

    public FirstScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        loadTextures();
        buildMenu();
        installInputHandling();
        Gdx.input.setInputProcessor(stage);
    }

    private void loadTextures() {
        backgroundTexture = loadTexture("menu/BG.jpg");
        panelTexture = loadTexture("menu/wood bg.png");
        titleTexture = loadTexture("menu/Title.png");
        playTexture = loadTexture("menu/Play.png");
        settingsTexture = loadTexture("menu/Settings.png");
        collectionTexture = loadTexture("menu/Collection.png");
        quitTexture = loadTexture("menu/Quit.png");

        // The art files keep each asset centered on an oversized transparent canvas.
        // These regions crop to the visible artwork (measured bounds) so layout uses real sizes.
        panelRegion = new TextureRegion(panelTexture, 397, 32, 519, 628);
        titleRegion = new TextureRegion(titleTexture, 35, 124, 2124, 523);
        playRegion = new TextureRegion(playTexture, 119, 261, 995, 194);
        settingsRegion = new TextureRegion(settingsTexture, 213, 291, 674, 137);
        collectionRegion = new TextureRegion(collectionTexture, 426, 63, 1734, 359);
        quitRegion = new TextureRegion(quitTexture, 87, 240, 1076, 218);
    }

    private Texture loadTexture(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    private void buildMenu() {
        Image background = new Image(backgroundTexture);
        background.setFillParent(true);
        background.setScaling(Scaling.fill);
        stage.addActor(background);

        float panelLeftX = Gdx.graphics.getWidth() - PANEL_RIGHT_MARGIN - PANEL_WIDTH;

        Image title = new Image(new TextureRegionDrawable(titleRegion));
        title.setSize(TITLE_WIDTH, TITLE_WIDTH * titleRegion.getRegionHeight() / (float) titleRegion.getRegionWidth());
        // Centered in the space left of the panel so it never slides under it.
        title.setPosition((panelLeftX - title.getWidth()) / 2f,
            Gdx.graphics.getHeight() - TITLE_TOP_MARGIN - title.getHeight());
        stage.addActor(title);

        Table panel = new Table();
        panel.setBackground(new TextureRegionDrawable(panelRegion));
        panel.setSize(PANEL_WIDTH, PANEL_HEIGHT);
        panel.setPosition(panelLeftX, (Gdx.graphics.getHeight() - PANEL_HEIGHT) / 2f);
        panel.pad(PANEL_FRAME_PAD);
        panel.center();

        panel.add(createButton(playRegion, () -> game.setScreen(new ChessGameScreen(game)))).spaceBottom(BUTTON_GAP).row();
        panel.add(createButton(settingsRegion, null)).spaceBottom(BUTTON_GAP).row();
        panel.add(createButton(collectionRegion, () -> game.setScreen(new CollectionScreen(game)))).spaceBottom(BUTTON_GAP).row();
        panel.add(createButton(quitRegion, Gdx.app::exit)).row();

        stage.addActor(panel);
    }

    private ImageButton createButton(TextureRegion region, Runnable action) {
        float aspect = region.getRegionHeight() / (float) region.getRegionWidth();
        ImageButton button = new ImageButton(new TextureRegionDrawable(region));
        button.getImageCell().size(BUTTON_WIDTH, BUTTON_WIDTH * aspect);

        if (action == null) {
            button.setTouchable(Touchable.disabled);
            return button;
        }

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                action.run();
            }
        });

        return button;
    }

    private void installInputHandling() {
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.F11) {
                    toggleFullscreen();
                    return true;
                }
                return false;
            }
        });
    }

    private void toggleFullscreen() {
        if (Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(1024, 572);
        } else {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }
    }

    @Override
    public void render(float delta) {
        if (Gdx.graphics.getWidth() <= 0 || Gdx.graphics.getHeight() <= 0) {
            return;
        }
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (width > 0 && height > 0 && stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        disposeTexture(backgroundTexture);
        disposeTexture(panelTexture);
        disposeTexture(titleTexture);
        disposeTexture(playTexture);
        disposeTexture(settingsTexture);
        disposeTexture(collectionTexture);
        disposeTexture(quitTexture);
    }

    private void disposeTexture(Texture texture) {
        if (texture != null) {
            texture.dispose();
        }
    }
}
