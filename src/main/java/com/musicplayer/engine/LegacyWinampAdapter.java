package com.musicplayer.engine;

import com.musicplayer.engine.legacy.LegacyWinampPlugin;
import com.musicplayer.exception.AudioException;

/**
 * Concrete Implementor 3 — Adapter.
 *
 * <p>Adapts {@link LegacyWinampPlugin} into the {@link AudioEngine} interface
 * so it can participate in the Bridge structure without modifying the legacy class.
 *
 * <h3>Incompatibilities resolved</h3>
 * <table border="1">
 *   <tr><th>Criterion</th><th>AudioEngine contract</th><th>LegacyWinampPlugin</th></tr>
 *   <tr>
 *     <td>Method name / signature</td>
 *     <td>{@code load(String)}</td>
 *     <td>{@code openTrack(String, int)} — extra {@code sampleRate} parameter</td>
 *   </tr>
 *   <tr>
 *     <td>Volume parameter type & range</td>
 *     <td>{@code setVolume(double)} in [0.0, 1.0]</td>
 *     <td>{@code setGainDb(float)} in [-60.0, 0.0] dB</td>
 *   </tr>
 *   <tr>
 *     <td>Failure mechanism</td>
 *     <td>throws {@link AudioException}</td>
 *     <td>returns negative {@code int} status codes</td>
 *   </tr>
 * </table>
 *
 * <p>Every non-zero status code is translated into an {@link AudioException}.
 * Nothing specific to {@link LegacyWinampPlugin} (codes, class name, etc.)
 * leaks through the {@link AudioEngine} contract.
 */
public class LegacyWinampAdapter implements AudioEngine {

    /** Fixed sample rate passed to the legacy plugin. */
    private static final int DEFAULT_SAMPLE_RATE = 44100;

    private final LegacyWinampPlugin plugin;

    public LegacyWinampAdapter(LegacyWinampPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Translates {@code load(path)} → {@code openTrack(path, 44100)}.
     * Translates non-zero status code → {@link AudioException}.
     */
    @Override
    public void load(String filePath) throws AudioException {
        int status = plugin.openTrack(filePath, DEFAULT_SAMPLE_RATE);
        if (status != 0) {
            throw new AudioException(
                "LegacyWinampAdapter: failed to load '" + filePath
                    + "' (status=" + status + ")");
        }
    }

    /**
     * Translates {@code play()} → {@code startPlayback()}.
     * Translates non-zero status code → {@link AudioException}.
     */
    @Override
    public void play() throws AudioException {
        int status = plugin.startPlayback();
        if (status != 0) {
            throw new AudioException(
                "LegacyWinampAdapter: playback failed (status=" + status + ")");
        }
    }

    /**
     * Translates {@code stop()} → {@code stopPlayback()}.
     * Translates non-zero status code → {@link AudioException}.
     */
    @Override
    public void stop() throws AudioException {
        int status = plugin.stopPlayback();
        if (status != 0) {
            throw new AudioException(
                "LegacyWinampAdapter: stop failed (status=" + status + ")");
        }
    }

    /**
     * Translates linear volume [0.0, 1.0] → dB gain [-60.0, 0.0] and
     * forwards to {@code setGainDb(float)}.
     *
     * <p>Conversion: {@code gainDb = 20 · log₁₀(level)}.
     * Special case: {@code level == 0.0} maps to {@code -60.0 dB} (near silence).
     *
     * <p>Range validation is performed before calling the legacy plugin —
     * an out-of-range value never reaches the plugin.
     */
    @Override
    public void setVolume(double level) throws AudioException {
        if (level < 0.0 || level > 1.0) {
            throw new AudioException(
                "LegacyWinampAdapter: volume must be in [0.0, 1.0], got " + level);
        }
        float gainDb = (level == 0.0)
            ? -60.0f
            : (float) (20.0 * Math.log10(level));
        int status = plugin.setGainDb(gainDb);
        if (status != 0) {
            throw new AudioException(
                "LegacyWinampAdapter: setGainDb failed (status=" + status + ")");
        }
    }
}
