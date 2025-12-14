package com.jrlabs.audiocity.data.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.jrlabs.audiocity.domain.model.AudioQueueItem
import com.jrlabs.audiocity.domain.model.Stop
import com.jrlabs.audiocity.domain.service.AudioService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.LinkedList
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TextToSpeech implementation of AudioService.
 * Follows Interface Segregation Principle - implements only AudioService methods.
 * Follows Single Responsibility Principle - only handles TTS audio.
 */
@Singleton
class TextToSpeechAudioService @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioService, TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    override val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _currentText = MutableStateFlow<String?>(null)
    override val currentText: StateFlow<String?> = _currentText.asStateFlow()

    private val _currentQueueItem = MutableStateFlow<AudioQueueItem?>(null)
    override val currentQueueItem: StateFlow<AudioQueueItem?> = _currentQueueItem.asStateFlow()

    private val _queuedItems = MutableStateFlow<List<AudioQueueItem>>(emptyList())
    override val queuedItems: StateFlow<List<AudioQueueItem>> = _queuedItems.asStateFlow()

    private val audioQueue = LinkedList<AudioQueueItem>()
    private val processedStopIds = mutableSetOf<String>()

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
                    _currentText.value = null
                    playNextInQueue()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    handlePlaybackError()
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    handlePlaybackError()
                }
            })

            isInitialized = true
        }
    }

    private fun handlePlaybackError() {
        _isPlaying.value = false
        _currentText.value = null
        playNextInQueue()
    }

    override fun speak(text: String, language: String) {
        if (!isInitialized) return

        val locale = when (language) {
            "es" -> Locale("es", "ES")
            "en" -> Locale.US
            else -> Locale("es", "ES")
        }
        textToSpeech?.language = locale

        _currentText.value = text
        val utteranceId = UUID.randomUUID().toString()

        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    override fun enqueueStop(stop: Stop) {
        if (processedStopIds.contains(stop.id)) return
        processedStopIds.add(stop.id)

        val queueItem = AudioQueueItem(
            id = UUID.randomUUID().toString(),
            stopId = stop.id,
            stopName = stop.name,
            text = stop.scriptEs,
            order = stop.order
        )

        audioQueue.add(queueItem)
        audioQueue.sortBy { it.order }

        updateQueuedItemsState()

        if (!_isPlaying.value && !_isPaused.value) {
            playNextInQueue()
        }
    }

    override fun playNextInQueue() {
        if (audioQueue.isEmpty()) {
            _currentQueueItem.value = null
            updateQueuedItemsState()
            return
        }

        val nextItem = audioQueue.poll()
        if (nextItem != null) {
            _currentQueueItem.value = nextItem
            speak(nextItem.text)
        }

        updateQueuedItemsState()
    }

    override fun pause() {
        textToSpeech?.stop()
        _isPaused.value = true
        _isPlaying.value = false
    }

    override fun resume() {
        if (_isPaused.value && _currentQueueItem.value != null) {
            _isPaused.value = false
            speak(_currentQueueItem.value!!.text)
        }
    }

    override fun stop() {
        textToSpeech?.stop()
        _isPlaying.value = false
        _isPaused.value = false
        _currentText.value = null
        _currentQueueItem.value = null
    }

    override fun skipToNext() {
        stop()
        playNextInQueue()
    }

    override fun clearQueue() {
        audioQueue.clear()
        processedStopIds.clear()
        stop()
        updateQueuedItemsState()
    }

    override fun getQueueCount(): Int = audioQueue.size

    private fun updateQueuedItemsState() {
        _queuedItems.value = audioQueue.toList()
    }

    override fun release() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }
}
