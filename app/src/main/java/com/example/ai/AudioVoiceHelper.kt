package com.example.ai

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * Handles:
 * 1. Playing Gemini TTS (`gemini-3.8-flash-tts`) audio payloads (PCM / WAV / MP3) with fallback to Android TextToSpeech.
 * 2. Recording microphone audio (`MediaRecorder`) and encoding to Base64 for `gemini-3.5-transcribe` & `gemini-3.8-live`.
 */
class AudioVoiceHelper(private val context: Context) {

    private var androidTts: TextToSpeech? = null
    private var isTtsReady = false
    private var mediaRecorder: MediaRecorder? = null
    private var currentAudioFile: File? = null

    init {
        androidTts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                androidTts?.language = Locale.US
                isTtsReady = true
            }
        }
    }

    suspend fun playGeminiOrFallbackTts(
        text: String,
        geminiAudioResult: Pair<String, String>?
    ) = withContext(Dispatchers.IO) {
        if (geminiAudioResult != null) {
            val (base64Data, mimeType) = geminiAudioResult
            try {
                val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                if (mimeType.contains("pcm", ignoreCase = true) || mimeType.contains("L16", ignoreCase = true)) {
                    playRawPcm24kHz(bytes)
                    return@withContext
                } else {
                    val tempFile = File(context.cacheDir, "taska_tts_out.wav")
                    FileOutputStream(tempFile).use { it.write(bytes) }
                    val player = MediaPlayer()
                    player.setDataSource(tempFile.absolutePath)
                    player.prepare()
                    player.start()
                    return@withContext
                }
            } catch (_: Exception) {
                // Fall through to local TTS if audio container format differs
            }
        }

        withContext(Dispatchers.Main) {
            if (isTtsReady) {
                androidTts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "taska_tts_${System.currentTimeMillis()}")
            }
        }
    }

    private fun playRawPcm24kHz(pcmBytes: ByteArray) {
        val sampleRate = 24000
        val minBufSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(pcmBytes.size)

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBufSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(pcmBytes, 0, pcmBytes.size)
        audioTrack.play()
    }

    fun startMicRecording(): Boolean {
        return try {
            stopMicRecordingAndGetBase64()
            val outFile = File(context.cacheDir, "taska_mic_input.m4a")
            if (outFile.exists()) outFile.delete()
            currentAudioFile = outFile

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioSamplingRate(16000)
            recorder.setAudioEncodingBitRate(64000)
            recorder.setOutputFile(outFile.absolutePath)
            recorder.prepare()
            recorder.start()
            mediaRecorder = recorder
            true
        } catch (e: Exception) {
            mediaRecorder = null
            false
        }
    }

    fun stopMicRecordingAndGetBase64(): String? {
        val recorder = mediaRecorder ?: return null
        mediaRecorder = null
        return try {
            recorder.stop()
            recorder.release()
            val file = currentAudioFile
            if (file != null && file.exists() && file.length() > 0) {
                val bytes = file.readBytes()
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else {
                null
            }
        } catch (e: Exception) {
            try {
                recorder.release()
            } catch (_: Exception) {
            }
            null
        }
    }

    fun stopSpeaking() {
        androidTts?.stop()
    }

    fun release() {
        stopMicRecordingAndGetBase64()
        androidTts?.stop()
        androidTts?.shutdown()
    }
}
