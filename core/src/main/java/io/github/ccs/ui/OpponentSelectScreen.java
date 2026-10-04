package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.ccs.MainGame;
import io.github.ccs.ai_networking.Difficulty;
import io.github.ccs.backend.SaveGame;
import io.github.ccs.game_logic.GameState;
import io.github.ccs.sound.SoundManager;

/**
 * Opponent selection screen behind PLAY: PvP / VS A.I. tiles, a time-control row,
 * START MATCH, and BACK TO MENU. VS A.I. opens a NEW GAME / LOAD choice, then the
 * difficulty modal. All layout lives in a 1024x572 design space scaled to the window.
 */
public class OpponentSelectScreen extends ScreenAdapter {
    private static final float DESIGN_W = 1024f;
    private static final float DESIGN_H = 572f;

    private static final Color TEXT_COLOR = new Color(0.96f, 0.93f, 0.86f, 1f);
    private static final Color DIM_TEXT_COLOR = new Color(0.62f, 0.56f, 0.44f, 1f);
    private static final Color HEADER_COLOR = new Color(0.98f, 0.80f, 0.08f, 1f);
    private static final Color START_COLOR = new Color(0.29f, 0.87f, 0.50f, 1f);
    private static final Color START_DISABLED_COLOR = new Color(0.45f, 0.45f, 0.45f, 1f);
    private static final Color EASY_COLOR = new Color(0.53f, 0.94f, 0.68f, 1f);
    private static final Color MEDIUM_COLOR = new Color(0.99f, 0.88f, 0.28f, 1f);
    private static final Color HARD_COLOR = new Color(0.97f, 0.44f, 0.44f, 1f);
    private static final Color TOAST_COLOR = new Color(0.95f, 0.45f, 0.40f, 1f);
    private static final Color DISABLED_TINT = new Color(0.55f, 0.55f, 0.55f, 1f);

    private static final Color BTN_BASE = new Color(0.553f, 0.431f, 0.388f, 1f);
    private static final Color BTN_BORDER = new Color(0.306f, 0.204f, 0.18f, 1f);
    private static final Color PANEL_BASE = new Color(0.361f, 0.251f, 0.2f, 1f);
    private static final Color PANEL_BORDER = new Color(0.243f, 0.153f, 0.137f, 1f);
    private static final Color OVERLAY_COLOR = new Color(0f, 0f, 0f, 0.72f);

    private static final int[] TIME_CHOICES = {1, 3, 5, 10};
    private static final Difficulty[] DIFFICULTIES = {Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD};
    private static final String[] DIFFICULTY_SUBTITLES = {
        "For beginners.", "A fair challenge.", "Prepare to lose."};
    private static final Color[] DIFFICULTY_COLORS = {EASY_COLOR, MEDIUM_COLOR, HARD_COLOR};

    private final MainGame game;
    private final Map<String, Texture> textures = new HashMap<String, Texture>();
    private final List<Label> allLabels = new ArrayList<Label>();

    private Stage stage;
    private float scale = 1f;
    private float offsetX;
    private float offsetY;

    private Image panelImage;
    private Label headerLabel;
    private Label timeCaptionLabel;
    private Image pvpTile;
    private Image aiTile;
    private Label pvpTitleLabel;
    private Label pvpSubLabel;
    private Label aiTitleLabel;
    private Label aiSubLabel;
    private final Image[] timeButtons = new Image[TIME_CHOICES.length];
    private final Label[] timeLabels = new Label[TIME_CHOICES.length];
    private Image startButton;
    private Label startLabel;
    private Image backButton;
    private Label backLabel;
    private Image knightWhitePvp;
    private Image knightBlackPvp;
    private Image knightWhiteAi;
    private Image kingBlackAi;
    private Texture whiteKnightTexture;
    private Texture blackKnightTexture;
    private Texture blackKingTexture;

