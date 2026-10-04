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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.ccs.MainGame;
import io.github.ccs.sound.SoundManager;

/** Main menu displaying the supplied menu artwork and navigation actions. */
public class FirstScreen extends ScreenAdapter {
    private static final float PANEL_WIDTH = 400f;
    private static final float PANEL_HEIGHT = 484f;
    private static final float PANEL_FRAME_PAD = 20f;
    private static final float BUTTON_WIDTH = 330f;
    private static final float BUTTON_GAP = 16f;
    private static final float TITLE_WIDTH = 500f;
    private static final float TITLE_PANEL_GAP = 40f;
    private static final float TITLE_PANEL_TOP_OVERLAP = 20f;

    private final MainGame game;
    private Stage stage;
    private Image backgroundImage;
    private Image titleImage;
    private Table menuPanel;
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
    private SettingsDialog settingsDialog;
    private final java.util.List<IlluminatedButton> menuButtons = new java.util.ArrayList<>();

    public FirstScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        SoundManager.getInstance().initialize();
        SoundManager.getInstance().playMenuMusic();
        loadTextures();
        buildMenu();
        installInputHandling();
        settingsDialog = new SettingsDialog(game);
        stage.addActor(settingsDialog);
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
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setScaling(Scaling.fill);
        stage.addActor(backgroundImage);

        titleImage = new Image(new TextureRegionDrawable(titleRegion));
        titleImage.setSize(TITLE_WIDTH, TITLE_WIDTH * titleRegion.getRegionHeight() / (float) titleRegion.getRegionWidth());
        stage.addActor(titleImage);

        menuPanel = new Table();
        menuPanel.setBackground(new TextureRegionDrawable(panelRegion));
        menuPanel.setSize(PANEL_WIDTH, PANEL_HEIGHT);
        menuPanel.pad(PANEL_FRAME_PAD);
        menuPanel.center();

        menuPanel.add(createButton(playRegion, () -> game.setScreen(new OpponentSelectScreen(game))))
            .spaceBottom(BUTTON_GAP).row();
        menuPanel.add(createButton(settingsRegion, () -> {
            SoundManager.getInstance().playUIClick();
            settingsDialog.open();
        })).spaceBottom(BUTTON_GAP).row();
        menuPanel.add(createButton(collectionRegion, () -> game.setScreen(new MapSelectScreen(game)))).spaceBottom(BUTTON_GAP).row();
        menuPanel.add(createButton(quitRegion, Gdx.app::exit)).row();

        stage.addActor(menuPanel);
        layoutMenu(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void layoutMenu(float width, float height) {
        float contentWidth = TITLE_WIDTH + TITLE_PANEL_GAP + PANEL_WIDTH;
        float contentLeft = (width - contentWidth) / 2f;
        float panelY = (height - PANEL_HEIGHT) / 2f;

        backgroundImage.setBounds(0f, 0f, width, height);
        titleImage.setPosition(contentLeft,
            panelY + PANEL_HEIGHT - titleImage.getHeight() + TITLE_PANEL_TOP_OVERLAP);
        menuPanel.setPosition(contentLeft + TITLE_WIDTH + TITLE_PANEL_GAP, panelY);
    }

    private IlluminatedButton createButton(TextureRegion region, Runnable action) {
        float aspect = region.getRegionHeight() / (float) region.getRegionWidth();
        IlluminatedButton button = new IlluminatedButton(region, action);
        button.getImageCell().size(BUTTON_WIDTH, BUTTON_WIDTH * aspect);
        menuButtons.add(button);
        return button;
    }

    private void installInputHandling() {
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.M) {
                    SoundManager.getInstance().toggleSound();
                    return true;
                }
                return false;
            }
        });
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
            layoutMenu(width, height);
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
        if (settingsDialog != null) {
            settingsDialog.dispose();
        }
        for (IlluminatedButton button : menuButtons) {
            button.dispose();
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
