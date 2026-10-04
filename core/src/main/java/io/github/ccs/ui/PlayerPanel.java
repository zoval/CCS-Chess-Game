package io.github.ccs.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.ccs.game_logic.Board;
import io.github.ccs.game_logic.Piece;

/**
 * One wooden HUD panel (opponent or player) styled after the Ingame_UI mockup:
 * iron-trimmed wood plank, pixel avatar, name + difficulty badge, captured tray
 * with material chip, status pill, timer box, turn glow, and CHECK badge.
 */
class PlayerPanel extends Group implements Disposable {
    enum Side { OPPONENT, PLAYER }

    enum BadgeStyle {EASY, MED, HARD, P2, WHITE}

    /** Supplies the theme piece textures for the captured tray (shared with BoardRenderer). */
    interface PieceTextures {
        Texture get(int signedPiece);
    }

    private static final Color WOOD_TOP = rgb(0x5b371b);
    private static final Color WOOD_BOTTOM = rgb(0x422611);
    private static final Color IRON_DARK = rgb(0x1f2227);
    private static final Color IRON_MID = rgb(0x4a515c);
    private static final Color BRACKET_DARK = rgb(0x1a1d20);
    private static final Color BRACKET_LIGHT = rgb(0x9aa4b2);
    private static final Color AVATAR_BG = rgb(0x180f08);
    private static final Color AVATAR_BORDER = rgb(0x2e1c0d);
    private static final Color TRAY_BORDER = rgb(0x2c1a0e);
    private static final Color TRAY_FILL = new Color(18 / 255f, 10 / 255f, 5 / 255f, 0.85f);
    private static final Color GLOW_GREEN = rgb(0x22c55e);
    private static final Color GLOW_RED = rgb(0xef4444);
    private static final Color CHECK_RED = rgb(0xff1100);

    private static final Color PILL_TURN_BG = rgb(0x022c22);
    private static final Color PILL_TURN_BORDER = rgb(0x10b981);
    private static final Color PILL_TURN_TEXT = rgb(0x6ee7b7);
    private static final Color PILL_WAIT_BG = new Color(0x1c1917);
    private static final Color PILL_WAIT_BORDER = rgb(0x44403c);
    private static final Color PILL_WAIT_TEXT = rgb(0xd6d3d1);
    private static final Color PILL_BUSY_BG = rgb(0x4c0519);
    private static final Color PILL_BUSY_BORDER = rgb(0xf43f5e);
    private static final Color PILL_BUSY_TEXT = rgb(0xfda4af);

    private static final Color TIMER_TEXT = rgb(0xe7e5e4);
    private static final Color TIMER_BORDER = rgb(0x292524);
    private static final Color TIMER_LOW = rgb(0xf87171);
    private static final Color CAPTURED_LABEL = rgb(0xa8a29e);

    private static final Color OPP_NAME_COLOR = rgb(0xfde047);
    private static final Color PLAYER_NAME_COLOR = rgb(0x6ee7b7);

    /** 16x16 pixel avatars: {x, y, w, h, rgb} rows straight from the mockup SVGs. */
    private static final int[][] AVATAR_SLIME = {
        {2, 2, 12, 12, 0x5c8e32}, {3, 3, 10, 10, 0x7ebd3e}, {4, 6, 2, 2, 0x1b2e0b},
        {10, 6, 2, 2, 0x1b2e0b}, {6, 10, 4, 2, 0x2d4f13}};
    private static final int[][] AVATAR_GOLEM = {
        {3, 2, 10, 11, 0xc5bcb5}, {5, 3, 6, 8, 0xdad2cb}, {5, 6, 2, 2, 0x9e3027},
        {9, 6, 2, 2, 0x9e3027}, {7, 8, 2, 4, 0xa49387}, {4, 11, 8, 2, 0x71675f}};
    private static final int[][] AVATAR_ENDERMAN = {
        {2, 2, 12, 12, 0x121212}, {3, 7, 3, 1, 0xbf40bf}, {4, 7, 1, 1, 0xe599f7},
        {10, 7, 3, 1, 0xbf40bf}, {11, 7, 1, 1, 0xe599f7}, {4, 10, 8, 1, 0x000000}};
    private static final int[][] AVATAR_P2 = {
        {3, 2, 10, 11, 0xd27d3b}, {4, 3, 8, 9, 0xe0a37e}, {3, 2, 10, 3, 0xcf6626},
        {4, 6, 2, 2, 0x2b6b55}, {10, 6, 2, 2, 0x2b6b55}, {6, 9, 4, 1, 0x94533a}};
    private static final int[][] AVATAR_PLAYER = {
        {3, 1, 10, 9, 0x718296}, {4, 2, 8, 3, 0x8ea2ba}, {4, 5, 2, 3, 0x2d3748},
        {10, 5, 2, 3, 0x2d3748}, {6, 5, 4, 2, 0x20252e}, {5, 7, 6, 1, 0x4a5568},
        {2, 10, 12, 5, 0x4a5568}, {5, 10, 6, 5, 0x3182ce}, {7, 11, 2, 4, 0x63b3ed}};

