package com.musicplayer.engine;

import com.musicplayer.exception.AudioException;

import java.io.IOException;

/**
 * Concrete Implementor 2.
 *
 * <p>Plays compressed audio (MP3, OGG, AAC) by launching an external
 * {@code ffplay} process via {@link ProcessBuilder}.
 * All process-level errors are translated into {@link AudioException}.
 */
public class FFmpegEngine implements AudioEngine {

    private String loadedPath;
    private Process process;
    private double currentVolume = 1.0;

    @Override
    public void load(String filePath) throws AudioException {
        if (filePath == null || filePath.isBlank()) {
            throw new AudioException("FFmpegEngine: file path must not be null or blank");
        }
        this.loadedPath = filePath;
    }

    @Override
    public void play() throws AudioException {
        if (loadedPath == null) {
            throw new AudioException("FFmpegEngine: no file loaded — call load() first");
        }
        try {
            // ffplay: suppress display, exit when done, apply volume filter
            ProcessBuilder pb = new ProcessBuilder(
                "ffplay", "-nodisp", "-autoexit",
                "-af", "volume=" + currentVolume,
                loadedPath
            );
            pb.redirectErrorStream(true);
            process = pb.start();
        } catch (IOException e) {
            throw new AudioException(
                "FFmpegEngine: failed to start ffplay for '" + loadedPath + "'", e);
        }
    }

    @Override
    public void stop() throws AudioException {
        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }

    @Override
    public void setVolume(double level) throws AudioException {
        if (level < 0.0 || level > 1.0) {
            throw new AudioException(
                "FFmpegEngine: volume must be in [0.0, 1.0], got " + level);
        }
        this.currentVolume = level;
    }
}
