package io.github.ccs.sound;

import java.util.EnumMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/**
 * Handles loading and caching of sound and music assets.
 * Provides error-safe loading with null checks.
 */
public class SoundLoader {
    
    private final Map<SoundCategory, Sound> sounds = new EnumMap<>(SoundCategory.class);
    private Music backgroundMusic;

    /**
     * Loads all predefined sound effects and background music.
     */
    public void loadAll() {
        for (SoundCategory category : SoundCategory.values()) {
            if (category == SoundCategory.BACKGROUND_MUSIC) {
                loadMusic(category);
            } else {
                loadSound(category);
            }
        }
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
     * Loads background music from the specified category.
     * Only one music track can be loaded at a time.
     *
     * @param category the music category to load
     */
    public void loadMusic(SoundCategory category) {
        try {
            // Dispose previous music if exists
            if (backgroundMusic != null) {
                backgroundMusic.dispose();
            }
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(category.getFilePath()));
            backgroundMusic.setLooping(true);
        } catch (Exception e) {
            Gdx.app.error("SoundLoader", "Failed to load music: " + category.getFilePath(), e);
        }
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
     * Gets the background music object.
     *
     * @return the Music object, or null if not loaded
     */
    public Music getMusic() {
        return backgroundMusic;
    }
    
    /**
     * Disposes all loaded sound and music resources.
     * Must be called to prevent memory leaks.
     */
    public void dispose() {
        // Dispose all sounds
        for (Sound sound : sounds.values()) {
            if (sound != null) {
                sound.dispose();
            }
        }
        sounds.clear();
        
        // Dispose music
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
            backgroundMusic = null;
        }
    }
}