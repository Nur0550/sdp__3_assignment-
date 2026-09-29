package com.musicplayer.engine;

import com.musicplayer.exception.AudioException;

/**
 * Bridge Implementor interface.
 *
 * <p>Defines the low-level audio operations that every concrete engine must provide.
 * The Abstraction layer ({@link com.musicplayer.playable.Playable} and its subclasses)
 * depends exclusively on this interface — never on any concrete engine class,
 * the legacy plugin, or its error codes.
 */
public interface AudioEngine {

    /**
     * Loads an audio file, preparing it for playback.
     *
     * @param filePath path to the audio file
     * @throws AudioException if the file cannot be loaded
     */
    void load(String filePath) throws AudioException;

    /**
     * Starts or resumes playback of the loaded file.
     *
     * @throws AudioException if playback cannot be started
     */
    void play() throws AudioException;

    /**
     * Stops playback.
     *
     * @throws AudioException if stopping fails
     */
    void stop() throws AudioException;

    /**
     * Sets the playback volume.
     *
     * @param level linear volume in [0.0, 1.0] where 0.0 is silence and 1.0 is full volume
     * @throws AudioException if the level is out of range or the engine rejects it
     */
    void setVolume(double level) throws AudioException;
}
