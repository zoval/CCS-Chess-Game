package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.ccs.MainGame;
import io.github.ccs.sound.SoundManager;

/**
 * Map selection screen (replaces the old Collection screen). Shows the maps as tabs
 * over the wooden MAPS panel with a landscape preview, board/difficulty info, and a
 * SELECT MAP button. All layout lives in a 1024x572 design space scaled to the window.
 */
public class MapSelectScreen extends ScreenAdapter {
    private static final float DESIGN_W = 1024f;
    private static final float DESIGN_H = 572f;
    private static final Color TEXT_COLOR = new Color(0.94f, 0.87f, 0.70f, 1f);
    private static final Color TAB_INACTIVE_TEXT = new Color(0.62f, 0.56f, 0.44f, 1f);
    private static final Color TAB_BACK = new Color(0.23f, 0.19f, 0.15f, 1f);
    private static final Color TAB_BACK_SELECTED = new Color(0.33f, 0.27f, 0.2f, 1f);

    private static final BoardTheme[] MAPS = {BoardTheme.CLASSIC, BoardTheme.STANDARD, BoardTheme.NETHER};
    private static final String[] BOARD_LINES = {
        "Board: Oak & Stone", "Board: Tournament Standard", "Board: Obsidian & Nether Brick"};
    private static final String[] DIFFICULTY_LINES = {
        "Difficulty: Easy", "Difficulty: Normal", "Difficulty: Hard"};

    private final MainGame game;
    private Stage stage;
    private Texture panelTexture;
    private Texture headerTexture;
    private Texture frameTexture;
    private Texture selectTexture;
    private Texture closeTexture;
    private Texture tabClassicTexture;
    private Texture tabClassicInactiveTexture;
    private Texture tabNetherTexture;
    private Texture tabNetherInactiveTexture;
    private Texture titleClassicTexture;
    private Texture titleNetherTexture;
    private Texture classicLandscapeTexture;
    private Texture netherLandscapeTexture;
    private Texture standardBoardTexture;
    private Texture tabBackTexture;

    private TextureRegion panelRegion;
    private TextureRegion headerRegion;
    private TextureRegion frameRegion;
    private TextureRegion selectRegion;
    private TextureRegion closeRegion;
    private TextureRegion tabClassicRegion;
    private TextureRegion tabClassicInactiveRegion;
    private TextureRegion tabNetherRegion;
    private TextureRegion tabNetherInactiveRegion;
    private TextureRegionDrawable titleClassicDrawable;
    private TextureRegionDrawable titleNetherDrawable;

    private Image panelImage;
    private Image headerImage;
    private Image frameImage;
    private Image previewImage;
    private Image selectButton;
    private Image closeButton;
    private Image titleImage;
    private final Image[] tabImages = new Image[3];
    private Label standardTabLabel;
    private Label titleLabel;
    private Label infoLabel;
    private int selectedTab;

    public MapSelectScreen(MainGame game) {
        this.game = game;
        for (int i = 0; i < MAPS.length; i++) {
            if (MAPS[i] == game.getSelectedMap()) {
                selectedTab = i;
            }
        }
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        loadTextures();
        buildUi();
        installInputHandling();
        Gdx.input.setInputProcessor(stage);
        if (!Gdx.graphics.isFullscreen()
            && (Gdx.graphics.getWidth() != MainGame.MENU_WINDOW_WIDTH
                || Gdx.graphics.getHeight() != MainGame.MENU_WINDOW_HEIGHT)) {
            Gdx.graphics.setWindowedMode(MainGame.MENU_WINDOW_WIDTH, MainGame.MENU_WINDOW_HEIGHT);
        }
    }

