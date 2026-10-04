package io.github.ccs.sound;

import com.badlogic.gdx.Gdx;

/**
 * Singleton manager for all game audio including sound effects and background music.
 * Handles loading, playback, volume control, and resource cleanup.
 */
public class SoundManager {
    private static SoundManager instance;

    private final SoundLoader soundLoader = new SoundLoader();
    private final AudioSettings settings = new AudioSettings();
    private final SoundEffectPlayer effectPlayer = new SoundEffectPlayer(soundLoader, settings);
    private final MusicPlayer musicPlayer = new MusicPlayer(soundLoader, settings);

    private boolean initialized = false;

    private SoundManager() {
        // Private constructor prevents direct instantiation
    }

    /**
     * Gets the singleton instance of SoundManager.
     * Thread-safe lazy initialization.
     *
     * @return the singleton SoundManager instance
     */
    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * Initializes audio resources by loading all sound and music files.
     */
    public void initialize() {
        if (!initialized) {
            soundLoader.loadAll();
            initialized = true;
            Gdx.app.log("SoundManager", "Sound system initialized");
        }
    }

    public AudioSettings getSettings() {
        return settings;
    }

    public SoundEffectPlayer getEffects() {
        return effectPlayer;
    }

    public MusicPlayer getMusicPlayer() {
        return musicPlayer;
    }

    public void setMasterVolume(float volume) {
        settings.setMasterVolume(volume);
        musicPlayer.updateVolume();
    }

    public void setSFXVolume(float volume) {
        settings.setSFXVolume(volume);
        musicPlayer.updateVolume();
    }

    public float getMasterVolume() {
        return settings.getMasterVolume();
    }

    public float getSFXVolume() {
        return settings.getSFXVolume();
    }

    /**
     * Toggles sound on/off. When disabled, stops background music.
     * When re-enabled, resumes background music if it was playing before.
     */
    public void toggleSound() {
        settings.toggleSound();
        musicPlayer.updateVolume();
    }

    public boolean isSoundEnabled() {
        return settings.isSoundEnabled();
    }

    public void setSoundEnabled(boolean soundEnabled) {
        settings.setSoundEnabled(soundEnabled);
        musicPlayer.updateVolume();
    }

    public boolean isInitialized() {
        return initialized;
    }

    public long playSound(SoundCategory category) {
        return effectPlayer.play(category);
    }

    public long playSound(SoundCategory category, float volumeScale) {
        return effectPlayer.play(category, volumeScale);
    }

    public void playPieceMove() {
        effectPlayer.playPieceMove();
    }

    public void playPromotionFlash() {
        effectPlayer.playPromotionFlash();
    }

    public void playPieceCapture() {
        effectPlayer.playPieceCapture();
    }

    public void playCheckmate() {
        effectPlayer.playCheckmate();
    }

    public void playStalemate() {
        effectPlayer.playStalemate();
    }

    public void playUIClick() {
        effectPlayer.playUIClick();
    }

    public void playStartGame() {
        effectPlayer.playStartGame();
    }

    public void playLose() {
        effectPlayer.playLose();
    }

    public void playMenuMusic() {
        musicPlayer.setPlaylist(SoundLoader.buildPlaylist("Menu", SoundLoader.MENU_FALLBACK));
    }

    public void playGameMusic() {
        musicPlayer.setPlaylist(SoundLoader.buildPlaylist("In-game", SoundLoader.GAME_FALLBACK));
    }

    public void stopMusic() {
        musicPlayer.stopMusic();
    }

    public void pauseMusic() {
        musicPlayer.pauseMusic();
    }

    public void resumeMusic() {
        musicPlayer.resumeMusic();
    }

    /**
     * Disposes of all audio resources.
     */
    public void dispose() {
        musicPlayer.stopMusic();
        soundLoader.dispose();
        initialized = false;
        Gdx.app.log("SoundManager", "Sound system disposed");
    }
}
