package com.musicplayer.playable;

import com.musicplayer.engine.AudioEngine;
import com.musicplayer.exception.AudioException;

import java.util.ArrayList;
import java.util.List;

/**
 * Refined Abstraction 2.
 *
 * <p>Represents an ordered playlist of audio tracks. Supports sequential
 * playback and track advancement with wrap-around.
 * Delegates all audio operations to the injected {@link AudioEngine} implementor.
 */
public class Playlist extends Playable {

    /** Playlist volume is slightly lower to allow the track-change gap to feel natural. */
    private static final double PLAYLIST_VOLUME = 0.85;

    private final String name;
    private final List<String> filePaths;
    private int currentIndex;

    /**
     * @param name      display name of the playlist (e.g. "Rock Classics")
     * @param filePaths ordered list of audio file paths; must not be null or empty
     * @param engine    the audio engine implementor
     * @throws IllegalArgumentException if {@code filePaths} is null or empty
     */
    public Playlist(String name, List<String> filePaths, AudioEngine engine) {
        super(engine);
        if (filePaths == null || filePaths.isEmpty()) {
            throw new IllegalArgumentException("Playlist must contain at least one track");
        }
        this.name         = name;
        this.filePaths    = new ArrayList<>(filePaths);
        this.currentIndex = 0;
    }

    /**
     * Loads and plays the current track at playlist volume.
     */
    @Override
    public void play() throws AudioException {
        System.out.printf("▶  Playlist [%s] — track %d / %d%n",
            name, currentIndex + 1, filePaths.size());
        engine.load(filePaths.get(currentIndex));
        engine.setVolume(PLAYLIST_VOLUME);
        engine.play();
    }

    /**
     * Stops playback of the current track.
     */
    @Override
    public void stop() throws AudioException {
        System.out.printf("■  Playlist [%s] stopped%n", name);
        engine.stop();
    }

    /**
     * Stops the current track and advances to the next one (wraps around to the first
     * track after the last).
     *
     * @throws AudioException if stopping or starting the next track fails
     */
    public void next() throws AudioException {
        engine.stop();
        currentIndex = (currentIndex + 1) % filePaths.size();
        play();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getName()        { return name; }
    public int getCurrentIndex()   { return currentIndex; }
    public int getSize()           { return filePaths.size(); }
}