    private Image overlayChoice;
    private Image overlayDifficulty;
    private Group choiceGroup;
    private Group difficultyGroup;
    private Image choicePanelImage;
    private Label choiceHeaderLabel;
    private Image newGameButton;
    private Label newGameLabel;
    private Image loadButton;
    private Label loadLabel;
    private Label loadHint;
    private Image choiceCancelButton;
    private Label choiceCancelLabel;
    private Texture choicePanelTexture;
    private Image modalPanelImage;
    private Label difficultyHeaderLabel;
    private final Image[] difficultyRows = new Image[DIFFICULTIES.length];
    private final Label[] difficultyNameLabels = new Label[DIFFICULTIES.length];
    private final Label[] difficultySubLabels = new Label[DIFFICULTIES.length];
    private Image difficultyCancelButton;
    private Label difficultyCancelLabel;

    private Group toastGroup;
    private Image toastChipImage;
    private Label toastLabel;

    private MainGame.GameMode chosenMode;
    private Difficulty chosenDifficulty;
    private int chosenMinutes = 10;
    private boolean startEnabled;

    public OpponentSelectScreen(MainGame game) {
        this.game = game;
        this.chosenMinutes = game.getTimeControlMinutes();
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        buildUi();
        installInputHandling();
        Gdx.input.setInputProcessor(stage);
        if (!Gdx.graphics.isFullscreen()
            && (Gdx.graphics.getWidth() != MainGame.MENU_WINDOW_WIDTH
                || Gdx.graphics.getHeight() != MainGame.MENU_WINDOW_HEIGHT)) {
            Gdx.graphics.setWindowedMode(MainGame.MENU_WINDOW_WIDTH, MainGame.MENU_WINDOW_HEIGHT);
        }
    }

