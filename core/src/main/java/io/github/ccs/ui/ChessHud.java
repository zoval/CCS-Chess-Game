package io.github.ccs.ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

import io.github.ccs.MainGame;
import io.github.ccs.ai_networking.Difficulty;
import io.github.ccs.game_logic.Board;
import io.github.ccs.sound.SoundManager;

/**
 * In-game HUD on {@code hudStage}: wooden player panels above and below the board,
 * settings gear inside the top panel, history nav arrows in the side margins, and
 * RESIGN / DRAW pixel buttons in the right margin.
 */
class ChessHud extends Group implements Disposable {
    static final float PANEL_H = 92f;
    static final float GAP = 6f;
    private static final int NAV_SIZE = 36;
    private static final int SIDE_BUTTON_W = 72;
    private static final int SIDE_BUTTON_H = 26;
    private static final int GEAR_SIZE = 32;

    private final Board board;
    private final MainGame game;
    private final boolean opponentIsAI;
    private final PlayerPanel topPanel;
    private final PlayerPanel bottomPanel;
    private final Image navBack;
    private final Image navForward;
    private final Image resignButton;
    private final Label resignLabel;
    private final Image drawButton;
    private final Label drawLabel;
    private final Image gearButton;
    private final Map<String, Texture> textures = new HashMap<String, Texture>();
    private Texture gearTexture;

    private Runnable navBackAction;
    private Runnable navForwardAction;
    private boolean navBackEnabled = true;
    private boolean navForwardEnabled = true;
    private boolean aiThinking;

    private int lastHistorySize = -1;
    private int lastIndex = -1;
    private boolean lastWhiteTurn;
    private boolean lastOver = true;
    private boolean lastCheckWhite;
    private boolean lastCheckBlack;
    private boolean lastAiThinking;

