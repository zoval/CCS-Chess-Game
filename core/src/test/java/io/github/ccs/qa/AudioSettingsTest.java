package io.github.ccs.qa;

import org.junit.Assert;
import org.junit.Test;

import io.github.ccs.sound.AudioSettings;

public class AudioSettingsTest {

    @Test
    public void defaultValuesAreValid() {
        AudioSettings settings = new AudioSettings();
        Assert.assertTrue(settings.isSoundEnabled());
        Assert.assertEquals(1.0f, settings.getMasterVolume(), 0.001f);
        Assert.assertEquals(1.0f, settings.getSFXVolume(), 0.001f);
        Assert.assertEquals(0.5f, settings.getMusicVolume(), 0.001f);
        Assert.assertEquals(1.0f, settings.getEffectiveSFXVolume(), 0.001f);
        Assert.assertEquals(0.5f, settings.getEffectiveMusicVolume(), 0.001f);
    }

    @Test
    public void volumeClampingWorks() {
        AudioSettings settings = new AudioSettings();
        settings.setMasterVolume(1.5f);
        Assert.assertEquals(1.0f, settings.getMasterVolume(), 0.001f);

        settings.setMasterVolume(-0.5f);
        Assert.assertEquals(0.0f, settings.getMasterVolume(), 0.001f);

        settings.setSFXVolume(2.0f);
        Assert.assertEquals(1.0f, settings.getSFXVolume(), 0.001f);

        settings.setMusicVolume(-1.0f);
        Assert.assertEquals(0.0f, settings.getMusicVolume(), 0.001f);
    }

    @Test
    public void muteCalculatesZeroEffectiveVolume() {
        AudioSettings settings = new AudioSettings();
        settings.setSoundEnabled(false);
        Assert.assertFalse(settings.isSoundEnabled());
        Assert.assertEquals(0.0f, settings.getEffectiveSFXVolume(), 0.001f);
        Assert.assertEquals(0.0f, settings.getEffectiveMusicVolume(), 0.001f);

        settings.toggleSound();
        Assert.assertTrue(settings.isSoundEnabled());
        Assert.assertEquals(1.0f, settings.getEffectiveSFXVolume(), 0.001f);
        Assert.assertEquals(0.5f, settings.getEffectiveMusicVolume(), 0.001f);
    }

    @Test
    public void effectiveVolumeScalesWithMaster() {
        AudioSettings settings = new AudioSettings();
        settings.setMasterVolume(0.5f);
        settings.setSFXVolume(0.8f);
        settings.setMusicVolume(0.4f);

        Assert.assertEquals(0.4f, settings.getEffectiveSFXVolume(), 0.001f);
        Assert.assertEquals(0.2f, settings.getEffectiveMusicVolume(), 0.001f);
    }
}
