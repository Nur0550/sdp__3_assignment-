package com.musicplayer;

import com.musicplayer.engine.AudioEngine;
import com.musicplayer.exception.AudioException;
import com.musicplayer.playable.Playlist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Playlist — Refined Abstraction 2")
class PlaylistTest {

    @Mock AudioEngine engine;

    private static final List<String> TRACKS = List.of(
        "resources/track1.wav",
        "resources/track2.wav",
        "resources/track3.wav"
    );

    private Playlist playlist;

    @BeforeEach
    void setUp() {
        playlist = new Playlist("Rock Classics", TRACKS, engine);
    }

    // ── play() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("play() loads first track, sets volume to 0.85, then calls play()")
    void play_loadsFirstTrackAt085() throws AudioException {
        playlist.play();

        InOrder order = inOrder(engine);
        order.verify(engine).load("resources/track1.wav");
        order.verify(engine).setVolume(0.85);
        order.verify(engine).play();
        verifyNoMoreInteractions(engine);
    }

    // ── stop() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("stop() delegates stop() to engine")
    void stop_delegatesStop() throws AudioException {
        playlist.stop();
        verify(engine).stop();
        verifyNoMoreInteractions(engine);
    }

    // ── next() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("next() stops current track then plays the second track")
    void next_stopsCurrentAndPlaysNext() throws AudioException {
        playlist.play();   // play track 1
        playlist.next();   // stop track 1, play track 2

        InOrder order = inOrder(engine);
        // first play()
        order.verify(engine).load("resources/track1.wav");
        order.verify(engine).setVolume(0.85);
        order.verify(engine).play();
        // next()
        order.verify(engine).stop();
        order.verify(engine).load("resources/track2.wav");
        order.verify(engine).setVolume(0.85);
        order.verify(engine).play();

        assertEquals(1, playlist.getCurrentIndex());
    }

    @Test
    @DisplayName("next() called twice advances to third track")
    void next_calledTwice_advancesToThirdTrack() throws AudioException {
        playlist.play();
        playlist.next();
        playlist.next();

        assertEquals(2, playlist.getCurrentIndex());
        // last load must be track3
        verify(engine).load("resources/track3.wav");
    }

    @Test
    @DisplayName("next() wraps around to the first track after the last")
    void next_wrapsAround_afterLastTrack() throws AudioException {
        // advance to end
        playlist.play();
        playlist.next(); // → track 2
        playlist.next(); // → track 3
        playlist.next(); // → wrap to track 1

        assertEquals(0, playlist.getCurrentIndex());
        // track1 loaded twice: once at start, once after wrap
        verify(engine, times(2)).load("resources/track1.wav");
    }

    @Test
    @DisplayName("single-track playlist: next() stays on the same track (index 0)")
    void next_singleTrack_staysOnSameIndex() throws AudioException {
        Playlist single = new Playlist("Solo", List.of("resources/only.wav"), engine);
        single.play();
        single.next();

        assertEquals(0, single.getCurrentIndex());
    }

    // ── constructor validation ────────────────────────────────────────────────

    @Test
    @DisplayName("constructor rejects null file list")
    void constructor_rejectsNullList() {
        assertThrows(IllegalArgumentException.class,
            () -> new Playlist("Bad", null, engine));
    }

    @Test
    @DisplayName("constructor rejects empty file list")
    void constructor_rejectsEmptyList() {
        assertThrows(IllegalArgumentException.class,
            () -> new Playlist("Empty", List.of(), engine));
    }

    @Test
    @DisplayName("constructor rejects null engine")
    void constructor_rejectsNullEngine() {
        assertThrows(IllegalArgumentException.class,
            () -> new Playlist("Bad", TRACKS, null));
    }

    // ── getters ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getName() and getSize() return correct values")
    void getters_returnCorrectValues() {
        assertEquals("Rock Classics", playlist.getName());
        assertEquals(3,               playlist.getSize());
    }
}
