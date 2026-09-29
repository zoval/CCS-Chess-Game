package io.github.ccs.sound;

/**
 * Manages audio settings including master and SFX volumes and mute state.
 * Music volume is fixed at a ratio of the SFX volume so it stays quieter.
 */
public class AudioSettings {
    private static final float MUSIC_TO_SFX_RATIO = 0.75f;

    private float masterVolume = 1.0f;
    private float sfxVolume = 1.0f;
    private boolean soundEnabled = true;

    public float getMasterVolume() {
        return masterVolume;
    }

    public void setMasterVolume(float masterVolume) {
        this.masterVolume = clamp(masterVolume);
    }

    public float getSFXVolume() {
        return sfxVolume;
    }

    public void setSFXVolume(float sfxVolume) {
        this.sfxVolume = clamp(sfxVolume);
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public boolean toggleSound() {
        soundEnabled = !soundEnabled;
        return soundEnabled;
    }

    public float getEffectiveSFXVolume() {
        return soundEnabled ? masterVolume * sfxVolume : 0f;
    }

    public float getEffectiveMusicVolume() {
        return getEffectiveSFXVolume() * MUSIC_TO_SFX_RATIO;
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
