# Design Rationale — Music Playback System

**Author:** Student  
**Assignment:** 3 — Bridge + Adapter patterns  
**Complexity Module:** Dynamic Implementor Selection

---

## 1. Problem Domain

A music player must handle different types of audio content — a single **Song** and an ordered **Playlist** — while rendering them through different **audio engines** depending on the file format:

- **WAV / AIFF** → Java's built-in `javax.sound.sampled` API
- **MP3 / OGG / AAC** → external `ffplay` (FFmpeg) process
- **MIDI** → a legacy third-party `LegacyWinampPlugin`

A naïve solution would produce a class for every combination: `WavSong`, `Mp3Song`, `MidiSong`, `WavPlaylist`, `Mp3Playlist`, `MidiPlaylist` — six classes for two content types and three engines. Adding a new engine or a new content type would each multiply the number of classes.

---

## 2. Why Bridge Alone Is Not Enough

The Bridge pattern resolves the subclass explosion by separating "what to play" (`Song`, `Playlist`) from "how to render it" (`AudioEngine`). However, one engine — `LegacyWinampPlugin` — **cannot directly implement `AudioEngine`** without being modified:

- Its method names differ entirely (`openTrack` vs `load`, `startPlayback` vs `play`)
- Its `openTrack` requires an extra `sampleRate` parameter absent from the interface
- Its `setGainDb` takes a `float` in decibels `[-60, 0]`, not a `double` in `[0.0, 1.0]`
- Its failure mechanism is a returned `int` status code, not a thrown exception

Bridge provides no mechanism to bridge these API-level incompatibilities.

---

## 3. Why Adapter Alone Is Not Enough

The Adapter pattern wraps `LegacyWinampPlugin` behind `AudioEngine`, resolving the incompatibilities. However, without Bridge, every new content type (`RadioStream`, `AlbumSide`) combined with every engine still requires its own class. Adapter has no mechanism to separately vary two independent hierarchies.

---

## 4. Why the Wrapped Implementation Is Genuinely Incompatible

`LegacyWinampPlugin` meets all three incompatibility criteria:

| Criterion | Evidence |
|---|---|
| **Different method name / signature** | `openTrack(String, int)` vs `load(String)` — extra parameter |
| **Different parameter type / range** | `setGainDb(float dB)` in `[-60, 0]` vs `setVolume(double)` in `[0.0, 1.0]` — different type and scale |
| **Different failure mechanism** | Returns negative `int` codes (`-1` … `-6`); all other engines throw `AudioException` |

The `LegacyWinampAdapter` performs three non-trivial translations:
1. Supplies the fixed `44100` Hz sample rate the plugin demands.
2. Converts linear volume to decibels: `gainDb = 20 · log₁₀(level)`.
3. Maps every non-zero status code to a descriptive `AudioException` — nothing plugin-specific leaks through the `AudioEngine` contract.

---

## 5. Complexity Module: Dynamic Implementor Selection

`AudioEngineSelector.selectFor(filePath)` inspects the file extension at runtime and returns the appropriate engine. The client (`Song`, `Playlist`) receives the engine through its constructor and is never aware of which concrete engine backs it.

**Open/Closed compliance:** adding support for a new format (e.g., `.flac`) requires adding a single `case` in `AudioEngineSelector` — no Abstraction class is touched.

---

## 6. One Limitation

The engine is assigned at construction time and is fixed for the lifetime of the `Playable` object. If the same content source needed to be re-routed to a different engine at runtime (e.g., switching from local playback to a remote streaming engine mid-session), the `Playable` object would need to be reconstructed. A hot-swap strategy (storing the engine in a mutable field with a setter) would address this at the cost of additional thread-safety considerations.
