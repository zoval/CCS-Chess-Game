package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

import io.github.ccs.MainGame;
import io.github.ccs.sound.SoundManager;

/**
 * Modal settings popup (wooden panel art from {@code menu/settings pop up}) with Music/SFX
 * volume sliders, a sound mute toggle, and the visual-aids toggle. Blocks input to whatever
 * is underneath while open; ESC closes it.
 */
public class SettingsDialog extends Group implements Disposable {
    private static final float WINDOW_ASPECT = 1056f / 1004f;

    private final MainGame game;
    private final Texture panelTexture;
    private final Texture closeTexture;
    private final Texture titleTexture;
    private final Texture musicLabelTexture;
    private final Texture sfxLabelTexture;
    private final Texture backdropTexture;
    private final Texture trackTexture;
    private final Texture knobTexture;
    private final Texture buttonUpTexture;
    private final Texture buttonCheckedTexture;
    private final BitmapFont font = new BitmapFont();
    private final Slider musicSlider;
    private final Slider sfxSlider;
    private final TextButton soundToggle;
    private final TextButton visualAidsToggle;

    public SettingsDialog(MainGame game) {
        this.game = game;

        float stageW = Gdx.graphics.getWidth();
        float stageH = Gdx.graphics.getHeight();
        float windowH = Math.min(stageH * 0.86f, stageW * 0.86f / WINDOW_ASPECT);
        float windowW = windowH * WINDOW_ASPECT;
        float windowX = (stageW - windowW) / 2f;
        float windowY = (stageH - windowH) / 2f;

        panelTexture = load("menu/settings pop up/setting1 (1).png");
        closeTexture = load("menu/settings pop up/setting2 (1).png");
        titleTexture = load("menu/settings pop up/setting3 (1).png");
        musicLabelTexture = load("menu/settings pop up/setting4 (1).png");
        sfxLabelTexture = load("menu/settings pop up/sfxx.png");
        backdropTexture = ProceduralTextures.whitePixel();
        trackTexture = ProceduralTextures.whitePixel();
        knobTexture = ProceduralTextures.softDot(64);
        buttonUpTexture = buttonBackground(false);
        buttonCheckedTexture = buttonBackground(true);
        font.getData().setScale(windowH / 320f);

        setSize(stageW, stageH);

        Image backdrop = new Image(new TextureRegionDrawable(new TextureRegion(backdropTexture)));
        backdrop.setColor(0f, 0f, 0f, 0.6f);
        backdrop.setFillParent(true);
        backdrop.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
        addActor(backdrop);

        Table window = new Table();
        window.setBackground(new TextureRegionDrawable(new TextureRegion(panelTexture)));
        window.setSize(windowW, windowH);
        window.setPosition(windowX, windowY);
        window.padLeft(windowW * 0.11f).padRight(windowW * 0.11f)
            .padTop(windowH * 0.10f).padBottom(windowH * 0.08f);
        addActor(window);

        float titleW = windowW * 0.55f;
        Image title = new Image(new TextureRegionDrawable(new TextureRegion(titleTexture)));
        window.add(title).width(titleW).height(titleW * 201f / 863f)
            .spaceBottom(windowH * 0.05f).row();

        musicSlider = createSlider();
        float labelW = windowW * 0.17f;
        Image musicLabel = new Image(new TextureRegionDrawable(new TextureRegion(musicLabelTexture)));
        window.add(musicLabel).width(labelW).height(labelW * 194f / 383f)
            .spaceRight(windowW * 0.04f);
        window.add(musicSlider).width(windowW * 0.50f)
            .spaceBottom(windowH * 0.045f).row();

        sfxSlider = createSlider();
        Image sfxLabel = new Image(new TextureRegionDrawable(new TextureRegion(sfxLabelTexture)));
        window.add(sfxLabel).width(labelW).height(labelW * 163f / 297f)
            .spaceRight(windowW * 0.04f);
        window.add(sfxSlider).width(windowW * 0.50f)
            .spaceBottom(windowH * 0.05f).row();

        soundToggle = createToggle();
        syncSoundToggle();
        window.add(soundToggle).width(windowW * 0.52f).height(windowH * 0.085f)
            .spaceBottom(windowH * 0.025f).row();

        visualAidsToggle = createToggle();
        syncVisualAidsToggle();
        window.add(visualAidsToggle).width(windowW * 0.52f).height(windowH * 0.085f).row();

        float closeSize = windowW * 0.13f;
        ImageButton closeButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(closeTexture)));
        closeButton.setSize(closeSize, closeSize * 282f / 359f);
        closeButton.setPosition(windowX + windowW - closeSize * 0.72f,
            windowY + windowH - closeButton.getHeight() * 0.72f);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });
        addActor(closeButton);

        musicSlider.setValue(SoundManager.getInstance().getMasterVolume());
        sfxSlider.setValue(SoundManager.getInstance().getSFXVolume());
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

        setVisible(false);
    }

    private Texture load(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    private Texture buttonBackground(boolean checked) {
        int width = 120;
        int height = 40;
        int radius = 12;
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(width, height, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(checked ? 0.42f : 0.26f, checked ? 0.32f : 0.18f, 0.12f, 0.95f);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean inCorner = (x < radius && y < radius && Math.hypot(x - radius, y - radius) > radius)
                    || (x >= width - radius && y < radius && Math.hypot(x - (width - radius - 1), y - radius) > radius)
                    || (x < radius && y >= height - radius && Math.hypot(x - radius, y - (height - radius - 1)) > radius)
                    || (x >= width - radius && y >= height - radius
                        && Math.hypot(x - (width - radius - 1), y - (height - radius - 1)) > radius);
                if (!inCorner) {
                    pixmap.drawPixel(x, y);
                }
            }
        }
        return new Texture(pixmap);
    }

    private Slider createSlider() {
        TextureRegionDrawable track = new TextureRegionDrawable(new TextureRegion(trackTexture));
        track.setMinHeight(8f);
        TextureRegionDrawable knob = new TextureRegionDrawable(new TextureRegion(knobTexture));
        knob.setMinWidth(30f);
        knob.setMinHeight(30f);
        Slider.SliderStyle style = new Slider.SliderStyle(track, knob);
        return new Slider(0f, 1f, 0.01f, false, style);
    }

    private TextButton createToggle() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = new TextureRegionDrawable(new TextureRegion(buttonUpTexture));
        style.down = new TextureRegionDrawable(new TextureRegion(buttonCheckedTexture));
        style.checked = new TextureRegionDrawable(new TextureRegion(buttonCheckedTexture));
        style.font = font;
        return new TextButton("", style);
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

    public boolean isOpen() {
        return isVisible();
    }

    public void open() {
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
        backdropTexture.dispose();
        trackTexture.dispose();
        knobTexture.dispose();
        buttonUpTexture.dispose();
        buttonCheckedTexture.dispose();
        font.dispose();
    }
}
