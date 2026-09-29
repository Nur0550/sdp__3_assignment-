package com.musicplayer.playable;

import com.musicplayer.engine.AudioEngine;
import com.musicplayer.exception.AudioException;

/**
 * Refined Abstraction 1.
 *
 * <p>Represents a single music track identified by a file path, title, and artist.
 * Delegates all audio operations to the injected {@link AudioEngine} implementor.
 */
public class Song extends Playable {

    private final String filePath;
    private final String title;
    private final String artist;

    /**
     * @param filePath path to the audio file
     * @param title    track title (e.g. "Bohemian Rhapsody")
     * @param artist   artist name (e.g. "Queen")
     * @param engine   the audio engine implementor
     */
    public Song(String filePath, String title, String artist, AudioEngine engine) {
        super(engine);
        this.filePath = filePath;
        this.title    = title;
        this.artist   = artist;
    }

    /**
     * Loads the track, sets volume to maximum, and starts playback.
     */
    @Override
    public void play() throws AudioException {
        System.out.printf("▶  Playing: %s — %s%n", artist, title);
        engine.load(filePath);
        engine.setVolume(1.0);
        engine.play();
    }

    /**
     * Stops playback of the current track.
     */
    @Override
    public void stop() throws AudioException {
        System.out.printf("■  Stopped: %s — %s%n", artist, title);
        engine.stop();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getFilePath() { return filePath; }
    public String getTitle()    { return title; }
    public String getArtist()   { return artist; }
}
