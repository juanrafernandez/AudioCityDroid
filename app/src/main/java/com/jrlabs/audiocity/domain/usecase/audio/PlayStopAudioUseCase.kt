package com.jrlabs.audiocity.domain.usecase.audio

import com.jrlabs.audiocity.domain.model.AudioQueueItem
import com.jrlabs.audiocity.domain.model.Stop
import com.jrlabs.audiocity.domain.service.AudioService
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Use case for playing audio for stops.
 * Encapsulates audio playback logic.
 */
class PlayStopAudioUseCase @Inject constructor(
    private val audioService: AudioService
) {

    val isPlaying: StateFlow<Boolean>
        get() = audioService.isPlaying

    val isPaused: StateFlow<Boolean>
        get() = audioService.isPaused

    val currentQueueItem: StateFlow<AudioQueueItem?>
        get() = audioService.currentQueueItem

    val queuedItems: StateFlow<List<AudioQueueItem>>
        get() = audioService.queuedItems

    /**
     * Enqueues a stop's audio for playback.
     */
    fun enqueue(stop: Stop) {
        audioService.enqueueStop(stop)
    }

    /**
     * Plays or resumes audio.
     */
    fun playOrResume() {
        if (audioService.isPaused.value) {
            audioService.resume()
        }
    }

    /**
     * Pauses current playback.
     */
    fun pause() {
        audioService.pause()
    }

    /**
     * Stops playback completely.
     */
    fun stop() {
        audioService.stop()
    }

    /**
     * Skips to next item in queue.
     */
    fun skipToNext() {
        audioService.skipToNext()
    }

    /**
     * Clears all queued audio.
     */
    fun clearQueue() {
        audioService.clearQueue()
    }
}
