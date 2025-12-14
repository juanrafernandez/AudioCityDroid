package com.jrlabs.audiocity.data.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.jrlabs.audiocity.domain.model.Stop
import com.jrlabs.audiocity.domain.service.AudioPreviewService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TextToSpeech implementation of AudioPreviewService.
 * Handles audio playback for stop previews (separate from active route audio).
 */
@Singleton
class TextToSpeechAudioPreviewService @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioPreviewService, TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private var currentStop: Stop? = null

    private val _currentPreviewStopId = MutableStateFlow<String?>(null)
    override val currentPreviewStopId: StateFlow<String?> = _currentPreviewStopId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    override val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    init {
        textToSpeech = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale("es", "ES"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech?.setLanguage(Locale("es"))
            }

            textToSpeech?.setSpeechRate(0.9f)

            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isPlaying.value = true
                    _isPaused.value = false
                }

                override fun onDone(utteranceId: String?) {
                    _isPlaying.value = false
                    _isPaused.value = false
                    _currentPreviewStopId.value = null
                    currentStop = null
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    handleError()
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    handleError()
                }
            })

            isInitialized = true
        }
    }

    private fun handleError() {
        _isPlaying.value = false
        _isPaused.value = false
        _currentPreviewStopId.value = null
        currentStop = null
    }

    override fun playPreview(stop: Stop) {
        if (!isInitialized) return

        // Stop any current playback
        textToSpeech?.stop()

        currentStop = stop
        _currentPreviewStopId.value = stop.id
        _isPaused.value = false

        val utteranceId = UUID.randomUUID().toString()
        textToSpeech?.speak(stop.scriptEs, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    override fun pausePreview() {
        textToSpeech?.stop()
        _isPaused.value = true
        _isPlaying.value = false
    }

    override fun resumePreview() {
        val stop = currentStop ?: return
        if (!_isPaused.value) return

        _isPaused.value = false
        val utteranceId = UUID.randomUUID().toString()
        textToSpeech?.speak(stop.scriptEs, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    override fun stopPreview() {
        textToSpeech?.stop()
        _isPlaying.value = false
        _isPaused.value = false
        _currentPreviewStopId.value = null
        currentStop = null
    }

    override fun isPlayingStop(stopId: String): Boolean {
        return _currentPreviewStopId.value == stopId && _isPlaying.value
    }

    override fun isPausedStop(stopId: String): Boolean {
        return _currentPreviewStopId.value == stopId && _isPaused.value
    }

    override fun release() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }
}
