package io.github.ccs.backend;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

import io.github.ccs.game_logic.Board;

public class SaveManager {
    private static final String SAVE_FILE = "save.json";

    public static void saveGame(Board board) {
        SaveData data = SaveData.capture(board);
        Json json = new Json();
        String jsonString = json.toJson(data);
        Gdx.files.local(SAVE_FILE).writeString(jsonString, false);
}

    public static SaveData loadGame() {
        FileHandle file = Gdx.files.local(SAVE_FILE);
        if (!file.exists()) {
            return null; // no saved game found
        }
        String jsonString = file.readString();
        Json json = new Json();
        return json.fromJson(SaveData.class, jsonString);
    }
}