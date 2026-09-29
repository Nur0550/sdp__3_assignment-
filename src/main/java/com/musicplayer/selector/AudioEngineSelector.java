package com.musicplayer.selector;

import com.musicplayer.engine.AudioEngine;

/**
 * Complexity Module: Dynamic Implementor Selection.
 *
 * <p>Selects the appropriate {@link AudioEngine} at <em>runtime</em> based on
 * the file extension of the requested audio file. The client ({@link com.musicplayer.playable.Playable}
 * and its subclasses) never hard-codes which concrete engine to use.
 *
 * <h3>Selection rules</h3>
 * <ul>
 *   <li>{@code .mid}, {@code .midi} → {@code LegacyWinampAdapter} (MIDI files)</li>
 *   <li>{@code .mp3}, {@code .ogg}, {@code .aac} → {@code FFmpegEngine} (compressed audio)</li>
 *   <li>Everything else ({@code .wav}, {@code .aiff}, …) → {@code JavaSoundEngine} (default)</li>
 * </ul>
 *
 * <p>Adding support for a new file format requires only a new {@code case} in
 * {@link #selectFor(String)} — no Abstraction class is touched (Open/Closed Principle).
 */
public class AudioEngineSelector {

    private final AudioEngine javaSoundEngine;
    private final AudioEngine ffmpegEngine;
    private final AudioEngine legacyWinampAdapter;

    /**
     * @param javaSoundEngine     engine for uncompressed audio (WAV, AIFF)
     * @param ffmpegEngine        engine for compressed audio (MP3, OGG, AAC)
     * @param legacyWinampAdapter adapted engine for MIDI files
     */
    public AudioEngineSelector(AudioEngine javaSoundEngine,
                               AudioEngine ffmpegEngine,
                               AudioEngine legacyWinampAdapter) {
        this.javaSoundEngine     = javaSoundEngine;
        this.ffmpegEngine        = ffmpegEngine;
        this.legacyWinampAdapter = legacyWinampAdapter;
    }

    /**
     * Returns the best engine for the given file, chosen purely from its extension.
     *
     * @param filePath path or file name with extension
     * @return the selected {@link AudioEngine}; never {@code null}
     */
    public AudioEngine selectFor(String filePath) {
        String ext = extractExtension(filePath);
        return switch (ext) {
            case "mid", "midi"       -> legacyWinampAdapter;
            case "mp3", "ogg", "aac" -> ffmpegEngine;
            default                  -> javaSoundEngine;  // wav, aiff, flac, …
        };
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static String extractExtension(String path) {
        if (path == null) return "";
        int dot = path.lastIndexOf('.');
        if (dot < 0 || dot == path.length() - 1) return "";
        return path.substring(dot + 1).toLowerCase();
    }
}
