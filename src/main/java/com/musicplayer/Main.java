package com.musicplayer;

import com.musicplayer.engine.FFmpegEngine;
import com.musicplayer.engine.JavaSoundEngine;
import com.musicplayer.engine.LegacyWinampAdapter;
import com.musicplayer.engine.legacy.LegacyWinampPlugin;
import com.musicplayer.playable.Playlist;
import com.musicplayer.playable.Song;
import com.musicplayer.selector.AudioEngineSelector;

import java.util.List;

/**
 * Demonstration entry point.
 *
 * <p>Shows how the Bridge + Adapter design works together with dynamic
 * implementor selection. No concrete engine class is referenced inside
 * {@link Song} or {@link Playlist}.
 */
public class Main {

    public static void main(String[] args) {
        // Build all three engines (one per format family)
        AudioEngineSelector selector = new AudioEngineSelector(
            new JavaSoundEngine(),                         // .wav, .aiff
            new FFmpegEngine(),                            // .mp3, .ogg, .aac
            new LegacyWinampAdapter(new LegacyWinampPlugin()) // .mid, .midi
        );

        System.out.println("=== Dynamic Engine Selection Demo ===");
        String[] files = {
            "resources/symphony.wav",   // → JavaSoundEngine
            "resources/jazz.mp3",       // → FFmpegEngine
            "resources/theme.mid"       // → LegacyWinampAdapter
        };
        for (String file : files) {
            System.out.printf("  %-30s → %s%n",
                file, selector.selectFor(file).getClass().getSimpleName());
        }

        System.out.println("\n=== Song playback (dynamic engine, no hard-coded choice) ===");
        try {
            // WAV song — engine selected automatically
            Song song = new Song(
                "resources/symphony.wav",
                "Symphony No. 5",
                "Beethoven",
                selector.selectFor("resources/symphony.wav")
            );
            // song.play();  // would require an actual WAV file

            // MIDI song — routed to LegacyWinampAdapter transparently
            Song midiSong = new Song(
                "resources/theme.mid",
                "Game Theme",
                "Composer",
                selector.selectFor("resources/theme.mid")
            );
            // midiSong.play();  // would require an actual MIDI file

            System.out.println("  Song objects created successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        System.out.println("\n=== Playlist Demo ===");
        try {
            Playlist playlist = new Playlist(
                "Morning Classics",
                List.of(
                    "resources/track1.wav",
                    "resources/track2.wav",
                    "resources/track3.wav"
                ),
                selector.selectFor("resources/track1.wav")
            );
            System.out.println("  Playlist '" + playlist.getName()
                + "' created with " + playlist.getSize() + " tracks.");
            // playlist.play();  // would require actual WAV files
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