    private void buildUi() {
        buildMainUi();
        buildChoiceModal();
        buildDifficultyModal();
        buildToast();

        refresh();
        layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /** Generates (or reuses) a flat pixel-button texture with beveled edges. */
    private Texture tex(String key, int w, int h, Color border, Color base, boolean pressed) {
        Texture cached = textures.get(key);
        if (cached != null) {
            return cached;
        }
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(border);
        pixmap.fill();
        // Flat overlays (w/h <= 8) must stay uniform: degenerate fillRectangle calls
        // at this size still rasterize and paint opaque texels into the fill color.
        if (w > 8 && h > 8) {
            Color fill = pressed ? new Color(base.r * 0.62f, base.g * 0.62f, base.b * 0.62f, 1f) : base;
            pixmap.setColor(fill);
            pixmap.fillRectangle(4, 4, w - 8, h - 8);
            Color light = new Color(Math.min(1f, fill.r + 0.14f), Math.min(1f, fill.g + 0.14f),
                Math.min(1f, fill.b + 0.14f), 1f);
            Color dark = new Color(fill.r * 0.7f, fill.g * 0.7f, fill.b * 0.7f, 1f);
            // Pressed swaps the bevel so the button looks inset.
            Color bevelTop = pressed ? dark : light;
            Color bevelBottom = pressed ? light : dark;
            pixmap.setColor(bevelTop);
            pixmap.fillRectangle(4, 4, w - 8, 3);
            pixmap.fillRectangle(4, 4, 3, h - 8);
            pixmap.setColor(bevelBottom);
            pixmap.fillRectangle(4, h - 7, w - 8, 3);
            pixmap.fillRectangle(w - 7, 4, 3, h - 8);
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap.dispose();
        textures.put(key, texture);
        return texture;
    }

    private Texture artTexture(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    private Label makeLabel(Group parent, String text, BitmapFont font, Color color) {
        Label label = new Label(text, new Label.LabelStyle(font, color));
        parent.addActor(label);
        allLabels.add(label);
        return label;
    }

    private Image makeImage(Group parent, Texture texture) {
        Image image = new Image(new TextureRegionDrawable(new TextureRegion(texture)));
        parent.addActor(image);
        return image;
    }

    private ClickListener click(final Runnable action) {
        return new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                action.run();
            }
        };
    }

    private void buildMainUi() {
        Group root = stage.getRoot();
        Texture mainPanel = tex("panel", 740, 510, PANEL_BORDER, PANEL_BASE, false);
        Texture tileNormal = tex("tile", 300, 170, BTN_BORDER, BTN_BASE, false);
        Texture tilePressed = tex("tileP", 300, 170, BTN_BORDER, BTN_BASE, true);
        Texture timeNormal = tex("time", 110, 40, BTN_BORDER, BTN_BASE, false);
        Texture timePressed = tex("timeP", 110, 40, BTN_BORDER, BTN_BASE, true);
        Texture startNormal = tex("start", 320, 56, BTN_BORDER, BTN_BASE, false);
        Texture backNormal = tex("back", 280, 42, BTN_BORDER, BTN_BASE, false);

        panelImage = makeImage(root, mainPanel);
        headerLabel = makeLabel(root, "CHOOSE OPPONENT", Fonts.title(), HEADER_COLOR);

        pvpTile = makeImage(root, tileNormal);
        whiteKnightTexture = artTexture("pieces/white_mc/white_horse[front]-64x64.png");
        blackKnightTexture = artTexture("pieces/black_mc/Black_horse[front]-64x64 (2).png");
        blackKingTexture = artTexture("pieces/black_mc/Black_King[front]-64x64 (2).png");
        knightWhitePvp = makeImage(root, whiteKnightTexture);
        knightBlackPvp = makeImage(root, blackKnightTexture);
        pvpTitleLabel = makeLabel(root, "PLAYER VS PLAYER", Fonts.medium(), TEXT_COLOR);
        pvpSubLabel = makeLabel(root, "Local Multiplayer", Fonts.tiny(), DIM_TEXT_COLOR);

        aiTile = makeImage(root, tileNormal);
        knightWhiteAi = makeImage(root, whiteKnightTexture);
        kingBlackAi = makeImage(root, blackKingTexture);
        aiTitleLabel = makeLabel(root, "PLAYER VS A.I.", Fonts.medium(), TEXT_COLOR);
        aiSubLabel = makeLabel(root, "Single Player", Fonts.tiny(), DIM_TEXT_COLOR);

        timeCaptionLabel = makeLabel(root, "TIME CONTROL", Fonts.tiny(), DIM_TEXT_COLOR);
        for (int i = 0; i < TIME_CHOICES.length; i++) {
            final int minutes = TIME_CHOICES[i];
            timeButtons[i] = makeImage(root, timeNormal);
            timeLabels[i] = makeLabel(root, minutes + " MIN", Fonts.small(), TEXT_COLOR);
            timeButtons[i].addListener(click(new Runnable() {
                @Override
                public void run() {
                    SoundManager.getInstance().playUIClick();
                    chosenMinutes = minutes;
                    refresh();
                }
            }));
        }

        startButton = makeImage(root, startNormal);
        startLabel = makeLabel(root, "START MATCH", Fonts.medium(), START_DISABLED_COLOR);
        startButton.addListener(click(new Runnable() {
            @Override
            public void run() {
                if (startEnabled) {
                    startMatch();
                }
            }
        }));

        backButton = makeImage(root, backNormal);
        backLabel = makeLabel(root, "BACK TO MENU", Fonts.small(), TEXT_COLOR);
        backButton.addListener(click(new Runnable() {
            @Override
            public void run() {
                backToMenu();
            }
        }));

        pvpTile.addListener(click(new Runnable() {
            @Override
            public void run() {
                SoundManager.getInstance().playUIClick();
                chosenMode = MainGame.GameMode.P_V_P;
                chosenDifficulty = null;
                refresh();
            }
        }));
        aiTile.addListener(click(new Runnable() {
            @Override
            public void run() {
                SoundManager.getInstance().playUIClick();
                overlayChoice.setVisible(true);
                choiceGroup.setVisible(true);
            }
        }));
    }

    private void buildChoiceModal() {
        Group root = stage.getRoot();
        Texture overlay = tex("overlay", 8, 8, OVERLAY_COLOR, OVERLAY_COLOR, false);
        overlayChoice = makeImage(root, overlay);
        overlayChoice.setVisible(false);
        overlayChoice.addListener(click(new Runnable() {
            @Override
            public void run() {
                overlayChoice.setVisible(false);
                choiceGroup.setVisible(false);
            }
        }));

        choiceGroup = new Group();
        choiceGroup.setVisible(false);
        root.addActor(choiceGroup);

        choicePanelTexture = artTexture("save_load/panel.png");
        choicePanelImage = new Image(new TextureRegionDrawable(
            new TextureRegion(choicePanelTexture, 50, 52, 1158, 624)));
        choiceGroup.addActor(choicePanelImage);

        choiceHeaderLabel = makeLabel(choiceGroup, "NEW GAME OR LOAD?", Fonts.medium(), HEADER_COLOR);

        Texture midNormal = tex("mid", 360, 60, BTN_BORDER, BTN_BASE, false);
        Texture cancelNormal = tex("cancel", 220, 42, BTN_BORDER, BTN_BASE, false);

        newGameButton = makeImage(choiceGroup, midNormal);
        newGameLabel = makeLabel(choiceGroup, "NEW GAME", Fonts.medium(), TEXT_COLOR);
        loadButton = makeImage(choiceGroup, midNormal);
        loadLabel = makeLabel(choiceGroup, "LOAD GAME", Fonts.medium(), TEXT_COLOR);
        loadHint = makeLabel(choiceGroup, "Resume your saved match", Fonts.tiny(), DIM_TEXT_COLOR);
        choiceCancelButton = makeImage(choiceGroup, cancelNormal);
        choiceCancelLabel = makeLabel(choiceGroup, "CANCEL", Fonts.small(), TEXT_COLOR);

        newGameButton.addListener(click(new Runnable() {
            @Override
            public void run() {
                SoundManager.getInstance().playUIClick();
                overlayChoice.setVisible(false);
                choiceGroup.setVisible(false);
                overlayDifficulty.setVisible(true);
                difficultyGroup.setVisible(true);
            }
        }));
        loadButton.addListener(click(new Runnable() {
            @Override
            public void run() {
                loadSavedGame();
            }
        }));
        choiceCancelButton.addListener(click(new Runnable() {
            @Override
            public void run() {
                SoundManager.getInstance().playUIClick();
                overlayChoice.setVisible(false);
                choiceGroup.setVisible(false);
            }
        }));
    }

    private void buildDifficultyModal() {
        Group root = stage.getRoot();
        Texture overlay = tex("overlay", 8, 8, OVERLAY_COLOR, OVERLAY_COLOR, false);
        overlayDifficulty = makeImage(root, overlay);
        overlayDifficulty.setVisible(false);
        overlayDifficulty.addListener(click(new Runnable() {
            @Override
            public void run() {
                overlayDifficulty.setVisible(false);
                difficultyGroup.setVisible(false);
            }
        }));

        difficultyGroup = new Group();
        difficultyGroup.setVisible(false);
        root.addActor(difficultyGroup);

        Texture modalPanel = tex("modalPanel", 480, 400, PANEL_BORDER, PANEL_BASE, false);
        Texture rowNormal = tex("row", 400, 78, BTN_BORDER, BTN_BASE, false);
        Texture cancelNormal = tex("cancel", 220, 42, BTN_BORDER, BTN_BASE, false);

        modalPanelImage = makeImage(difficultyGroup, modalPanel);
        difficultyHeaderLabel = makeLabel(difficultyGroup, "SELECT A.I. DIFFICULTY", Fonts.medium(), HEADER_COLOR);

        for (int i = 0; i < DIFFICULTIES.length; i++) {
            final Difficulty difficulty = DIFFICULTIES[i];
            difficultyRows[i] = makeImage(difficultyGroup, rowNormal);
            difficultyNameLabels[i] = makeLabel(difficultyGroup, difficulty.name(),
                Fonts.medium(), DIFFICULTY_COLORS[i]);
            difficultySubLabels[i] = makeLabel(difficultyGroup, DIFFICULTY_SUBTITLES[i],
                Fonts.tiny(), DIM_TEXT_COLOR);
            difficultyRows[i].addListener(click(new Runnable() {
                @Override
                public void run() {
                    SoundManager.getInstance().playUIClick();
                    chosenMode = MainGame.GameMode.P_V_AI;
                    chosenDifficulty = difficulty;
                    overlayDifficulty.setVisible(false);
                    difficultyGroup.setVisible(false);
                    refresh();
                }
            }));
        }

        difficultyCancelButton = makeImage(difficultyGroup, cancelNormal);
        difficultyCancelLabel = makeLabel(difficultyGroup, "CANCEL", Fonts.small(), TEXT_COLOR);
        difficultyCancelButton.addListener(click(new Runnable() {
            @Override
            public void run() {
                SoundManager.getInstance().playUIClick();
                overlayDifficulty.setVisible(false);
                difficultyGroup.setVisible(false);
            }
        }));
    }

    private void buildToast() {
        toastGroup = new Group();
        toastGroup.setVisible(false);
        stage.getRoot().addActor(toastGroup);

        Texture chip = tex("chip", 280, 44, PANEL_BORDER, PANEL_BASE, false);
        toastChipImage = new Image(new TextureRegionDrawable(new TextureRegion(chip)));
        toastGroup.addActor(toastChipImage);
        toastLabel = new Label("NO SAVED GAME", new Label.LabelStyle(Fonts.small(), TOAST_COLOR));
        toastGroup.addActor(toastLabel);
        allLabels.add(toastLabel);
    }

    private void showToast(String message) {
        toastLabel.setText(message);
        toastGroup.clearActions();
        toastGroup.setVisible(true);
        toastGroup.addAction(Actions.sequence(Actions.delay(1.6f), Actions.visible(false)));
    }

    /** Syncs tile/button visuals and START availability with the current selection. */
    private void refresh() {
        Texture tileNormal = tex("tile", 300, 170, BTN_BORDER, BTN_BASE, false);
        Texture tilePressed = tex("tileP", 300, 170, BTN_BORDER, BTN_BASE, true);
        Texture timeNormal = tex("time", 110, 40, BTN_BORDER, BTN_BASE, false);
        Texture timePressed = tex("timeP", 110, 40, BTN_BORDER, BTN_BASE, true);

        boolean pvpSelected = chosenMode == MainGame.GameMode.P_V_P;
        boolean aiSelected = chosenMode == MainGame.GameMode.P_V_AI;
        pvpTile.setDrawable(new TextureRegionDrawable(new TextureRegion(
            pvpSelected ? tilePressed : tileNormal)));
        aiTile.setDrawable(new TextureRegionDrawable(new TextureRegion(
            aiSelected ? tilePressed : tileNormal)));
        aiTitleLabel.setText(aiSelected && chosenDifficulty != null
            ? "VS A.I. (" + chosenDifficulty.name() + ")" : "PLAYER VS A.I.");

        for (int i = 0; i < TIME_CHOICES.length; i++) {
            boolean selected = TIME_CHOICES[i] == chosenMinutes;
            timeButtons[i].setDrawable(new TextureRegionDrawable(new TextureRegion(
                selected ? timePressed : timeNormal)));
        }

        startEnabled = chosenMode != null
            && (chosenMode == MainGame.GameMode.P_V_P || chosenDifficulty != null);
        startButton.setColor(startEnabled ? Color.WHITE : DISABLED_TINT);
        startLabel.setColor(startEnabled ? START_COLOR : START_DISABLED_COLOR);
    }

    private void startMatch() {
        SoundManager.getInstance().playUIClick();
        game.setGameMode(chosenMode);
        if (chosenMode == MainGame.GameMode.P_V_AI && chosenDifficulty != null) {
            game.setDifficulty(chosenDifficulty);
        }
        game.setTimeControlMinutes(chosenMinutes);
        GameState pending = game.consumePendingLoad();
        if (!Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(MainGame.GAME_WINDOW_WIDTH, MainGame.GAME_WINDOW_HEIGHT);
        }
        game.setScreen(pending != null ? new ChessGameScreen(game, pending) : new ChessGameScreen(game));
    }

    private void loadSavedGame() {
        GameState state = SaveGame.read();
        if (state == null) {
            showToast("NO SAVED GAME");
            return;
        }
        SoundManager.getInstance().playUIClick();
        if (!Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(MainGame.GAME_WINDOW_WIDTH, MainGame.GAME_WINDOW_HEIGHT);
        }
        game.setScreen(new ChessGameScreen(game, state));
    }

    private void backToMenu() {
        SoundManager.getInstance().playUIClick();
        game.setScreen(new FirstScreen(game));
    }

    private void installInputHandling() {
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    if (difficultyGroup.isVisible()) {
                        overlayDifficulty.setVisible(false);
                        difficultyGroup.setVisible(false);
                    } else if (choiceGroup.isVisible()) {
                        overlayChoice.setVisible(false);
                        choiceGroup.setVisible(false);
                    } else {
                        backToMenu();
                    }
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
        scale = Math.min(width / DESIGN_W, height / DESIGN_H);
        offsetX = (width - DESIGN_W * scale) / 2f;
        offsetY = (height - DESIGN_H * scale) / 2f;

        for (int i = 0; i < allLabels.size(); i++) {
            allLabels.get(i).setFontScale(scale);
        }

        place(panelImage, 142, 25, 740, 510);
        center(headerLabel, 512, 472);

        place(pvpTile, 182, 250, 300, 170);
        place(knightWhitePvp, 248, 340, 56, 56);
        place(knightBlackPvp, 360, 340, 56, 56);
        center(pvpTitleLabel, 332, 302);
        center(pvpSubLabel, 332, 276);

        place(aiTile, 542, 250, 300, 170);
        place(knightWhiteAi, 608, 340, 56, 56);
        place(kingBlackAi, 720, 340, 56, 56);
        center(aiTitleLabel, 692, 302);
        center(aiSubLabel, 692, 276);

        center(timeCaptionLabel, 512, 222);
        for (int i = 0; i < TIME_CHOICES.length; i++) {
            float buttonX = 256 + i * 134;
            place(timeButtons[i], buttonX, 162, 110, 40);
            center(timeLabels[i], buttonX + 55, 174);
        }

        place(startButton, 352, 92, 320, 56);
        center(startLabel, 512, 108);
        place(backButton, 372, 34, 280, 42);
        center(backLabel, 512, 45);

        overlayChoice.setBounds(0, 0, width, height);
        choiceGroup.setBounds(offsetX, offsetY, DESIGN_W * scale, DESIGN_H * scale);
        place(choicePanelImage, 192, 114, 640, 345);
        center(choiceHeaderLabel, 512, 400);
        place(newGameButton, 332, 288, 360, 60);
        center(newGameLabel, 512, 306);
        place(loadButton, 332, 210, 360, 60);
        center(loadLabel, 512, 228);
        center(loadHint, 512, 194);
        place(choiceCancelButton, 412, 148, 220, 42);
        center(choiceCancelLabel, 512, 158);

        overlayDifficulty.setBounds(0, 0, width, height);
        difficultyGroup.setBounds(offsetX, offsetY, DESIGN_W * scale, DESIGN_H * scale);
        place(modalPanelImage, 272, 86, 480, 400);
        center(difficultyHeaderLabel, 512, 436);
        float[] rowY = {322, 232, 142};
        for (int i = 0; i < DIFFICULTIES.length; i++) {
            place(difficultyRows[i], 312, rowY[i], 400, 78);
            center(difficultyNameLabels[i], 512, rowY[i] + 44);
            center(difficultySubLabels[i], 512, rowY[i] + 18);
        }
        place(difficultyCancelButton, 402, 96, 220, 42);
        center(difficultyCancelLabel, 512, 106);

        toastGroup.setBounds(offsetX + 372 * scale, offsetY + 268 * scale, 280 * scale, 44 * scale);
        toastChipImage.setBounds(0, 0, 280 * scale, 44 * scale);
        toastLabel.setPosition(140 * scale - toastLabel.getPrefWidth() / 2f, 16 * scale);
    }

    /** Places an actor at design coordinates; accounts for design-space group parents. */
    private void place(Actor actor, float x, float y, float w, float h) {
        Group parent = actor.getParent();
        boolean inDesignGroup = parent instanceof Group && parent != stage.getRoot();
        actor.setBounds(inDesignGroup ? x * scale : offsetX + x * scale,
            inDesignGroup ? y * scale : offsetY + y * scale,
            w * scale, h * scale);
    }

    /** Centers a label horizontally at design x, baseline-ish at design y. */
    private void center(Label label, float cx, float y) {
        Group parent = label.getParent();
        boolean inDesignGroup = parent instanceof Group && parent != stage.getRoot();
        float px = inDesignGroup ? 0 : offsetX;
        float py = inDesignGroup ? 0 : offsetY;
        label.setPosition(px + cx * scale - label.getPrefWidth() / 2f, py + y * scale);
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
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();
        disposeTexture(whiteKnightTexture);
        disposeTexture(blackKnightTexture);
        disposeTexture(blackKingTexture);
        disposeTexture(choicePanelTexture);
    }

    private void disposeTexture(Texture texture) {
        if (texture != null) {
            texture.dispose();
        }
    }
}
