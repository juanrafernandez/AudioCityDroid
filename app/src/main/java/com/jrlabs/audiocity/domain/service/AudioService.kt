package com.jrlabs.audiocity.domain.service

import com.jrlabs.audiocity.domain.model.AudioQueueItem
import com.jrlabs.audiocity.domain.model.Stop
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for audio playback service.
 * Follows Interface Segregation Principle - only audio-related operations.
 *
 * Allows for different implementations:
 * - TextToSpeech (current)
 * - MediaPlayer (pre-recorded audio)
 * - Mock for testing
 */
interface AudioService {

    /**
     * Flow indicating whether audio is currently playing.
     */
    val isPlaying: StateFlow<Boolean>

    /**
     * Flow indicating whether audio is paused.
     */
    val isPaused: StateFlow<Boolean>

    /**
     * Flow of the current text being spoken.
     */
    val currentText: StateFlow<String?>

    /**
     * Flow of the current queue item being played.
     */
    val currentQueueItem: StateFlow<AudioQueueItem?>

    /**
     * Flow of all queued items.
     */
    val queuedItems: StateFlow<List<AudioQueueItem>>

    /**
     * Speaks the given text immediately.
     * @param text The text to speak.
     * @param language The language code (e.g., "es", "en").
     */
    fun speak(text: String, language: String = "es")

    /**
     * Adds a stop's audio to the playback queue.
     * @param stop The stop whose audio script should be queued.
     */
    fun enqueueStop(stop: Stop)

    /**
     * Plays the next item in the queue.
     */
    fun playNextInQueue()

    /**
     * Pauses the current playback.
     */
    fun pause()

    /**
     * Resumes playback from where it was paused.
     */
    fun resume()

    /**
     * Stops playback completely.
     */
    fun stop()

    /**
     * Skips to the next item in the queue.
     */
    fun skipToNext()

    /**
     * Clears all items from the queue.
     */
    fun clearQueue()

    /**
     * Returns the number of items in the queue.
     */
    fun getQueueCount(): Int

    /**
     * Releases all resources. Should be called when the service is no longer needed.
     */
    fun release()
}
