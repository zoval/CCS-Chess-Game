package io.github.ccs.sound;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

/**
 * Handles playback, pausing, and volume synchronization for background music.
 * Supports shuffle playlists that auto-advance on track completion.
 */
public class MusicPlayer {
    private final SoundLoader soundLoader;
    private final AudioSettings settings;

    /** Currently playing Music object (loaded fresh per track). */
    private Music current;

    /** Active playlist; empty means single-track legacy mode. */
    private List<String> playlist = new ArrayList<>();
    private int playlistIndex = 0;

    public MusicPlayer(SoundLoader soundLoader, AudioSettings settings) {
        this.soundLoader = soundLoader;
        this.settings = settings;
    }

    // -----------------------------------------------------------------------
    // Playlist API
    // -----------------------------------------------------------------------

    /**
     * Starts playing a shuffled playlist. Disposes previous track first.
     * Each track is loaded fresh so memory stays bounded to one track at a time.
     */
    public void setPlaylist(List<String> tracks) {
        stopAndDisposeCurrent();
        playlist = new ArrayList<>(tracks);
        playlistIndex = 0;
        if (!playlist.isEmpty()) {
            playTrack(0);
        }
    }

    private void playTrack(int index) {
        stopAndDisposeCurrent();
        String path = playlist.get(index);
        try {
            current = Gdx.audio.newMusic(Gdx.files.internal(path));
        } catch (Exception e) {
            Gdx.app.error("MusicPlayer", "Failed to load track: " + path, e);
            advanceTrack(); // skip broken track
            return;
        }
        current.setVolume(settings.getEffectiveMusicVolume());
        current.setLooping(false);
        current.setOnCompletionListener(m -> Gdx.app.postRunnable(this::advanceTrack));
        if (settings.isSoundEnabled()) {
            current.play();
        }
    }

    private void advanceTrack() {
        if (playlist.isEmpty()) return;
        playlistIndex = (playlistIndex + 1) % playlist.size();
        playTrack(playlistIndex);
    }

    // -----------------------------------------------------------------------
    // Legacy single-track API (still used by the SoundManager delegation)
    // -----------------------------------------------------------------------

    /**
     * Starts playing the menu music track in loop mode (single-track fallback).
     */
    public void playMenuMusic() {
        switchTo(soundLoader.getMenuMusic());
    }

    /**
     * Starts playing the in-game music track in loop mode (single-track fallback).
     */
    public void playGameMusic() {
        switchTo(soundLoader.getGameMusic());
    }

    private void switchTo(Music next) {
        if (next == null) {
            return;
        }
        // Leaving playlist mode
        playlist.clear();

        if (current != next) {
            stopAndDisposeCurrent();
            current = next;
        }

        current.setVolume(settings.getEffectiveMusicVolume());
        current.setLooping(true);
        current.setOnCompletionListener(null);

        if (settings.isSoundEnabled() && !current.isPlaying()) {
            current.play();
        }
    }

    // -----------------------------------------------------------------------
    // Playback controls
    // -----------------------------------------------------------------------

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

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    /**
     * Stops and disposes the current track only if it was loaded by the playlist
     * system (i.e. not the shared SoundLoader-owned Music objects).
     * Playlist tracks are loaded fresh per-track so they must be disposed.
     * Legacy single-track objects are owned by SoundLoader and must NOT be disposed here.
     */
    private void stopAndDisposeCurrent() {
        if (current == null) return;
        current.stop();
        // Dispose only playlist-owned tracks (not SoundLoader's shared Music objects)
        if (!playlist.isEmpty()
                || (soundLoader.getMenuMusic() != current && soundLoader.getGameMusic() != current)) {
            current.dispose();
        }
        current = null;
    }
}

