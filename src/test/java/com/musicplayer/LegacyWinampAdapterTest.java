package com.musicplayer;

import com.musicplayer.engine.LegacyWinampAdapter;
import com.musicplayer.engine.legacy.LegacyWinampPlugin;
import com.musicplayer.exception.AudioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that {@link LegacyWinampAdapter} correctly translates between
 * the {@link com.musicplayer.engine.AudioEngine} contract and the
 * {@link LegacyWinampPlugin} API, and that no plugin-specific detail leaks
 * through the public interface.
 *
 * <p>Uses a test stub as allowed by assignment requirement 3.6
 * ("mock or stub the implementations").
 */
@DisplayName("LegacyWinampAdapter — failure translation & parameter mapping")
class LegacyWinampAdapterTest {

    /**
     * Test stub simulating LegacyWinampPlugin with configurable return codes
     * and parameter inspection.
     */
    static class StubLegacyWinampPlugin extends LegacyWinampPlugin {
        int openTrackStatus = 0;
        int startPlaybackStatus = 0;
        int stopPlaybackStatus = 0;
        int setGainDbStatus = 0;

        String lastOpenedPath;
        int lastSampleRate = -1;
        float lastGainDb = Float.NaN;

        int openTrackCalls = 0;
        int startPlaybackCalls = 0;
        int stopPlaybackCalls = 0;
        int setGainDbCalls = 0;

        @Override
        public int openTrack(String path, int sampleRate) {
            this.openTrackCalls++;
            this.lastOpenedPath = path;
            this.lastSampleRate = sampleRate;
            return openTrackStatus;
        }

        @Override
        public int startPlayback() {
            this.startPlaybackCalls++;
            return startPlaybackStatus;
        }

        @Override
        public int stopPlayback() {
            this.stopPlaybackCalls++;
            return stopPlaybackStatus;
        }

        @Override
        public int setGainDb(float gainDb) {
            this.setGainDbCalls++;
            this.lastGainDb = gainDb;
            return setGainDbStatus;
        }
    }

    private StubLegacyWinampPlugin stubPlugin;
    private LegacyWinampAdapter adapter;

    @BeforeEach
    void setUp() {
        stubPlugin = new StubLegacyWinampPlugin();
        adapter = new LegacyWinampAdapter(stubPlugin);
    }

    // ── load() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("load(): calls openTrack with fixed 44100 Hz sample rate")
    void load_passesCorrectSampleRate() throws AudioException {
        adapter.load("theme.mid");

        assertEquals(1, stubPlugin.openTrackCalls);
        assertEquals("theme.mid", stubPlugin.lastOpenedPath);
        assertEquals(44100, stubPlugin.lastSampleRate);
    }

    @Test
    @DisplayName("load(): status 0 → no exception thrown")
    void load_statusZero_noException() {
        stubPlugin.openTrackStatus = 0;
        assertDoesNotThrow(() -> adapter.load("track.mid"));
    }

    @Test
    @DisplayName("load(): status -1 → throws AudioException")
    void load_statusMinus1_throwsAudioException() {
        stubPlugin.openTrackStatus = -1;
        assertThrows(AudioException.class, () -> adapter.load("missing.mid"));
    }

    @Test
    @DisplayName("load(): exception message does NOT contain 'LegacyWinampPlugin' (no leakage)")
    void load_exceptionMessage_doesNotLeakPluginClassName() {
        stubPlugin.openTrackStatus = -2;

        AudioException ex = assertThrows(AudioException.class,
            () -> adapter.load("bad.mid"));

        assertFalse(ex.getMessage().contains("LegacyWinampPlugin"),
            "AudioException must not expose the internal legacy class name");
    }

    // ── play() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("play(): status 0 → calls startPlayback(), no exception")
    void play_statusZero_noException() {
        stubPlugin.startPlaybackStatus = 0;

        assertDoesNotThrow(() -> adapter.play());
        assertEquals(1, stubPlugin.startPlaybackCalls);
    }

    @Test
    @DisplayName("play(): status -3 (no track loaded) → throws AudioException")
    void play_statusMinus3_throwsAudioException() {
        stubPlugin.startPlaybackStatus = -3;

        assertThrows(AudioException.class, () -> adapter.play());
    }

    @Test
    @DisplayName("play(): status -4 (already playing) → throws AudioException")
    void play_statusMinus4_throwsAudioException() {
        stubPlugin.startPlaybackStatus = -4;

        assertThrows(AudioException.class, () -> adapter.play());
    }

    // ── stop() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("stop(): status 0 → calls stopPlayback(), no exception")
    void stop_statusZero_noException() {
        stubPlugin.stopPlaybackStatus = 0;

        assertDoesNotThrow(() -> adapter.stop());
        assertEquals(1, stubPlugin.stopPlaybackCalls);
    }

    @Test
    @DisplayName("stop(): status -5 (not playing) → throws AudioException")
    void stop_statusMinus5_throwsAudioException() {
        stubPlugin.stopPlaybackStatus = -5;

        assertThrows(AudioException.class, () -> adapter.stop());
    }

    // ── setVolume() — parameter mapping ──────────────────────────────────────

    @Test
    @DisplayName("setVolume(1.0) → setGainDb(0.0f)  [max volume = 0 dB]")
    void setVolume_1_0_setsGain_0dB() throws AudioException {
        adapter.setVolume(1.0);

        assertEquals(1, stubPlugin.setGainDbCalls);
        assertEquals(0.0f, stubPlugin.lastGainDb, 0.001f);
    }

    @Test
    @DisplayName("setVolume(0.0) → setGainDb(-60.0f)  [silence = -60 dB]")
    void setVolume_0_0_setsGain_minus60dB() throws AudioException {
        adapter.setVolume(0.0);

        assertEquals(1, stubPlugin.setGainDbCalls);
        assertEquals(-60.0f, stubPlugin.lastGainDb, 0.001f);
    }

    @Test
    @DisplayName("setVolume(0.5) → setGainDb ≈ -6.02f  [half power ≈ -6 dB]")
    void setVolume_0_5_setsCorrectGainDb() throws AudioException {
        float expected = (float) (20.0 * Math.log10(0.5)); // ≈ -6.02 dB
        adapter.setVolume(0.5);

        assertEquals(1, stubPlugin.setGainDbCalls);
        assertEquals(expected, stubPlugin.lastGainDb, 0.01f);
    }

    // ── setVolume() — range validation (plugin must NOT be called) ────────────

    @Test
    @DisplayName("setVolume(-0.1) → throws AudioException WITHOUT calling plugin")
    void setVolume_negative_throwsWithoutCallingPlugin() {
        assertThrows(AudioException.class, () -> adapter.setVolume(-0.1));
        assertEquals(0, stubPlugin.setGainDbCalls);
    }

    @Test
    @DisplayName("setVolume(1.1) → throws AudioException WITHOUT calling plugin")
    void setVolume_above1_throwsWithoutCallingPlugin() {
        assertThrows(AudioException.class, () -> adapter.setVolume(1.1));
        assertEquals(0, stubPlugin.setGainDbCalls);
    }

    // ── setVolume() — plugin error translation ────────────────────────────────

    @Test
    @DisplayName("setVolume(): plugin returns -6 → throws AudioException")
    void setVolume_pluginError_throwsAudioException() {
        stubPlugin.setGainDbStatus = -6;

        assertThrows(AudioException.class, () -> adapter.setVolume(0.5));
    }
}
