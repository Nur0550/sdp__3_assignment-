package com.musicplayer.engine.legacy;

/**
 * Simulates a third-party legacy audio plugin with an <strong>incompatible</strong> API.
 *
 * <p><strong>This class must NOT be modified.</strong> It represents an external
 * library whose source we do not own.
 *
 * <p>Incompatibilities with {@link com.musicplayer.engine.AudioEngine}:
 * <ul>
 *   <li><b>Method name mismatch:</b> {@code openTrack} vs {@code load},
 *       {@code startPlayback} vs {@code play}, {@code stopPlayback} vs {@code stop},
 *       {@code setGainDb} vs {@code setVolume}</li>
 *   <li><b>Parameter mismatch:</b> {@code openTrack} requires an extra {@code sampleRate} int;
 *       {@code setGainDb} takes a {@code float} in dB [-60, 0], not a linear [0.0, 1.0] double</li>
 *   <li><b>Different failure mechanism:</b> returns negative {@code int} status codes
 *       instead of throwing exceptions</li>
 * </ul>
 */
public class LegacyWinampPlugin {

    private String loadedPath;
    private boolean playing;

    /**
     * Opens an audio track for playback.
     *
     * @param path       path to the audio file
     * @param sampleRate desired sample rate in Hz (e.g. 44100)
     * @return {@code 0} on success; {@code -1} if path is null/blank;
     *         {@code -2} if the format is unsupported
     */
    public int openTrack(String path, int sampleRate) {
        if (path == null || path.isBlank()) {
            return -1;
        }
        this.loadedPath = path;
        System.out.printf("[LegacyWinamp] openTrack(\"%s\", %d Hz)%n", path, sampleRate);
        return 0;
    }

    /**
     * Begins playback of the previously opened track.
     *
     * @return {@code 0} on success; {@code -3} if no track is loaded;
     *         {@code -4} if playback is already in progress
     */
    public int startPlayback() {
        if (loadedPath == null) {
            return -3;
        }
        if (playing) {
            return -4;
        }
        playing = true;
        System.out.printf("[LegacyWinamp] startPlayback → \"%s\"%n", loadedPath);
        return 0;
    }

    /**
     * Halts playback.
     *
     * @return {@code 0} on success; {@code -5} if not currently playing
     */
    public int stopPlayback() {
        if (!playing) {
            return -5;
        }
        playing = false;
        System.out.println("[LegacyWinamp] stopPlayback");
        return 0;
    }

    /**
     * Adjusts the audio gain in decibels.
     *
     * @param gainDb gain value in the range {@code [-60.0, 0.0]};
     *               {@code 0.0} is full volume, {@code -60.0} is near silence
     * @return {@code 0} on success; {@code -6} if {@code gainDb} is out of range
     */
    public int setGainDb(float gainDb) {
        if (gainDb < -60.0f || gainDb > 0.0f) {
            return -6;
        }
        System.out.printf("[LegacyWinamp] setGainDb(%.2f dB)%n", gainDb);
        return 0;
    }
}