    /** @return the avatar rect data for the given opponent flavor. */
    static int[][] avatarFor(BadgeStyle badge) {
        switch (badge) {
            case EASY:
                return AVATAR_SLIME;
            case MED:
                return AVATAR_GOLEM;
            case HARD:
                return AVATAR_ENDERMAN;
            case P2:
                return AVATAR_P2;
            default:
                return AVATAR_PLAYER;
        }
    }

    /** @return badge text for the given opponent flavor. */
    static String badgeTextFor(BadgeStyle badge) {
        switch (badge) {
            case EASY:
                return "EASY AI";
            case MED:
                return "MED AI";
            case HARD:
                return "HARD AI";
            case P2:
                return "LOCAL P2";
            default:
                return "WHITE PIECES";
        }
    }

    private static Color rgb(int hex) {
        return new Color((hex >> 16 & 0xff) / 255f, (hex >> 8 & 0xff) / 255f, (hex & 0xff) / 255f, 1f);
    }

    private final Side side;
    private final PieceTextures pieceTextures;
    private final Map<String, Texture> textures = new HashMap<String, Texture>();
    private final List<Image> capturedIcons = new ArrayList<Image>();
    private final Comparator<Integer> captureOrder = new Comparator<Integer>() {
        @Override
        public int compare(Integer a, Integer b) {
            return Piece.typeOf(b) - Piece.typeOf(a);
        }
    };

    private Image panelImage;
    private Image glowImage;
    private Image avatarFrame;
    private Image avatarImage;
    private Image checkChip;
    private Label checkLabel;
    private Label nameLabel;
    private Image badgeChip;
    private Label badgeLabel;
    private Image trayImage;
    private Label capturedLabel;
    private Group capturedGroup;
    private Image scoreChip;
    private Label scoreLabel;
    private Image pillImage;
    private Label pillLabel;
    private Image timerImage;
    private Label timerLabel;

    private Texture panelTexture;
    private TextureRegionDrawable pillTurnDrawable;
    private TextureRegionDrawable pillWaitDrawable;
    private TextureRegionDrawable pillBusyDrawable;

    private float time;
    private float panelX;
    private float panelY;
    private int builtWidth = -1;
    private int builtHeight = -1;
    private int lastCaptureHash = Integer.MIN_VALUE;
    private Color glowColor = GLOW_GREEN;
    private boolean checkPulse;
    private boolean checkVisible;

    PlayerPanel(Side side, String name, BadgeStyle badge, PieceTextures pieceTextures) {
        this.side = side;
        this.pieceTextures = pieceTextures;

        Texture glow = buildGlowTexture();
        glowImage = new Image(new TextureRegionDrawable(new TextureRegion(glow)));
        addActor(glowImage);

        panelImage = new Image();
        addActor(panelImage);

        avatarFrame = new Image(new TextureRegionDrawable(new TextureRegion(buildAvatarFrame())));
        addActor(avatarFrame);

        avatarImage = new Image(new TextureRegionDrawable(new TextureRegion(avatarTexture(PlayerPanel.avatarFor(badge)))));
        addActor(avatarImage);

        Color[] badgeColors = badgeColors(badge);
        badgeChip = new Image(new TextureRegionDrawable(new TextureRegion(
            solid("badge", 24, 15, badgeColors[1], badgeColors[0]))));
        addActor(badgeChip);
        badgeLabel = makeLabel(badgeTextFor(badge), Fonts.tiny(), badgeColors[2]);

        nameLabel = makeLabel(name, Fonts.medium(), side == Side.PLAYER ? PLAYER_NAME_COLOR : OPP_NAME_COLOR);

        checkChip = new Image(new TextureRegionDrawable(new TextureRegion(
            solid("check", 52, 14, Color.WHITE, CHECK_RED))));
        addActor(checkChip);
        checkLabel = makeLabel("CHECK", Fonts.tiny(), Color.WHITE);

        trayImage = new Image(new TextureRegionDrawable(new TextureRegion(buildTray())));
        addActor(trayImage);
        capturedLabel = makeLabel("CAPTURED:", Fonts.tiny(), CAPTURED_LABEL);
        capturedGroup = new Group();
        addActor(capturedGroup);

        Color scoreBorder = side == Side.PLAYER ? rgb(0x065f46) : rgb(0x92400e);
        Color scoreBg = side == Side.PLAYER ? rgb(0x022c22) : rgb(0x451a03);
        Color scoreText = side == Side.PLAYER ? rgb(0x34d399) : rgb(0xfbbf24);
        scoreChip = new Image(new TextureRegionDrawable(new TextureRegion(
            solid("score", 44, 20, scoreBorder, scoreBg))));
        addActor(scoreChip);
        scoreLabel = makeLabel("", Fonts.tiny(), scoreText);

        pillTurnDrawable = pillDrawable("pillTurn", PILL_TURN_BORDER, PILL_TURN_BG);
        pillWaitDrawable = pillDrawable("pillWait", PILL_WAIT_BORDER, PILL_WAIT_BG);
        pillBusyDrawable = pillDrawable("pillBusy", PILL_BUSY_BORDER, PILL_BUSY_BG);
        pillImage = new Image(pillWaitDrawable);
        addActor(pillImage);
        pillLabel = makeLabel("WAITING", Fonts.tiny(), PILL_WAIT_TEXT);

        timerImage = new Image(new TextureRegionDrawable(new TextureRegion(
            translucent("timer", 94, 24, TIMER_BORDER, 0, 0, 0, 0.45f))));
        addActor(timerImage);
        timerLabel = makeLabel("", Fonts.small(), TIMER_TEXT);
    }

