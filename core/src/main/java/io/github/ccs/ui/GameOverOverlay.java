package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import io.github.ccs.sound.SoundManager;

/**
 * Full-screen game-over composition: the win/lose art layers stack at their
 * designed positions on a 1600x1600 canvas fitted to the window. Clicking
 * the dim backdrop passes through so the history nav stays reachable; the
 * two art buttons launch their actions and glow on hover.
 */
public class GameOverOverlay extends Group implements Disposable {

    /** Which art set to stack: WIN for victory, LOSE for defeat, DRAW reuses the lose set. */
    public enum Result { WIN, LOSE, DRAW }

    private static final float CANVAS = 1600f;
    /** board, icon, text layer crops (x, y, w, h, top-left origin) on the 1600 canvas. */
    private static final int[][] WIN_CROPS = {
        {33, 312, 1556, 975}, {379, 0, 817, 671}, {335, 568, 939, 293}};
    private static final int[][] LOSE_CROPS = {
        {33, 169, 1567, 1118}, {424, 0, 747, 621}, {344, 572, 937, 282}};
    private static final int[] BACK_CROP = {200, 946, 583, 158};
    private static final int[] AGAIN_CROP = {809, 922, 590, 194};
    private static final int[] BACK_LIGHT_CROP = {79, 729, 817, 517};
    private static final int[] AGAIN_LIGHT_CROP = {679, 729, 867, 517};

    private final Texture backdropTexture;
    private final Image boardImage = new Image();
    private final Image iconImage = new Image();
    private final Image textImage = new Image();
    private final Image backButton = new Image();
    private final Image backLight = new Image();
    private final Image playAgainButton = new Image();
    private final Image playAgainLight = new Image();
    private final Label detailLabel;
    private final Texture backTexture;
    private final Texture backLightTexture;
    private final Texture againTexture;
    private final Texture againLightTexture;
    private Texture[] winLayers;
    private Texture[] loseLayers;
    private TextureRegionDrawable winBoardDrawable;
    private TextureRegionDrawable winIconDrawable;
    private TextureRegionDrawable winTextDrawable;
    private TextureRegionDrawable loseBoardDrawable;
    private TextureRegionDrawable loseIconDrawable;
    private TextureRegionDrawable loseTextDrawable;
    private TextureRegionDrawable backDrawable;
    private TextureRegionDrawable backLightDrawable;
    private TextureRegionDrawable againDrawable;
    private TextureRegionDrawable againLightDrawable;
    private Runnable playAgainAction;
    private Runnable backAction;
    private int[][] activeCrops;
    private boolean showTextArt;

    public GameOverOverlay() {
        backdropTexture = ProceduralTextures.whitePixel();
        // Only the overlay's own buttons catch clicks; taps elsewhere fall
        // through to the HUD (history nav) underneath.
        setTouchable(Touchable.childrenOnly);

        Image backdrop = new Image(new TextureRegionDrawable(new TextureRegion(backdropTexture)));
        backdrop.setColor(0f, 0f, 0f, 0.55f);
        backdrop.setFillParent(true);
        // Clicks pass through so history review stays available after the game ends.
        backdrop.setTouchable(Touchable.disabled);
        addActor(backdrop);

        addActor(boardImage);
        addActor(iconImage);
        addActor(textImage);

        detailLabel = new Label("", new Label.LabelStyle(Fonts.large(), Color.GOLD));
        detailLabel.setAlignment(Align.center);
        addActor(detailLabel);

        backTexture = load("game_end/back_button.png");
        backLightTexture = load("game_end/back_button_light.png");
        againTexture = load("game_end/play_again_button.png");
        againLightTexture = load("game_end/play_again_button_light.png");
        backDrawable = new TextureRegionDrawable(new TextureRegion(backTexture,
            BACK_CROP[0], BACK_CROP[1], BACK_CROP[2], BACK_CROP[3]));
        backLightDrawable = new TextureRegionDrawable(new TextureRegion(backLightTexture,
            BACK_LIGHT_CROP[0], BACK_LIGHT_CROP[1], BACK_LIGHT_CROP[2], BACK_LIGHT_CROP[3]));
        againDrawable = new TextureRegionDrawable(new TextureRegion(againTexture,
            AGAIN_CROP[0], AGAIN_CROP[1], AGAIN_CROP[2], AGAIN_CROP[3]));
        againLightDrawable = new TextureRegionDrawable(new TextureRegion(againLightTexture,
            AGAIN_LIGHT_CROP[0], AGAIN_LIGHT_CROP[1], AGAIN_LIGHT_CROP[2], AGAIN_LIGHT_CROP[3]));

        addActor(backLight);
        addActor(playAgainLight);
        addActor(backButton);
        addActor(playAgainButton);

        backButton.setDrawable(backDrawable);
        backLight.setDrawable(backLightDrawable);
        backLight.setVisible(false);
        playAgainButton.setDrawable(againDrawable);
        playAgainLight.setDrawable(againLightDrawable);
        playAgainLight.setVisible(false);

        backButton.addListener(hoverButtons(backLight, () -> {
            if (backAction != null) {
                backAction.run();
            }
        }));
        playAgainButton.addListener(hoverButtons(playAgainLight, () -> {
            if (playAgainAction != null) {
                playAgainAction.run();
            }
        }));

        setVisible(false);
    }

