package io.github.ccs.sound;

import java.util.Random;

import com.badlogic.gdx.audio.Sound;

/**
 * Handles sound effects playback, volume scaling, and random variation selection.
 */
public class SoundEffectPlayer {
    private final SoundLoader soundLoader;
    private final AudioSettings settings;
    private final Random random = new Random();

    public SoundEffectPlayer(SoundLoader soundLoader, AudioSettings settings) {
        this.soundLoader = soundLoader;
        this.settings = settings;
    }

    /**
     * Plays a sound effect at the current effective SFX volume.
     *
     * @param category the sound category to play
     * @return the sound instance id, or -1 if not played
     */
    public long play(SoundCategory category) {
        return play(category, 1.0f);
    }

    /**
     * Plays a sound effect with a custom volume multiplier.
     *
     * @param category the sound category to play
     * @param volumeMultiplier multiplier applied to current SFX volume (0.0 to 1.0)
     * @return the sound instance id, or -1 if not played
     */
    public long play(SoundCategory category, float volumeMultiplier) {
        float volume = settings.getEffectiveSFXVolume() * Math.max(0f, volumeMultiplier);
        if (volume <= 0f) {
            return -1;
        }

        Sound sound = soundLoader.getSound(category);
        if (sound != null) {
            return sound.play(volume);
        }
        return -1;
    }

    /**
     * Plays a sound effect with custom volume, pitch, and pan.
     *
     * @param category the sound category to play
     * @param volumeMultiplier multiplier applied to current SFX volume
     * @param pitch pitch multiplier (0.5 to 2.0, 1.0 = normal)
     * @param pan pan from -1.0 (left) to 1.0 (right)
     * @return the sound instance id, or -1 if not played
     */
    public long play(SoundCategory category, float volumeMultiplier, float pitch, float pan) {
        float volume = settings.getEffectiveSFXVolume() * Math.max(0f, volumeMultiplier);
        if (volume <= 0f) {
            return -1;
        }

        Sound sound = soundLoader.getSound(category);
        if (sound != null) {
            return sound.play(volume, pitch, pan);
        }
        return -1;
    }

    public void playPieceMove() {
        play(SoundCategory.PIECE_MOVE);
    }

    public void playPieceCapture() {
        play(SoundCategory.PIECE_CAPTURE);
    }

    public void playCheckmate() {
        play(SoundCategory.CHECKMATE);
    }

    public void playStalemate() {
        SoundCategory category = random.nextBoolean() ? SoundCategory.STALEMATE_1 : SoundCategory.STALEMATE_2;
        play(category);
    }

    public void playUIClick() {
        play(SoundCategory.UI_CLICK);
    }

    public void playStartGame() {
        play(SoundCategory.START_GAME);
    }

    public void playLose() {
        play(SoundCategory.LOSE);
    }
}