    ChessHud(Board board, MainGame game, BoardRenderer renderer, SettingsDialog settingsDialog) {
        this.board = board;
        this.game = game;
        this.opponentIsAI = game.getGameMode() == MainGame.GameMode.P_V_AI;

        PlayerPanel.BadgeStyle opponentBadge;
        String opponentName;
        if (opponentIsAI) {
            Difficulty difficulty = game.getDifficulty();
            if (difficulty == Difficulty.EASY) {
                opponentName = "SLIME BOT";
                opponentBadge = PlayerPanel.BadgeStyle.EASY;
            } else if (difficulty == Difficulty.MEDIUM) {
                opponentName = "IRON GOLEM";
                opponentBadge = PlayerPanel.BadgeStyle.MED;
            } else {
                opponentName = "ENDERMAN";
                opponentBadge = PlayerPanel.BadgeStyle.HARD;
            }
        } else {
            opponentName = "PLAYER 2";
            opponentBadge = PlayerPanel.BadgeStyle.P2;
        }

        topPanel = new PlayerPanel(PlayerPanel.Side.OPPONENT, opponentName, opponentBadge,
            renderer.getPieceTextureAccess());
        bottomPanel = new PlayerPanel(PlayerPanel.Side.PLAYER, "PLAYER 1",
            PlayerPanel.BadgeStyle.WHITE, renderer.getPieceTextureAccess());
        addActor(topPanel);
        addActor(bottomPanel);

        navBack = new Image(new TextureRegionDrawable(new TextureRegion(navTexture(true))));
        navBack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!navBackEnabled) {
                    return;
                }
                SoundManager.getInstance().playUIClick();
                if (navBackAction != null) {
                    navBackAction.run();
                }
            }
        });
        addActor(navBack);

        navForward = new Image(new TextureRegionDrawable(new TextureRegion(navTexture(false))));
        navForward.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!navForwardEnabled) {
                    return;
                }
                SoundManager.getInstance().playUIClick();
                if (navForwardAction != null) {
                    navForwardAction.run();
                }
            }
        });
        addActor(navForward);

        resignButton = new Image(new TextureRegionDrawable(new TextureRegion(pixelButton("resign"))));
        resignButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (board.isGameOver() || board.isViewingHistory()) {
                    return;
                }
                SoundManager.getInstance().playUIClick();
                board.resign();
            }
        });
        addActor(resignButton);
        resignLabel = makeLabel("RESIGN");

        drawButton = new Image(new TextureRegionDrawable(new TextureRegion(pixelButton("draw"))));
        drawButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (board.isGameOver() || board.isViewingHistory()) {
                    return;
                }
                SoundManager.getInstance().playUIClick();
                board.agreeDraw();
            }
        });
        drawButton.setVisible(!opponentIsAI);
        addActor(drawButton);
        drawLabel = makeLabel("DRAW");
        drawLabel.setVisible(!opponentIsAI);

        gearTexture = ProceduralTextures.gearIcon(64);
        gearButton = new Image(new TextureRegionDrawable(new TextureRegion(gearTexture)));
        gearButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().playUIClick();
                if (settingsDialog.isOpen()) {
                    settingsDialog.hide();
                } else {
                    settingsDialog.open();
                }
            }
        });
        addActor(gearButton);
    }

    private Label makeLabel(String text) {
        Label label = new Label(text, new Label.LabelStyle(Fonts.tiny(), com.badlogic.gdx.graphics.Color.WHITE));
        addActor(label);
        return label;
    }

    /** Iron-framed wood chip with a gold arrow. */
    private Texture navTexture(boolean left) {
        String key = left ? "navBack" : "navForward";
        Texture cached = textures.get(key);
        if (cached != null) {
            return cached;
        }
        Pixmap pixmap = new Pixmap(NAV_SIZE, NAV_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(new com.badlogic.gdx.graphics.Color(0x1f2227));
        pixmap.fill();
        pixmap.setColor(new com.badlogic.gdx.graphics.Color(0x4a515c));
        pixmap.fillRectangle(1, 1, NAV_SIZE - 2, NAV_SIZE - 2);
        pixmap.setColor(new com.badlogic.gdx.graphics.Color(0x422611));
        pixmap.fillRectangle(2, 2, NAV_SIZE - 4, NAV_SIZE - 4);
        pixmap.setColor(1f, 1f, 1f, 0.14f);
        pixmap.fillRectangle(2, 2, NAV_SIZE - 4, 1);
        pixmap.setColor(new com.badlogic.gdx.graphics.Color(0xffdf40));
        if (left) {
            pixmap.fillTriangle(23, 9, 23, 27, 11, 18);
        } else {
            pixmap.fillTriangle(13, 9, 13, 27, 25, 18);
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        textures.put(key, texture);
        return texture;
    }

    private Texture pixelButton(String key) {
        Texture cached = textures.get(key);
        if (cached != null) {
            return cached;
        }
        Pixmap pixmap = new Pixmap(SIDE_BUTTON_W, SIDE_BUTTON_H, Pixmap.Format.RGBA8888);
        pixmap.setColor(new com.badlogic.gdx.graphics.Color(0x92400e));
        pixmap.fill();
        pixmap.setColor(new com.badlogic.gdx.graphics.Color(0x7c3f14));
        pixmap.fillRectangle(1, 1, SIDE_BUTTON_W - 2, SIDE_BUTTON_H - 2);
        pixmap.setColor(1f, 1f, 1f, 0.18f);
        pixmap.fillRectangle(1, 1, SIDE_BUTTON_W - 2, 1);
        pixmap.setColor(0f, 0f, 0f, 0.35f);
        pixmap.fillRectangle(1, SIDE_BUTTON_H - 3, SIDE_BUTTON_W - 2, 2);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        textures.put(key, texture);
        return texture;
    }

    /** Anchors panels to the board column and places margin buttons; call on show and resize. */
    void layout(float width, float height, float boardX, float boardY, float boardSize) {
        setBounds(0, 0, width, height);
        topPanel.layoutPanel(boardX, boardY + boardSize + GAP, boardSize, PANEL_H);
        bottomPanel.layoutPanel(boardX, 0, boardSize, PANEL_H);

        float centerY = height / 2f - NAV_SIZE / 2f;
        navBack.setBounds(boardX / 2f - NAV_SIZE / 2f, centerY, NAV_SIZE, NAV_SIZE);
        navForward.setBounds(width - boardX / 2f - NAV_SIZE / 2f, centerY, NAV_SIZE, NAV_SIZE);

        float sideX = boardX + boardSize + (boardX - SIDE_BUTTON_W) / 2f;
        float resignY = height / 2f - 70f;
        resignButton.setBounds(sideX, resignY, SIDE_BUTTON_W, SIDE_BUTTON_H);
        resignLabel.setPosition(sideX + SIDE_BUTTON_W / 2f - resignLabel.getPrefWidth() / 2f,
            resignY + SIDE_BUTTON_H / 2f - resignLabel.getPrefHeight() / 2f);
        float drawY = height / 2f - 106f;
        drawButton.setBounds(sideX, drawY, SIDE_BUTTON_W, SIDE_BUTTON_H);
        drawLabel.setPosition(sideX + SIDE_BUTTON_W / 2f - drawLabel.getPrefWidth() / 2f,
            drawY + SIDE_BUTTON_H / 2f - drawLabel.getPrefHeight() / 2f);

        float gearX = boardX + boardSize - GEAR_SIZE - 14f;
        float gearY = boardY + boardSize + GAP + PANEL_H / 2f - GEAR_SIZE / 2f;
        gearButton.setBounds(gearX, gearY, GEAR_SIZE, GEAR_SIZE);
    }

    /** Sets the history nav callbacks (wired by the view-only history mode). */
    void setNavActions(Runnable back, Runnable forward) {
        navBackAction = back;
        navForwardAction = forward;
    }

    /** Grays the nav arrows out at the ends of history or while a move is animating. */
    void setNavEnabled(boolean backEnabled, boolean forwardEnabled) {
        if (navBackEnabled == backEnabled && navForwardEnabled == forwardEnabled) {
            return;
        }
        navBackEnabled = backEnabled;
        navForwardEnabled = forwardEnabled;
        navBack.setColor(navTint(backEnabled));
        navForward.setColor(navTint(forwardEnabled));
    }

    private static com.badlogic.gdx.graphics.Color navTint(boolean enabled) {
        return enabled
            ? com.badlogic.gdx.graphics.Color.WHITE
            : new com.badlogic.gdx.graphics.Color(0.45f, 0.45f, 0.45f, 0.85f);
    }

    /** Lets the AI driver flag thinking state so pills show THINKING... (Phase 10). */
    void setAiThinking(boolean thinking) {
        this.aiThinking = thinking;
    }

    /** Pulses glows and check badges. */
    void tick(float delta) {
        topPanel.tick(delta);
        bottomPanel.tick(delta);
    }

    /** Dirty-checked refresh of pills, glows, captured trays, and material chips. */
    void refreshState() {
        int historySize = board.getHistory().size();
        int index = board.getHistoryIndex();
        boolean whiteTurn = board.isWhiteTurn();
        boolean over = board.isGameOver();
        boolean checkWhite = board.isKingInCheck(true);
        boolean checkBlack = board.isKingInCheck(false);
        if (historySize == lastHistorySize && index == lastIndex && whiteTurn == lastWhiteTurn
            && over == lastOver && checkWhite == lastCheckWhite && checkBlack == lastCheckBlack
            && aiThinking == lastAiThinking) {
            return;
        }
        lastHistorySize = historySize;
        lastIndex = index;
        lastWhiteTurn = whiteTurn;
        lastOver = over;
        lastCheckWhite = checkWhite;
        lastCheckBlack = checkBlack;
        lastAiThinking = aiThinking;
        topPanel.update(board, opponentIsAI, aiThinking);
        bottomPanel.update(board, false, false);
    }

    /** Updates both countdown labels; sub-minute times render red. */
    void setTimers(long whiteMillis, long blackMillis) {
        setTimer(bottomPanel, whiteMillis);
        setTimer(topPanel, blackMillis);
    }

    private void setTimer(PlayerPanel panel, long millis) {
        long total = Math.max(0, millis);
        long minutes = total / 60000L;
        long seconds = (total / 1000L) % 60L;
        String text = minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
        panel.setTimer(text, total < 60000L);
    }

    @Override
    public void dispose() {
        topPanel.dispose();
        bottomPanel.dispose();
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();
        if (gearTexture != null) {
            gearTexture.dispose();
            gearTexture = null;
        }
    }
}
