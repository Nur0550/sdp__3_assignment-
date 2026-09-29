package com.musicplayer.engine;

import com.musicplayer.exception.AudioException;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Concrete Implementor 1.
 *
 * <p>Plays WAV / AIFF audio files using the standard Java {@link javax.sound.sampled} API.
 * All internal checked exceptions are translated into {@link AudioException}.
 */
public class JavaSoundEngine implements AudioEngine {

    private Clip clip;

    @Override
    public void load(String filePath) throws AudioException {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File(filePath));
            clip = AudioSystem.getClip();
            clip.open(stream);
        } catch (UnsupportedAudioFileException e) {
            throw new AudioException("JavaSoundEngine: unsupported format for '" + filePath + "'", e);
        } catch (IOException e) {
            throw new AudioException("JavaSoundEngine: I/O error loading '" + filePath + "'", e);
        } catch (LineUnavailableException e) {
            throw new AudioException("JavaSoundEngine: audio line unavailable for '" + filePath + "'", e);
        }
    }

    @Override
    public void play() throws AudioException {
        if (clip == null) {
            throw new AudioException("JavaSoundEngine: no file loaded — call load() first");
        }
        clip.setFramePosition(0);
        clip.start();
    }

    @Override
    public void stop() throws AudioException {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    @Override
    public void setVolume(double level) throws AudioException {
        if (level < 0.0 || level > 1.0) {
            throw new AudioException(
                "JavaSoundEngine: volume must be in [0.0, 1.0], got " + level);
        }
        if (clip == null) {
            throw new AudioException("JavaSoundEngine: no file loaded — call load() first");
        }
        FloatControl gainControl =
            (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float dB = (float) (20.0 * Math.log10(level == 0.0 ? 1e-6 : level));
        float clampedDb = Math.max(gainControl.getMinimum(),
                                   Math.min(gainControl.getMaximum(), dB));
        gainControl.setValue(clampedDb);
    }
}
