package com.jrlabs.audiocity.domain.service

import com.jrlabs.audiocity.domain.model.Stop
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for audio preview service.
 * Handles audio playback for stop previews (separate from active route audio).
 * This is used when browsing stops in route detail view.
 */
interface AudioPreviewService {

    /**
     * Current stop being previewed (if any).
     */
    val currentPreviewStopId: StateFlow<String?>

    /**
     * Whether preview is currently playing.
     */
    val isPlaying: StateFlow<Boolean>

    /**
     * Whether preview is paused.
     */
    val isPaused: StateFlow<Boolean>

    /**
     * Plays audio preview for a stop.
     * @param stop The stop to preview.
     */
    fun playPreview(stop: Stop)

    /**
     * Pauses current preview.
     */
    fun pausePreview()

    /**
     * Resumes paused preview.
     */
    fun resumePreview()

    /**
     * Stops and clears current preview.
     */
    fun stopPreview()

    /**
     * Checks if a specific stop is currently being played.
     * @param stopId The ID of the stop to check.
     */
    fun isPlayingStop(stopId: String): Boolean

    /**
     * Checks if a specific stop is currently paused.
     * @param stopId The ID of the stop to check.
     */
    fun isPausedStop(stopId: String): Boolean

    /**
     * Releases resources when preview service is no longer needed.
     */
    fun release()
}