    private Label makeLabel(String text, BitmapFont font, Color color) {
        Label label = new Label(text, new Label.LabelStyle(font, color));
        addActor(label);
        return label;
    }

    private Texture solid(String key, int w, int h, Color border, Color fill) {
        return cached(key, w, h, border, fill, 1f, 1f, 1f);
    }

    private Texture translucent(String key, int w, int h, Color border, float r, float g, float b, float a) {
        return cached(key, w, h, border, new Color(r, g, b, a), 1f, 1f, 1f);
    }

    private Texture cached(String key, int w, int h, Color border, Color fill, float br, float bg, float bb) {
        Texture texture = textures.get(key);
        if (texture != null) {
            return texture;
        }
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(border);
        pixmap.fill();
        pixmap.setColor(fill);
        pixmap.fillRectangle(1, 1, w - 2, h - 2);
        pixmap.setColor(br, bg, bb, 0.25f);
        pixmap.fillRectangle(1, 1, w - 2, 1);
        texture = new Texture(pixmap);
        pixmap.dispose();
        textures.put(key, texture);
        return texture;
    }

    private TextureRegionDrawable pillDrawable(String key, Color border, Color bg) {
        return new TextureRegionDrawable(new TextureRegion(solid(key, 118, 24, border, bg)));
    }

    private Color[] badgeColors(BadgeStyle badge) {
        switch (badge) {
            case EASY:
                return new Color[]{rgb(0x022c22), rgb(0x059669), rgb(0x6ee7b7)};
            case MED:
                return new Color[]{rgb(0x451a03), rgb(0xd97706), rgb(0xfcd34d)};
            case HARD:
                return new Color[]{rgb(0x3b0764), rgb(0x9333ea), rgb(0xd8b4fe)};
            case P2:
                return new Color[]{rgb(0x172554), rgb(0x2563eb), rgb(0x93c5fd)};
            default:
                return new Color[]{rgb(0x082f49), rgb(0x0284c7), rgb(0x7dd3fc)};
        }
    }

    /** Wood plank with iron trim and corner brackets, regenerated when the panel size changes. */
    private void ensurePanelTexture(int w, int h) {
        if (w == builtWidth && h == builtHeight) {
            return;
        }
        builtWidth = w;
        builtHeight = h;
        if (panelTexture != null) {
            panelTexture.dispose();
        }
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(IRON_MID);
        pixmap.fill();
        pixmap.setColor(IRON_DARK);
        pixmap.fillRectangle(2, 2, w - 4, h - 4);
        for (int y = 6; y < h - 6; y++) {
            float t = (y - 6) / (float) Math.max(1, h - 12);
            pixmap.setColor(WOOD_TOP.cpy().lerp(WOOD_BOTTOM, t));
            pixmap.fillRectangle(6, y, w - 12, 1);
        }
        pixmap.setColor(1f, 1f, 1f, 0.18f);
        pixmap.fillRectangle(6, 6, w - 12, 2);
        pixmap.setColor(0f, 0f, 0f, 0.55f);
        pixmap.fillRectangle(6, h - 9, w - 12, 3);
        bracket(pixmap, 0, 0);
        bracket(pixmap, w - 10, 0);
        bracket(pixmap, 0, h - 10);
        bracket(pixmap, w - 10, h - 10);
        panelTexture = new Texture(pixmap);
        pixmap.dispose();
        panelImage.setDrawable(new TextureRegionDrawable(new TextureRegion(panelTexture)));
    }