    private void loadTextures() {
        panelTexture = loadTexture("maps/board_art.png");
        headerTexture = loadTexture("maps/header_maps.png");
        frameTexture = loadTexture("maps/panel_frame.png");
        selectTexture = loadTexture("maps/select_map_button.png");
        closeTexture = loadTexture("maps/close.png");
        tabClassicTexture = loadTexture("maps/tab_classic.png");
        tabClassicInactiveTexture = loadTexture("maps/tab_classic_inactive.png");
        tabNetherTexture = loadTexture("maps/tab_nether.png");
        tabNetherInactiveTexture = loadTexture("maps/tab_nether_inactive.png");
        titleClassicTexture = loadTexture("maps/title_classic.png");
        titleNetherTexture = loadTexture("maps/title_nether.png");
        classicLandscapeTexture = loadTexture("maps/classic_landscape.jpeg");
        netherLandscapeTexture = loadTexture("maps/nether_landscape.jpeg");
        standardBoardTexture = loadTexture("pieces/board.png");
        tabBackTexture = ProceduralTextures.whitePixel();

        // Measured alpha-bounds crops; the art files keep each asset on an oversized canvas.
        panelRegion = new TextureRegion(panelTexture);
        headerRegion = new TextureRegion(headerTexture, 17, 52, 884, 179);
        frameRegion = new TextureRegion(frameTexture, 22, 49, 724, 384);
        selectRegion = new TextureRegion(selectTexture, 83, 12, 483, 131);
        closeRegion = new TextureRegion(closeTexture, 35, 16, 111, 108);
        tabClassicRegion = new TextureRegion(tabClassicTexture, 44, 35, 415, 116);
        tabClassicInactiveRegion = new TextureRegion(tabClassicInactiveTexture, 63, 14, 429, 122);
        tabNetherRegion = new TextureRegion(tabNetherTexture, 115, 9, 423, 117);
        tabNetherInactiveRegion = new TextureRegion(tabNetherInactiveTexture, 90, 18, 415, 123);
        titleClassicDrawable = new TextureRegionDrawable(new TextureRegion(titleClassicTexture, 64, 17, 467, 49));
        titleNetherDrawable = new TextureRegionDrawable(new TextureRegion(titleNetherTexture, 9, 32, 598, 62));
    }

