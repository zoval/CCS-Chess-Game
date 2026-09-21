package io.github.ccs.ui;
import io.github.ccs.backend.SaveData;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.ccs.MainGame;

/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {
    private final MainGame game;
    private Stage stage;
    private BitmapFont font;

    public FirstScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        font = new BitmapFont(); // Default LibGDX font

        Gdx.input.setInputProcessor(stage);

        // UI layout
        Table table = new Table();
        table.setFillParent(true);

        // Title
        TextButton.TextButtonStyle titleStyle = new TextButton.TextButtonStyle();
        titleStyle.font = font;
        TextButton title = new TextButton("Chess Game", titleStyle);

        // Play Button
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        TextButton playButton = new TextButton("Play", buttonStyle);

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                game.setScreen(new ChessGameScreen(game)); // Transition to game screen
            }
        });

        // Load Button
        TextButton loadButton = new TextButton("Load Game", buttonStyle);
        loadButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                
                SaveData.SaveGame(); // Call the loadGame method to load the saved game state
                System.out.println("Load Game button clicked!"); // Placeholder for load game functionality
            }
        });

        // Add elements to table
        table.top();
        table.add(title).padTop(40).center();
        table.row();
        table.add(playButton).padTop(150).center();
        table.row();
        table.add(loadButton).padTop(10).center();

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (font != null) font.dispose();
    }
}
