package com.jrlabs.audiocity.services

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.jrlabs.audiocity.data.model.AudioQueueItem
import com.jrlabs.audiocity.data.model.Stop
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.LinkedList
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioService @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _currentText = MutableStateFlow<String?>(null)
    val currentText: StateFlow<String?> = _currentText.asStateFlow()

    private val _currentQueueItem = MutableStateFlow<AudioQueueItem?>(null)
    val currentQueueItem: StateFlow<AudioQueueItem?> = _currentQueueItem.asStateFlow()

    private val _queuedItems = MutableStateFlow<List<AudioQueueItem>>(emptyList())
    val queuedItems: StateFlow<List<AudioQueueItem>> = _queuedItems.asStateFlow()

    private val audioQueue = LinkedList<AudioQueueItem>()
    private val processedStopIds = mutableSetOf<String>()

    init {
        textToSpeech = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale("es", "ES"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default Spanish
                textToSpeech?.setLanguage(Locale("es"))
            }

            // Set speech rate (0.5 = slower, 1.0 = normal, 2.0 = faster)
            textToSpeech?.setSpeechRate(0.9f)

            // Set up progress listener
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isPlaying.value = true
                    _isPaused.value = false
                }

                override fun onDone(utteranceId: String?) {
                    _isPlaying.value = false
                    _currentText.value = null
                    // Play next item in queue
                    playNextInQueue()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isPlaying.value = false
                    _currentText.value = null
                    playNextInQueue()
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isPlaying.value = false
                    _currentText.value = null
                    playNextInQueue()
                }
            })

            isInitialized = true
        }
    }

    fun speak(text: String, language: String = "es") {
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

    fun enqueueStop(stop: Stop) {
        // Avoid duplicates
        if (processedStopIds.contains(stop.id)) return
        processedStopIds.add(stop.id)

        val queueItem = AudioQueueItem(
            id = UUID.randomUUID().toString(),
            stopId = stop.id,
            stopName = stop.name,
            text = stop.scriptEs,
            order = stop.order
        )

        // Add to queue maintaining order
        audioQueue.add(queueItem)
        audioQueue.sortBy { it.order }

        updateQueuedItemsState()

        // If nothing is playing, start playing
        if (!_isPlaying.value && !_isPaused.value) {
            playNextInQueue()
        }
    }

    fun playNextInQueue() {
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

    fun pause() {
        textToSpeech?.stop()
        _isPaused.value = true
        _isPlaying.value = false
    }

    fun resume() {
        if (_isPaused.value && _currentQueueItem.value != null) {
            _isPaused.value = false
            speak(_currentQueueItem.value!!.text)
        }
    }

    fun stop() {
        textToSpeech?.stop()
        _isPlaying.value = false
        _isPaused.value = false
        _currentText.value = null
        _currentQueueItem.value = null
    }

    fun skipToNext() {
        stop()
        playNextInQueue()
    }

    fun clearQueue() {
        audioQueue.clear()
        processedStopIds.clear()
        stop()
        updateQueuedItemsState()
    }

    fun getQueueCount(): Int = audioQueue.size

    private fun updateQueuedItemsState() {
        _queuedItems.value = audioQueue.toList()
    }

    fun release() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }
}
