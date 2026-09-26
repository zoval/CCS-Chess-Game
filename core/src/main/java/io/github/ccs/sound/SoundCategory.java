package io.github.ccs.sound;

/**
 * Enum defining all sound categories with their file paths.
 * Centralizes sound asset configuration for easy management.
 */
public enum SoundCategory {
    PIECE_MOVE("sounds/moving(wood).ogg"),
    PIECE_CAPTURE("sounds/capture(bubble).ogg"),
    CHECKMATE("sounds/checkmate(xp leveling up).mp3"),
    STALEMATE_1("sounds/drawORstalemate1(anvil fall).mp3"),
    STALEMATE_2("sounds/drawORstalemate2(villager huh).mp3"),
    UI_CLICK("sounds/UISelect.mp3"),
    START_GAME("sounds/startgame(orb).mp3"),
    LOSE("sounds/losingSoundfx(ghast).mp3"),
    BACKGROUND_MUSIC("sounds/startgame(orb).mp3");
    
    private final String filePath;
    
    SoundCategory(String filePath) {
        this.filePath = filePath;
    }
    
    public String getFilePath() {
        return filePath;
    }
}
