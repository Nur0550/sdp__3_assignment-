package com.musicplayer;

import com.musicplayer.engine.AudioEngine;
import com.musicplayer.selector.AudioEngineSelector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MockitoExtension.class)
@DisplayName("AudioEngineSelector — Dynamic Implementor Selection")
class AudioEngineSelectorTest {

    @Mock AudioEngine javaSoundEngine;
    @Mock AudioEngine ffmpegEngine;
    @Mock AudioEngine legacyWinampAdapter;

    private AudioEngineSelector selector;

    @BeforeEach
    void setUp() {
        selector = new AudioEngineSelector(javaSoundEngine, ffmpegEngine, legacyWinampAdapter);
    }

    // ── JavaSoundEngine (default) ─────────────────────────────────────────────

    @Test
    @DisplayName(".wav → JavaSoundEngine")
    void wav_returnsJavaSoundEngine() {
        assertSame(javaSoundEngine, selector.selectFor("symphony.wav"));
    }

    @Test
    @DisplayName(".WAV (uppercase) → JavaSoundEngine")
    void wav_uppercase_returnsJavaSoundEngine() {
        assertSame(javaSoundEngine, selector.selectFor("symphony.WAV"));
    }

    @Test
    @DisplayName(".aiff → JavaSoundEngine (default)")
    void aiff_returnsJavaSoundEngine() {
        assertSame(javaSoundEngine, selector.selectFor("track.aiff"));
    }

    @Test
    @DisplayName("no extension → JavaSoundEngine (default)")
    void noExtension_returnsDefault() {
        assertSame(javaSoundEngine, selector.selectFor("track"));
    }

    // ── FFmpegEngine ──────────────────────────────────────────────────────────

    @Test
    @DisplayName(".mp3 → FFmpegEngine")
    void mp3_returnsFFmpegEngine() {
        assertSame(ffmpegEngine, selector.selectFor("jazz.mp3"));
    }

    @Test
    @DisplayName(".ogg → FFmpegEngine")
    void ogg_returnsFFmpegEngine() {
        assertSame(ffmpegEngine, selector.selectFor("track.ogg"));
    }

    @Test
    @DisplayName(".aac → FFmpegEngine")
    void aac_returnsFFmpegEngine() {
        assertSame(ffmpegEngine, selector.selectFor("podcast.aac"));
    }

    // ── LegacyWinampAdapter ───────────────────────────────────────────────────

    @Test
    @DisplayName(".mid → LegacyWinampAdapter")
    void mid_returnsLegacyAdapter() {
        assertSame(legacyWinampAdapter, selector.selectFor("theme.mid"));
    }

    @Test
    @DisplayName(".midi → LegacyWinampAdapter")
    void midi_returnsLegacyAdapter() {
        assertSame(legacyWinampAdapter, selector.selectFor("theme.midi"));
    }

    @Test
    @DisplayName(".MID (uppercase) → LegacyWinampAdapter")
    void mid_uppercase_returnsLegacyAdapter() {
        assertSame(legacyWinampAdapter, selector.selectFor("theme.MID"));
    }
}