    private Texture loadTexture(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    private void buildUi() {
        panelImage = new Image(panelRegion);
        stage.addActor(panelImage);

        headerImage = new Image(headerRegion);
        stage.addActor(headerImage);

        for (int i = 0; i < 3; i++) {
            final int index = i;
            tabImages[i] = new Image();
            tabImages[i].addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (selectedTab != index) {
                        SoundManager.getInstance().playUIClick();
                        selectedTab = index;
                        refresh();
                    }
                }
            });
            stage.addActor(tabImages[i]);
        }
        tabImages[1].setDrawable(new TextureRegionDrawable(new TextureRegion(tabBackTexture)));
        standardTabLabel = new Label("STANDARD", new Label.LabelStyle(Fonts.small(), TEXT_COLOR));
        stage.addActor(standardTabLabel);

        frameImage = new Image(frameRegion);
        stage.addActor(frameImage);

        previewImage = new Image();
        stage.addActor(previewImage);

        titleImage = new Image();
        stage.addActor(titleImage);
        titleLabel = new Label("STANDARD", new Label.LabelStyle(Fonts.large(), TEXT_COLOR));
        stage.addActor(titleLabel);

        infoLabel = new Label("", new Label.LabelStyle(Fonts.small(), TEXT_COLOR));
        infoLabel.setAlignment(Align.center);
        stage.addActor(infoLabel);

        selectButton = new Image(selectRegion);
        selectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().playUIClick();
                game.setSelectedMap(MAPS[selectedTab]);
                game.setScreen(new FirstScreen(game));
            }
        });
        stage.addActor(selectButton);

        closeButton = new Image(closeRegion);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().playUIClick();
                game.setScreen(new FirstScreen(game));
            }
        });
        stage.addActor(closeButton);

        refresh();
        layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void refresh() {
        tabImages[0].setDrawable(new TextureRegionDrawable(
            selectedTab == 0 ? tabClassicRegion : tabClassicInactiveRegion));
        tabImages[1].setColor(selectedTab == 1 ? TAB_BACK_SELECTED : TAB_BACK);
        tabImages[2].setDrawable(new TextureRegionDrawable(
            selectedTab == 2 ? tabNetherRegion : tabNetherInactiveRegion));
        standardTabLabel.setColor(selectedTab == 1 ? TEXT_COLOR : TAB_INACTIVE_TEXT);

        switch (MAPS[selectedTab]) {
            case CLASSIC:
                previewImage.setDrawable(new TextureRegionDrawable(new TextureRegion(classicLandscapeTexture)));
                titleImage.setDrawable(titleClassicDrawable);
                break;
            case STANDARD:
                previewImage.setDrawable(new TextureRegionDrawable(new TextureRegion(standardBoardTexture)));
                break;
            case NETHER:
                previewImage.setDrawable(new TextureRegionDrawable(new TextureRegion(netherLandscapeTexture)));
                titleImage.setDrawable(titleNetherDrawable);
                break;
        }
        boolean artTitle = MAPS[selectedTab] != BoardTheme.STANDARD;
        titleImage.setVisible(artTitle);
        titleLabel.setVisible(!artTitle);

        infoLabel.setText(BOARD_LINES[selectedTab] + "\n" + DIFFICULTY_LINES[selectedTab]);
    }

    private void installInputHandling() {
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    SoundManager.getInstance().playUIClick();
                    game.setScreen(new FirstScreen(game));
                    return true;
                }
                if (keycode == Input.Keys.M) {
                    SoundManager.getInstance().toggleSound();
                    return true;
                }
                return false;
            }
        });
    }

    private void layout(float width, float height) {
        float scale = Math.min(width / DESIGN_W, height / DESIGN_H);
        float offsetX = (width - DESIGN_W * scale) / 2f;
        float offsetY = (height - DESIGN_H * scale) / 2f;

        place(panelImage, scale, offsetX, offsetY, 160, 30, 704, 506);
        place(headerImage, scale, offsetX, offsetY, 362, 492, 300, 61);
        place(tabImages[0], scale, offsetX, offsetY, 270, 412, 150, 42);
        place(tabImages[1], scale, offsetX, offsetY, 436, 412, 150, 42);
        place(tabImages[2], scale, offsetX, offsetY, 602, 412, 152, 42);
        standardTabLabel.setFontScale(scale);
        standardTabLabel.setPosition(
            offsetX + (436 + (150 - standardTabLabel.getPrefWidth() / scale) / 2f) * scale,
            offsetY + (412 + (42 - standardTabLabel.getPrefHeight() / scale) / 2f) * scale);
        place(frameImage, scale, offsetX, offsetY, 277, 130, 470, 249);
        place(previewImage, scale, offsetX, offsetY, 317, 150, 390, 209);
        place(titleImage, scale, offsetX, offsetY, 369, 84, 286, 30);
        titleLabel.setFontScale(scale);
        titleLabel.setPosition(offsetX + (DESIGN_W * scale - titleLabel.getPrefWidth()) / 2f,
            offsetY + 84 * scale + 4 * scale);
        infoLabel.setFontScale(scale);
        infoLabel.setSize(704 * scale, 60 * scale);
        infoLabel.setPosition(offsetX + 160 * scale, offsetY + 26 * scale);
        place(selectButton, scale, offsetX, offsetY, 387, 40, 250, 68);
        place(closeButton, scale, offsetX, offsetY, 816, 496, 36, 35);
    }

    private void place(Image image, float scale, float offsetX, float offsetY,
                       float x, float y, float w, float h) {
        image.setBounds(offsetX + x * scale, offsetY + y * scale, w * scale, h * scale);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.06f, 0.05f, 0.04f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (width > 0 && height > 0 && stage != null) {
            stage.getViewport().update(width, height, true);
            layout(width, height);
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
        disposeTexture(panelTexture);
        disposeTexture(headerTexture);
        disposeTexture(frameTexture);
        disposeTexture(selectTexture);
        disposeTexture(closeTexture);
        disposeTexture(tabClassicTexture);
        disposeTexture(tabClassicInactiveTexture);
        disposeTexture(tabNetherTexture);
        disposeTexture(tabNetherInactiveTexture);
        disposeTexture(titleClassicTexture);
        disposeTexture(titleNetherTexture);
        disposeTexture(classicLandscapeTexture);
        disposeTexture(netherLandscapeTexture);
        disposeTexture(standardBoardTexture);
        disposeTexture(tabBackTexture);
    }

    private void disposeTexture(Texture texture) {
        if (texture != null) {
            texture.dispose();
        }
    }
}
