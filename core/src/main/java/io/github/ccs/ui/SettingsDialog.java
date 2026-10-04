package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import io.github.ccs.MainGame;
import io.github.ccs.sound.SoundManager;

/**
 * Modal settings popup (wooden panel art from {@code menu/settings pop up}, cropped to its
 * visible bounds). Rebuilds its layout on open and on window resize so it re-centers between
 * the 1024x572 menu and 750x750 game windows. Blocks input to whatever is underneath while
 * open; ESC closes it.
 */
public class SettingsDialog extends Group implements Disposable {
    private static final float WINDOW_ASPECT = 875f / 824f;

    // Alpha-bound crops of the transparent-canvas art (measured; x, y, w, h).
    private static final int[] PANEL_CROP = {88, 88, 875, 824};
    private static final int[] CLOSE_CROP = {91, 55, 182, 183};
    private static final int[] TITLE_CROP = {37, 48, 779, 127};
    private static final int[] MUSIC_CROP = {22, 68, 316, 73};
    private static final int[] SFX_CROP = {22, 34, 194, 71};
    private static final int[] KNOB_CROP = {484, 48, 77, 123};

    private final MainGame game;
    private final Texture panelTexture;
    private final Texture closeTexture;
    private final Texture titleTexture;
    private final Texture musicLabelTexture;
    private final Texture sfxLabelTexture;
    private final Texture knobTexture;
    private final Texture backdropTexture;
    private final Texture trackTexture;
    private final Texture buttonUpTexture;
    private final Texture buttonCheckedTexture;
    private final BitmapFont font = new BitmapFont();
    private final Slider.SliderStyle sliderStyle;
    private final TextButton.TextButtonStyle toggleStyle;
    private Slider musicSlider;
    private Slider sfxSlider;
    private TextButton soundToggle;
    private TextButton visualAidsToggle;
    private TextButton saveButton;
    private Label savedLabel;
    private Runnable saveHandler;
    private float saveToastTimer;
    private boolean saveButtonVisible;

