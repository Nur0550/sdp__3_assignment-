package com.musicplayer.playable;

import com.musicplayer.engine.AudioEngine;
import com.musicplayer.exception.AudioException;

/**
 * Bridge Abstraction.
 *
 * <p>Holds a reference to an {@link AudioEngine} implementor and delegates
 * all low-level audio operations through it.
 *
 * <p><strong>Invariant:</strong> this class and its subclasses must never
 * reference any concrete engine class, {@code LegacyWinampPlugin},
 * its status codes, or its exceptions directly.
 */
public abstract class Playable {

    /** The implementor — can be swapped for any AudioEngine without touching subclasses. */
    protected final AudioEngine engine;

    protected Playable(AudioEngine engine) {
        if (engine == null) throw new IllegalArgumentException("engine must not be null");
        this.engine = engine;
    }

    /**
     * Loads and starts playback. Concrete behaviour defined by subclasses.
     *
     * @throws AudioException if playback cannot be started
     */
    public abstract void play() throws AudioException;

    /**
     * Stops playback. Concrete behaviour defined by subclasses.
     *
     * @throws AudioException if stopping fails
     */
    public abstract void stop() throws AudioException;

    /**
     * Adjusts playback volume via the implementor.
     *
     * @param level linear volume in [0.0, 1.0]
     * @throws AudioException if the engine rejects the value
     */
    public void setVolume(double level) throws AudioException {
        engine.setVolume(level);
    }
}
