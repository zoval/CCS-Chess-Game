package io.github.ccs.sound;

import com.badlogic.gdx.audio.Music;

/**
 * Handles playback, pausing, looping, and volume synchronization for background music.
 */
public class MusicPlayer {
    private final SoundLoader soundLoader;
    private final AudioSettings settings;

    public MusicPlayer(SoundLoader soundLoader, AudioSettings settings) {
        this.soundLoader = soundLoader;
        this.settings = settings;
    }

    /**
     * Starts playing background music in loop mode.
     */
    public void playBackgroundMusic() {
        Music music = soundLoader.getMusic();
        if (music == null) {
            return;
        }

        float volume = settings.getEffectiveMusicVolume();
        music.setVolume(volume);
        music.setLooping(true);

        if (settings.isSoundEnabled() && !music.isPlaying()) {
            music.play();
        }
    }

    /**
     * Stops the background music.
     */
    public void stopBackgroundMusic() {
        Music music = soundLoader.getMusic();
        if (music != null && music.isPlaying()) {
            music.stop();
        }
    }

    /**
     * Pauses the background music.
     */
    public void pauseBackgroundMusic() {
        Music music = soundLoader.getMusic();
        if (music != null && music.isPlaying()) {
            music.pause();
        }
    }

    /**
     * Resumes background music playback if enabled.
     */
    public void resumeBackgroundMusic() {
        if (!settings.isSoundEnabled()) {
            return;
        }
        playBackgroundMusic();
    }

    /**
     * Synchronizes music volume when settings change.
     */
    public void updateVolume() {
        Music music = soundLoader.getMusic();
        if (music == null) {
            return;
        }

        float volume = settings.getEffectiveMusicVolume();
        music.setVolume(volume);

        if (!settings.isSoundEnabled() && music.isPlaying()) {
            music.pause();
        } else if (settings.isSoundEnabled() && !music.isPlaying() && volume > 0f) {
            music.play();
        }
    }

    /**
     * Checks if background music is currently playing.
     *
     * @return true if playing, false otherwise
     */
    public boolean isPlaying() {
        Music music = soundLoader.getMusic();
        return music != null && music.isPlaying();
    }
}