    private ClickListener hoverButtons(final Image glow, final Runnable action) {
        return new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer,
                              com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                if (pointer == -1) {
                    glow.setVisible(true);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer,
                             com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                if (pointer == -1) {
                    glow.setVisible(false);
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().playUIClick();
                action.run();
            }
        };
    }

    /**
     * Stacks the art for the result. {@code detail} adds a pixel-font reason
     * line; when the reason replaces the baked text art ({@code showArtText}
     * false) only the detail line is shown in the text slot.
     */
    public void open(Result result, String detail, boolean showArtText) {
        boolean win = result == Result.WIN;
        Texture[] layers = ensureLayers(win);
        activeCrops = win ? WIN_CROPS : LOSE_CROPS;
        showTextArt = showArtText;

        TextureRegionDrawable board = win ? winBoardDrawable : loseBoardDrawable;
        TextureRegionDrawable icon = win ? winIconDrawable : loseIconDrawable;
        TextureRegionDrawable text = win ? winTextDrawable : loseTextDrawable;
        boardImage.setDrawable(board);
        iconImage.setDrawable(icon);
        textImage.setDrawable(text);

        detailLabel.setText(detail == null ? "" : detail);
        layout();
        setVisible(true);
    }

    public void close() {
        setVisible(false);
    }

    public boolean isOpen() {
        return isVisible();
    }

    public void setActions(Runnable playAgain, Runnable back) {
        playAgainAction = playAgain;
        backAction = back;
    }

    /** Refits the 1600x1600 composition to the current window. */
    public void layout() {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        setSize(width, height);
        float scale = Math.min(width, height) / CANVAS;
        float originX = (width - CANVAS * scale) / 2f;
        float originY = 0f;

        if (activeCrops != null) {
            place(boardImage, activeCrops[0], scale, originX, originY);
            place(iconImage, activeCrops[1], scale, originX, originY);
            place(textImage, activeCrops[2], scale, originX, originY);
            textImage.setVisible(showTextArt);
        }
        place(backButton, BACK_CROP, scale, originX, originY);
        place(backLight, BACK_LIGHT_CROP, scale, originX, originY);
        place(playAgainButton, AGAIN_CROP, scale, originX, originY);
        place(playAgainLight, AGAIN_LIGHT_CROP, scale, originX, originY);

        detailLabel.setFontScale(scale * 2.0f);
        detailLabel.setSize(900 * scale, 44 * scale);
        detailLabel.setPosition(originX + CANVAS / 2f * scale - detailLabel.getWidth() / 2f,
            originY + (CANVAS - 891) * scale - detailLabel.getHeight() / 2f);
    }

    private static void place(Image image, int[] crop, float scale, float originX, float originY) {
        image.setBounds(originX + crop[0] * scale,
            originY + (CANVAS - crop[1] - crop[3]) * scale,
            crop[2] * scale, crop[3] * scale);
    }

    private Texture[] ensureLayers(boolean win) {
        Texture[] layers = win ? winLayers : loseLayers;
        if (layers != null) {
            return layers;
        }
        String prefix = win ? "win" : "lose";
        int[][] crops = win ? WIN_CROPS : LOSE_CROPS;
        Texture board = load("game_end/" + prefix + "_board.png");
        Texture icon = load("game_end/" + prefix + "_icon.png");
        Texture text = load("game_end/" + prefix + "_text.png");
        TextureRegionDrawable boardDrawable =
            new TextureRegionDrawable(new TextureRegion(board, crops[0][0], crops[0][1], crops[0][2], crops[0][3]));
        TextureRegionDrawable iconDrawable =
            new TextureRegionDrawable(new TextureRegion(icon, crops[1][0], crops[1][1], crops[1][2], crops[1][3]));
        TextureRegionDrawable textDrawable =
            new TextureRegionDrawable(new TextureRegion(text, crops[2][0], crops[2][1], crops[2][2], crops[2][3]));
        if (win) {
            winBoardDrawable = boardDrawable;
            winIconDrawable = iconDrawable;
            winTextDrawable = textDrawable;
            winLayers = new Texture[]{board, icon, text};
        } else {
            loseBoardDrawable = boardDrawable;
            loseIconDrawable = iconDrawable;
            loseTextDrawable = textDrawable;
            loseLayers = new Texture[]{board, icon, text};
        }
        return win ? winLayers : loseLayers;
    }

    private static Texture load(String path) {
        return new Texture(Gdx.files.internal(path));
    }

    @Override
    public void dispose() {
        backdropTexture.dispose();
        if (winLayers != null) {
            for (Texture texture : winLayers) {
                texture.dispose();
            }
        }
        if (loseLayers != null) {
            for (Texture texture : loseLayers) {
                texture.dispose();
            }
        }
        backTexture.dispose();
        backLightTexture.dispose();
        againTexture.dispose();
        againLightTexture.dispose();
    }
}
