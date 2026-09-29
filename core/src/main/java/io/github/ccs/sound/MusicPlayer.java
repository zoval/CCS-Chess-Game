package io.github.ccs.sound;

import com.badlogic.gdx.audio.Music;

/**
 * Handles playback, pausing, looping, and volume synchronization for background music.
 * Only one track plays at a time; switching tracks stops the previous one.
 */
public class MusicPlayer {
    private final SoundLoader soundLoader;
    private final AudioSettings settings;

    private Music current;

    public MusicPlayer(SoundLoader soundLoader, AudioSettings settings) {
        this.soundLoader = soundLoader;
        this.settings = settings;
    }

    /**
     * Starts playing the menu music track in loop mode.
     */
    public void playMenuMusic() {
        switchTo(soundLoader.getMenuMusic());
    }

    /**
     * Starts playing the in-game music track in loop mode.
     */
    public void playGameMusic() {
        switchTo(soundLoader.getGameMusic());
    }

    private void switchTo(Music next) {
        if (next == null) {
            return;
        }

        if (current != next) {
            if (current != null) {
                current.stop();
            }
            current = next;
        }

        current.setVolume(settings.getEffectiveMusicVolume());
        current.setLooping(true);

        if (settings.isSoundEnabled() && !current.isPlaying()) {
            current.play();
        }
    }

    /**
     * Stops the current music track.
     */
    public void stopMusic() {
        if (current != null && current.isPlaying()) {
            current.stop();
        }
    }

    /**
     * Pauses the current music track.
     */
    public void pauseMusic() {
        if (current != null && current.isPlaying()) {
            current.pause();
        }
    }

    /**
     * Resumes music playback if enabled.
     */
    public void resumeMusic() {
        if (!settings.isSoundEnabled()) {
            return;
        }
        if (current != null && !current.isPlaying()) {
            current.play();
        }
    }

    /**
     * Synchronizes music volume when settings change.
     */
    public void updateVolume() {
        if (current == null) {
            return;
        }

        float volume = settings.getEffectiveMusicVolume();
        current.setVolume(volume);

        if (!settings.isSoundEnabled() && current.isPlaying()) {
            current.pause();
        } else if (settings.isSoundEnabled() && !current.isPlaying() && volume > 0f) {
            current.play();
        }
    }

    /**
     * Checks if music is currently playing.
     *
     * @return true if playing, false otherwise
     */
    public boolean isPlaying() {
        return current != null && current.isPlaying();
    }
}