    public SettingsDialog(MainGame game) {
        this.game = game;

        panelTexture = load("menu/settings pop up/setting1 (1).png");
        closeTexture = load("menu/settings pop up/setting2 (1).png");
        titleTexture = load("menu/settings pop up/setting3 (1).png");
        musicLabelTexture = load("menu/settings pop up/setting4 (1).png");
        sfxLabelTexture = load("menu/settings pop up/sfxx.png");
        knobTexture = load("menu/settings pop up/setting5 (1).png");
        backdropTexture = ProceduralTextures.whitePixel();
        trackTexture = trackBackground();
        buttonUpTexture = buttonBackground(false);
        buttonCheckedTexture = buttonBackground(true);

        sliderStyle = new Slider.SliderStyle();
        TextureRegionDrawable track = new TextureRegionDrawable(new TextureRegion(trackTexture));
        track.setMinHeight(14f);
        TextureRegionDrawable knob = cropped(knobTexture, KNOB_CROP);
        knob.setMinWidth(30f * KNOB_CROP[2] / KNOB_CROP[3]);
        knob.setMinHeight(30f);
        sliderStyle.background = track;
        sliderStyle.knob = knob;

        toggleStyle = new TextButton.TextButtonStyle();
        toggleStyle.up = new TextureRegionDrawable(new TextureRegion(buttonUpTexture));
        toggleStyle.down = new TextureRegionDrawable(new TextureRegion(buttonCheckedTexture));
        toggleStyle.checked = new TextureRegionDrawable(new TextureRegion(buttonCheckedTexture));
        toggleStyle.font = font;

        // Added once here: layout() rebuilds children and would duplicate this listener.
        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    hide();
                    return true;
                }
                return false;
            }
        });

        layout();
        setVisible(false);
    }

    /** Rebuilds the dialog for the current window size; safe to call from resize(). */
    public void layout() {
        float stageW = Gdx.graphics.getWidth();
        float stageH = Gdx.graphics.getHeight();
        float windowH = Math.min(stageH * 0.86f, stageW * 0.86f / WINDOW_ASPECT);
        float windowW = windowH * WINDOW_ASPECT;
        float windowX = (stageW - windowW) / 2f;
        float windowY = (stageH - windowH) / 2f;

        setSize(stageW, stageH);
        clearChildren(true);
        font.getData().setScale(windowH / 320f);

        Image backdrop = new Image(new TextureRegionDrawable(new TextureRegion(backdropTexture)));
        backdrop.setColor(0f, 0f, 0f, 0.6f);
        backdrop.setBounds(0f, 0f, stageW, stageH);
        backdrop.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
        addActor(backdrop);

        Table window = new Table();
        window.setBackground(cropped(panelTexture, PANEL_CROP));
        window.setSize(windowW, windowH);
        window.setPosition(windowX, windowY);
        window.padLeft(windowW * 0.11f).padRight(windowW * 0.11f)
            .padTop(windowH * 0.10f).padBottom(windowH * 0.08f);
        addActor(window);

        // colspan(2): single-column cells would inflate column 0 past the label width and push
        // the slider column past the panel's right edge.
        float titleW = windowW * 0.55f;
        Image title = new Image(cropped(titleTexture, TITLE_CROP));
        window.add(title).colspan(2).width(titleW).height(titleW * 127f / 779f)
            .spaceBottom(windowH * 0.05f).row();

        float labelW = windowW * 0.17f;
        musicSlider = newSlider();
        musicSlider.setValue(SoundManager.getInstance().getMasterVolume());
        Image musicLabel = new Image(cropped(musicLabelTexture, MUSIC_CROP));
        window.add(musicLabel).width(labelW).height(labelW * 73f / 316f)
            .spaceRight(windowW * 0.04f);
        window.add(musicSlider).width(windowW * 0.50f)
            .spaceBottom(windowH * 0.045f).row();

        sfxSlider = newSlider();
        sfxSlider.setValue(SoundManager.getInstance().getSFXVolume());
        Image sfxLabel = new Image(cropped(sfxLabelTexture, SFX_CROP));
        window.add(sfxLabel).width(labelW).height(labelW * 71f / 194f)
            .spaceRight(windowW * 0.04f);
        window.add(sfxSlider).width(windowW * 0.50f)
            .spaceBottom(windowH * 0.05f).row();

        soundToggle = createToggle();
        syncSoundToggle();
        window.add(soundToggle).colspan(2).width(windowW * 0.52f).height(windowH * 0.085f)
            .spaceBottom(windowH * 0.025f).row();

        visualAidsToggle = createToggle();
        syncVisualAidsToggle();
        window.add(visualAidsToggle).colspan(2).width(windowW * 0.52f).height(windowH * 0.085f).row();

        // Listeners attach after the initial syncs so the sync's setChecked() can't re-enter.
        // layout() rebuilds fresh actors each time, so these never accumulate.
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().setMasterVolume(musicSlider.getValue());
            }
        });
        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().setSFXVolume(sfxSlider.getValue());
            }
        });
        soundToggle.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().setSoundEnabled(soundToggle.isChecked());
                syncSoundToggle();
            }
        });
        visualAidsToggle.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setVisualAidsEnabled(visualAidsToggle.isChecked());
                syncVisualAidsToggle();
            }
        });

        float closeW = windowW * 0.13f * 182f / 359f;
        float closeH = closeW * 183f / 182f;
        ImageButton closeButton = new ImageButton(cropped(closeTexture, CLOSE_CROP));
        closeButton.setSize(closeW, closeH);
        closeButton.setPosition(windowX + windowW - closeW * 0.75f,
            windowY + windowH - closeH * 0.75f);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });
        addActor(closeButton);

        // Overlay actors for the VS-AI-only save flow; they sit in the
        // window's padding bands so the Table layout is untouched.
        saveButton = createToggle();
        saveButton.setText("SAVE GAME");
        saveButton.setVisible(saveButtonVisible);
        float saveWidth = windowW * 0.52f;
        saveButton.setSize(saveWidth, windowH * 0.085f);
        saveButton.setPosition(windowX + (windowW - saveWidth) / 2f, windowY + windowH * 0.015f);
        saveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // setChecked(false) below re-fires changed(); ignore that nested call.
                if (!saveButton.isChecked()) {
                    return;
                }
                SoundManager.getInstance().playUIClick();
                saveButton.setChecked(false);
                if (saveHandler != null) {
                    saveHandler.run();
                }
                saveToastTimer = 1.6f;
                savedLabel.setVisible(true);
            }
        });
        addActor(saveButton);

        savedLabel = new Label("GAME SAVED", new Label.LabelStyle(font, new Color(0.55f, 0.85f, 0.35f, 1f)));
        savedLabel.setVisible(saveToastTimer > 0f);
        savedLabel.setSize(windowW, windowH * 0.06f);
        savedLabel.setAlignment(Align.center);
        savedLabel.setPosition(windowX, windowY + windowH * 0.905f);
        addActor(savedLabel);
    }

    private Texture load(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    private TextureRegionDrawable cropped(Texture texture, int[] crop) {
        return new TextureRegionDrawable(new TextureRegion(texture, crop[0], crop[1], crop[2], crop[3]));
    }

    private Texture buttonBackground(boolean checked) {
        int width = 120;
        int height = 40;
        int radius = 12;
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(checked ? 0.42f : 0.26f, checked ? 0.32f : 0.18f, 0.12f, 0.95f);
        drawRoundedStrip(pixmap, 0, 0, width, height, radius);
        return new Texture(pixmap);
    }

    /** Brown pill matching the slider art's track (setting5), instead of a white bar. */
    private Texture trackBackground() {
        int width = 64;
        int height = 14;
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.05f, 0.04f, 0.03f, 0.9f);
        drawRoundedStrip(pixmap, 0, 0, width, height, height / 2);
        pixmap.setColor(50 / 255f, 35 / 255f, 30 / 255f, 1f);
        drawRoundedStrip(pixmap, 1, 1, width - 2, height - 2, (height - 2) / 2);
        return new Texture(pixmap);
    }

    private void drawRoundedStrip(Pixmap pixmap, int x0, int y0, int width, int height, int radius) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean inCorner = (x < radius && y < radius && Math.hypot(x - radius, y - radius) > radius)
                    || (x >= width - radius && y < radius && Math.hypot(x - (width - radius - 1), y - radius) > radius)
                    || (x < radius && y >= height - radius && Math.hypot(x - radius, y - (height - radius - 1)) > radius)
                    || (x >= width - radius && y >= height - radius
                        && Math.hypot(x - (width - radius - 1), y - (height - radius - 1)) > radius);
                if (!inCorner) {
                    pixmap.drawPixel(x0 + x, y0 + y);
                }
            }
        }
    }

    private Slider newSlider() {
        return new Slider(0f, 1f, 0.01f, false, sliderStyle);
    }

    private TextButton createToggle() {
        return new TextButton("", toggleStyle);
    }

    private void syncSoundToggle() {
        boolean enabled = SoundManager.getInstance().isSoundEnabled();
        soundToggle.setChecked(enabled);
        soundToggle.setText("SOUND: " + (enabled ? "ON" : "OFF"));
    }

    private void syncVisualAidsToggle() {
        boolean enabled = game.isVisualAidsEnabled();
        visualAidsToggle.setChecked(enabled);
        visualAidsToggle.setText("VISUAL AIDS: " + (enabled ? "ON" : "OFF"));
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (saveToastTimer > 0f) {
            saveToastTimer -= delta;
            if (saveToastTimer <= 0f) {
                savedLabel.setVisible(false);
            }
        }
    }

    /** Shows or hides the SAVE GAME button (used only in VS AI mode). */
    public void showSaveButton(boolean visible) {
        saveButtonVisible = visible;
        saveButton.setVisible(visible);
        if (!visible) {
            savedLabel.setVisible(false);
            saveToastTimer = 0f;
        }
    }

    /** Sets the callback run when SAVE GAME is clicked. */
    public void setSaveHandler(Runnable saveHandler) {
        this.saveHandler = saveHandler;
    }

    public boolean isOpen() {
        return isVisible();
    }

    public void open() {
        layout();
        toFront();
        setVisible(true);
        Stage stage = getStage();
        if (stage != null) {
            stage.setKeyboardFocus(this);
        }
        SoundManager.getInstance().playUIClick();
    }

    public void hide() {
        setVisible(false);
        Stage stage = getStage();
        if (stage != null && stage.getKeyboardFocus() == this) {
            stage.setKeyboardFocus(null);
        }
        SoundManager.getInstance().playUIClick();
    }

    @Override
    public void dispose() {
        panelTexture.dispose();
        closeTexture.dispose();
        titleTexture.dispose();
        musicLabelTexture.dispose();
        sfxLabelTexture.dispose();
        knobTexture.dispose();
        backdropTexture.dispose();
        trackTexture.dispose();
        buttonUpTexture.dispose();
        buttonCheckedTexture.dispose();
        font.dispose();
    }
}
