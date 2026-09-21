package io.github.ccs.backend;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

public class SaveManager {
    private static final String SAVE_FILE = "save.json";

    public static void saveGame(SaveData SaveData) {
        Json json = new Json();
        String jsonString = json.toJson(SaveData);
        FileHandle file = Gdx.files.local(SAVE_FILE);
        file.writeString(jsonString, false);
    }

    public static SaveData loadGame() {
        FileHandle file = Gdx.files.local(SAVE_FILE);
        if (!file.exists()) {
            return null; // No saved game found
        }
        String jsonString = file.readString();
        Json json = new Json();
        return json.fromJson(SaveData.class, jsonString);
    }
}
