package com.nothing.glyphbattery.data.audio

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

class ClapDetectorSource(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val tag = "ClapDetectorSource"

    private val _isListening = MutableStateFlow(false)
    val isListening = _isListening.asStateFlow()

    private val _clapTriggeredTimestamp = MutableStateFlow(0L)
    val clapTriggeredTimestamp = _clapTriggeredTimestamp.asStateFlow()

    private var recordingJob: Job? = null
    private var audioRecord: AudioRecord? = null

    fun startListening() {
        if (_isListening.value) return

        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            audioRecord?.startRecording()
            _isListening.value = true

            recordingJob = scope.launch(Dispatchers.IO) {
                val buffer = ShortArray(bufferSize)
                var lastPeakTime = 0L
                var clapCount = 0

                while (isActive && _isListening.value) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        var maxAmp = 0
                        for (i in 0 until read) {
                            val amp = abs(buffer[i].toInt())
                            if (amp > maxAmp) maxAmp = amp
                        }

                        // Clap sound impulse threshold
                        if (maxAmp > 18000) {
                            val now = System.currentTimeMillis()
                            val diff = now - lastPeakTime
                            if (diff in 150..600) {
                                // Double clap detected!
                                Log.i(tag, "Double clap acoustic pattern detected!")
                                _clapTriggeredTimestamp.value = now
                                clapCount = 0
                                lastPeakTime = 0L
                            } else {
                                clapCount = 1
                                lastPeakTime = now
                            }
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.w(tag, "Record audio permission not granted. Clap detector paused.")
        } catch (e: Exception) {
            Log.w(tag, "Error starting clap detector: ${e.message}")
        }
    }

    fun stopListening() {
        _isListening.value = false
        recordingJob?.cancel()
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
    }

    fun triggerManualClap() {
        _clapTriggeredTimestamp.value = System.currentTimeMillis()
    }
}