    private void bracket(Pixmap pixmap, int x, int y) {
        pixmap.setColor(BRACKET_DARK);
        pixmap.fillRectangle(x, y, 10, 10);
        pixmap.setColor(BRACKET_LIGHT);
        pixmap.fillRectangle(x + 2, y + 2, 6, 6);
    }

    private Texture buildAvatarFrame() {
        Pixmap pixmap = new Pixmap(58, 58, Pixmap.Format.RGBA8888);
        pixmap.setColor(AVATAR_BORDER);
        pixmap.fill();
        pixmap.setColor(AVATAR_BG);
        pixmap.fillRectangle(3, 3, 52, 52);
        pixmap.setColor(1f, 1f, 1f, 0.15f);
        pixmap.fillRectangle(3, 3, 52, 1);
        pixmap.fillRectangle(3, 3, 1, 52);
        pixmap.setColor(0f, 0f, 0f, 0.5f);
        pixmap.fillRectangle(3, 53, 52, 2);
        pixmap.fillRectangle(53, 3, 2, 52);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture avatarTexture(int[][] rects) {
        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        for (int[] rect : rects) {
            pixmap.setColor(rgb(rect[4]));
            pixmap.fillRectangle(rect[0], rect[1], rect[2], rect[3]);
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap.dispose();
        return texture;
    }

    private Texture buildTray() {
        Pixmap pixmap = new Pixmap(240, 26, Pixmap.Format.RGBA8888);
        pixmap.setColor(TRAY_BORDER);
        pixmap.fill();
        pixmap.setColor(TRAY_FILL);
        pixmap.fillRectangle(2, 2, 236, 22);
        pixmap.setColor(0f, 0f, 0f, 0.6f);
        pixmap.fillRectangle(2, 2, 236, 2);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /** White pulsing border used for the active-turn glow; tinted per state. */
    private Texture buildGlowTexture() {
        int size = 48;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int edge = Math.min(Math.min(x, y), Math.min(size - 1 - x, size - 1 - y));
                float alpha = edge < 4 ? 1f : Math.max(0f, 1f - (edge - 3) / 8f);
                if (alpha > 0f) {
                    pixmap.setColor(1f, 1f, 1f, alpha);
                    pixmap.drawPixel(x, y);
                }
            }
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /** Places the panel at screen coordinates; contents keep fixed pixel offsets. */
    void layoutPanel(float px, float py, float pw, float ph) {
        panelX = px;
        panelY = py;
        ensurePanelTexture((int) pw, (int) ph);

        glowImage.setBounds(px - 10, py - 10, pw + 20, ph + 20);
        panelImage.setBounds(px, py, pw, ph);
        avatarFrame.setBounds(px + 12, py + 17, 58, 58);
        avatarImage.setBounds(px + 21, py + 26, 40, 40);

        float badgeX = px + 80 + nameLabel.getPrefWidth() + 8;
        badgeChip.setBounds(badgeX, py + 63, badgeLabel.getPrefWidth() + 10, 15);
        badgeLabel.setPosition(badgeX + 5, py + 65.5f);
        nameLabel.setPosition(px + 80, py + 64);

        checkChip.setBounds(px + 46, py + 69, 52, 14);
        checkLabel.setPosition(px + 46 + 26 - checkLabel.getPrefWidth() / 2f, py + 72);

        trayImage.setBounds(px + 80, py + 28, 240, 26);
        capturedLabel.setPosition(px + 86, py + 34);
        for (int i = 0; i < capturedIcons.size(); i++) {
            capturedIcons.get(i).setBounds(px + 84 + i * 15, py + 33, 14, 14);
        }
        scoreChip.setBounds(px + 328, py + 31, 44, 20);
        scoreLabel.setPosition(px + 328 + 22 - scoreLabel.getPrefWidth() / 2f, py + 36);

        pillImage.setBounds(px + pw - 170, py + 56, 118, 24);
        pillLabel.setPosition(px + pw - 170 + 59 - pillLabel.getPrefWidth() / 2f,
            py + 56 + 12 - pillLabel.getPrefHeight() / 2f);
        timerImage.setBounds(px + pw - 146, py + 22, 94, 24);
        timerLabel.setPosition(px + pw - 106 + 47 - timerLabel.getPrefWidth() / 2f,
            py + 22 + 12 - timerLabel.getPrefHeight() / 2f);
    }

    /** Refreshes pill, glow, check badge, captured tray, and material chip from the board. */
    void update(Board board, boolean opponentIsAI, boolean aiThinking) {
        boolean white = side == Side.PLAYER;
        boolean over = board.isGameOver();
        boolean myTurn = board.isWhiteTurn() == white;
        checkVisible = board.isKingInCheck(white);
        checkPulse = checkVisible;
        checkChip.setVisible(checkVisible);
        checkLabel.setVisible(checkVisible);

        if (over) {
            setPill(pillWaitDrawable, "WAITING", PILL_WAIT_TEXT);
            glowImage.setVisible(false);
        } else if (myTurn) {
            if (white) {
                setPill(pillTurnDrawable, "YOUR TURN", PILL_TURN_TEXT);
            } else {
                setPill(pillBusyDrawable, opponentIsAI ? "THINKING..." : "P2'S TURN", PILL_BUSY_TEXT);
            }
            glowColor = white ? GLOW_GREEN : GLOW_RED;
            glowImage.setVisible(true);
        } else {
            setPill(pillWaitDrawable, "WAITING", PILL_WAIT_TEXT);
            glowImage.setVisible(false);
        }
        if (checkVisible) {
            glowColor = GLOW_RED;
            glowImage.setVisible(true);
        }

        refreshCaptured(board);
        int balance = board.getMaterialBalance();
        int advantage = white ? balance : -balance;
        boolean showScore = advantage > 0;
        scoreChip.setVisible(showScore);
        scoreLabel.setVisible(showScore);
        if (showScore) {
            scoreLabel.setText("+" + advantage);
            scoreLabel.setPosition(panelX + 328 + 22 - scoreLabel.getPrefWidth() / 2f, panelY + 36);
        }
    }

    private void setPill(TextureRegionDrawable drawable, String text, Color color) {
        pillImage.setDrawable(drawable);
        if (!text.equals(pillLabel.getText().toString())) {
            pillLabel.setText(text);
        }
        pillLabel.setColor(color);
        // Position depends on text width; recompute from the anchored pill origin.
        pillLabel.setPosition(panelX + panelImage.getWidth() - 170 + 59 - pillLabel.getPrefWidth() / 2f,
            panelY + 56 + 12 - pillLabel.getPrefHeight() / 2f);
    }

    private void refreshCaptured(Board board) {
        List<Integer> captured = new ArrayList<Integer>(board.getCapturedPieces(side == Side.PLAYER));
        int hash = captured.size() * 31;
        for (int i = 0; i < captured.size(); i++) {
            hash = hash * 31 + captured.get(i);
        }
        if (hash == lastCaptureHash) {
            return;
        }
        lastCaptureHash = hash;
        capturedGroup.clearChildren();
        capturedIcons.clear();
        Collections.sort(captured, captureOrder);
        int shown = Math.min(captured.size(), 10);
        for (int i = 0; i < shown; i++) {
            Texture texture = pieceTextures.get(captured.get(i));
            Image icon = new Image(new TextureRegionDrawable(new TextureRegion(texture)));
            icon.setBounds(panelX + 84 + i * 15, panelY + 33, 14, 14);
            capturedGroup.addActor(icon);
            capturedIcons.add(icon);
        }
    }

    /** Pulses the turn glow and CHECK badge. */
    void tick(float delta) {
        time += delta;
        if (glowImage.isVisible()) {
            float period = checkPulse ? 0.5f : 1.4f;
            float pulse = 0.72f + 0.28f * (float) Math.sin(time * (float) Math.PI * 2f / period);
            glowImage.setColor(glowColor.r, glowColor.g, glowColor.b, pulse);
        }
        if (checkVisible) {
            float pulse = 0.7f + 0.3f * (float) Math.sin(time * (float) Math.PI * 4f);
            checkChip.setColor(1f, 1f, 1f, pulse);
            checkLabel.getColor().a = pulse;
        }
    }

    /** Sets the countdown text; {@code low} turns it red under one minute. */
    void setTimer(String text, boolean low) {
        timerLabel.setText(text);
        timerLabel.setColor(low ? TIMER_LOW : TIMER_TEXT);
        timerLabel.setPosition(panelX + panelImage.getWidth() - 146 + 47 - timerLabel.getPrefWidth() / 2f,
            panelY + 22 + 12 - timerLabel.getPrefHeight() / 2f);
    }

    @Override
    public void dispose() {
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();
        if (panelTexture != null) {
            panelTexture.dispose();
        }
    }
}
