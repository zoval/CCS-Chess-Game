package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.ccs.MainGame;
import io.github.ccs.sound.SoundManager;

/** Screen to select the chess board/piece theme. */
public class CollectionScreen extends ScreenAdapter {
    private final MainGame game;
    private Stage stage;

    public CollectionScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        root.add(new Label("Select Theme", new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(new com.badlogic.gdx.graphics.g2d.BitmapFont(), com.badlogic.gdx.graphics.Color.WHITE))).padBottom(20f).row();

        root.add(createThemeButton("Standard", BoardTheme.STANDARD)).padBottom(10f).row();
        root.add(createThemeButton("Minecraft", BoardTheme.MINECRAFT)).padBottom(20f).row();
        
        root.add(createBackButton());

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

    private TextButton createThemeButton(String label, final BoardTheme theme) {
        TextButton button = new TextButton(label, new com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle(null, null, null, new com.badlogic.gdx.graphics.g2d.BitmapFont()));
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().playUIClick();
                game.setSelectedTheme(theme);
                game.setScreen(new FirstScreen(game));
            }
        });
        return button;
    }

    private TextButton createBackButton() {
        TextButton back = new TextButton("Back", new com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle(null, null, null, new com.badlogic.gdx.graphics.g2d.BitmapFont()));
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().playUIClick();
                game.setScreen(new FirstScreen(game));
            }
        });
        return back;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
    }
}
