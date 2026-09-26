package io.github.ccs.sound;

/**
 * Manages audio settings including master, SFX, and music volumes and mute state.
 */
public class AudioSettings {
    private float masterVolume = 1.0f;
    private float sfxVolume = 1.0f;
    private float musicVolume = 0.5f;
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

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = clamp(musicVolume);
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
        return soundEnabled ? masterVolume * musicVolume : 0f;
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
