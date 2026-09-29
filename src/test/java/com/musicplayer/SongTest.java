package com.musicplayer;

import com.musicplayer.engine.AudioEngine;
import com.musicplayer.exception.AudioException;
import com.musicplayer.playable.Song;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Song — Refined Abstraction 1")
class SongTest {

    @Mock AudioEngine engine;

    private Song song;

    @BeforeEach
    void setUp() {
        song = new Song("resources/track.wav", "Bohemian Rhapsody", "Queen", engine);
    }

    // ── play() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("play() calls load(), setVolume(1.0), play() on engine — in that order")
    void play_delegatesInOrder() throws AudioException {
        song.play();

        InOrder order = inOrder(engine);
        order.verify(engine).load("resources/track.wav");
        order.verify(engine).setVolume(1.0);
        order.verify(engine).play();
        verifyNoMoreInteractions(engine);
    }

    @Test
    @DisplayName("play() propagates AudioException thrown by engine.load()")
    void play_propagatesLoadException() throws AudioException {
        doThrow(new AudioException("load failed")).when(engine).load(any());

        AudioException ex = assertThrows(AudioException.class, () -> song.play());
        assertEquals("load failed", ex.getMessage());
        verify(engine).load(any());
        verify(engine, never()).play();
    }

    @Test
    @DisplayName("play() propagates AudioException thrown by engine.play()")
    void play_propagatesPlayException() throws AudioException {
        doThrow(new AudioException("play failed")).when(engine).play();

        assertThrows(AudioException.class, () -> song.play());
    }

    // ── stop() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("stop() delegates stop() to engine")
    void stop_delegatesStop() throws AudioException {
        song.stop();

        verify(engine).stop();
        verifyNoMoreInteractions(engine);
    }

    @Test
    @DisplayName("stop() propagates AudioException from engine")
    void stop_propagatesException() throws AudioException {
        doThrow(new AudioException("stop failed")).when(engine).stop();

        assertThrows(AudioException.class, () -> song.stop());
    }

    // ── setVolume() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("setVolume(0.7) delegates to engine.setVolume(0.7)")
    void setVolume_delegatesToEngine() throws AudioException {
        song.setVolume(0.7);
        verify(engine).setVolume(0.7);
    }

    // ── getters ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getters return the values provided at construction")
    void getters_returnConstructorValues() {
        assertEquals("resources/track.wav", song.getFilePath());
        assertEquals("Bohemian Rhapsody",   song.getTitle());
        assertEquals("Queen",               song.getArtist());
    }

    @Test
    @DisplayName("constructor rejects null engine")
    void constructor_rejectsNullEngine() {
        assertThrows(IllegalArgumentException.class,
            () -> new Song("path.wav", "Title", "Artist", null));
    }
}
