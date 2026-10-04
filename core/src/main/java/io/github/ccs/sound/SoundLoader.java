package io.github.ccs.sound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

/**
 * Handles loading and caching of sound and music assets.
 * Provides error-safe loading with null checks.
 */
public class SoundLoader {

    // Fallback playlists used when directory scan returns nothing
    static final List<String> MENU_FALLBACK = Arrays.asList(
        "sounds/Menu page music.mp3",
        "sounds/MenupageLabyrinthine.mp3"
    );

    static final List<String> GAME_FALLBACK = Arrays.asList(
        "sounds/In-game music.mp3",
        "sounds/In-gameBelow and Above.mp3",
        "sounds/In-gameBroken Clocks.mp3",
        "sounds/In-gameFirebugs.mp3",
        "sounds/In-gameFireflies.mp3",
        "sounds/In-gameLilypad.mp3",
        "sounds/In-gameO_s Piano.mp3"
    );

    private final Map<SoundCategory, Sound> sounds = new EnumMap<>(SoundCategory.class);
    private Music menuMusic;
    private Music gameMusic;

    /**
     * Loads all predefined sound effects and background music.
     */
    public void loadAll() {
        for (SoundCategory category : SoundCategory.values()) {
            if (isMusic(category)) {
                loadMusic(category);
            } else {
                loadSound(category);
            }
        }
    }

    private static boolean isMusic(SoundCategory category) {
        return category == SoundCategory.MENU_MUSIC || category == SoundCategory.GAME_MUSIC;
    }

    /**
     * Loads a sound effect from the specified category.
     * Logs error but does not crash if file is missing.
     *
     * @param category the sound category to load
     */
    public void loadSound(SoundCategory category) {
        try {
            Sound sound = Gdx.audio.newSound(Gdx.files.internal(category.getFilePath()));
            sounds.put(category, sound);
        } catch (Exception e) {
            Gdx.app.error("SoundLoader", "Failed to load sound: " + category.getFilePath(), e);
        }
    }

    /**
     * Loads a music track from the specified category.
     *
     * @param category the music category to load
     */
    public void loadMusic(SoundCategory category) {
        try {
            Music music = Gdx.audio.newMusic(Gdx.files.internal(category.getFilePath()));
            music.setLooping(true);
            if (category == SoundCategory.MENU_MUSIC) {
                menuMusic = music;
            } else {
                gameMusic = music;
            }
        } catch (Exception e) {
            Gdx.app.error("SoundLoader", "Failed to load music: " + category.getFilePath(), e);
        }
    }

    /**
     * Builds a shuffled playlist of asset paths whose filenames start with the given prefix.
     * Falls back to the fallback list if the directory scan finds nothing.
     *
     * @param prefix   filename prefix to match (e.g. "In-game" or "Menu")
     * @param fallback list of asset paths to use when no matching files are found
     * @return shuffled, non-empty list of asset paths
     */
    public static List<String> buildPlaylist(String prefix, List<String> fallback) {
        List<String> result = new ArrayList<>();
        try {
            FileHandle dir = Gdx.files.internal("sounds");
            for (FileHandle f : dir.list()) {
                if (f.name().startsWith(prefix)) {
                    result.add("sounds/" + f.name());
                }
            }
        } catch (Exception e) {
            Gdx.app.log("SoundLoader", "Directory scan failed, using fallback: " + e.getMessage());
        }
        if (result.isEmpty()) {
            result.addAll(fallback);
        }
        Collections.shuffle(result);
        return result;
    }

    /**
     * Gets a loaded sound by category.
     * Returns null if sound failed to load or wasn't loaded.
     *
     * @param category the sound category
     * @return the Sound object, or null if not available
     */
    public Sound getSound(SoundCategory category) {
        return sounds.get(category);
    }

    /**
     * Gets the menu music object.
     *
     * @return the Music object, or null if not loaded
     */
    public Music getMenuMusic() {
        return menuMusic;
    }

    /**
     * Gets the in-game music object.
     *
     * @return the Music object, or null if not loaded
     */
    public Music getGameMusic() {
        return gameMusic;
    }

    /**
     * Disposes all loaded sound and music resources.
     * Must be called to prevent memory leaks.
     */
    public void dispose() {
        for (Sound sound : sounds.values()) {
            if (sound != null) {
                sound.dispose();
            }
        }
        sounds.clear();

        if (menuMusic != null) {
            menuMusic.dispose();
            menuMusic = null;
        }
        if (gameMusic != null) {
            gameMusic.dispose();
            gameMusic = null;
        }
    }
}