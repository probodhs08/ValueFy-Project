package com.example.valuefy

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class SpeechToTextHelper(
    private val editText: EditText,
    private val stopButton: Button,
    private val startButton: Button,
    private val statusText: TextView,
    private val taskExtractor: TaskExtractor,
    private val taskManager: TaskManager
) : RecognitionListener {

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val transcribedText = matches[0]
            editText.setText(transcribedText)

            val extractedTasks = taskExtractor.extractTasks(transcribedText)
            if (extractedTasks.isNotEmpty()) {
                extractedTasks.forEach { taskManager.saveTask(it) }
                Log.d("SpeechToTextHelper", "Tasks extracted and saved: $extractedTasks") // ✅ Debug Log
            } else {
                Log.d("SpeechToTextHelper", "No tasks found in transcription.")
            }
        } else {
            editText.setText("No speech detected. Try again!")
        }
        stopButton.isEnabled = false
        startButton.isEnabled = true
        statusText.setText("Tap to Start Recording")
    }

    override fun onError(error: Int) {
        val errorMessage = getErrorText(error)
        Log.e("SpeechToTextHelper", "Speech recognition error: $errorMessage")
        statusText.setText("Error: $errorMessage")
        stopButton.isEnabled = false
        startButton.isEnabled = true
    }

    override fun onReadyForSpeech(params: Bundle?) {
        statusText.setText("Ready to record...")
    }

    override fun onBeginningOfSpeech() {
        statusText.setText("Recording...")
    }

    override fun onEndOfSpeech() {
        statusText.setText("Processing...")
    }

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onPartialResults(partialResults: Bundle?) {}

    override fun onEvent(eventType: Int, params: Bundle?) {}

    override fun onBufferReceived(buffer: ByteArray?) {
        Log.d("SpeechToTextHelper", "Audio buffer received.")
    }

    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission denied"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer is busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
            else -> "Unknown error"
        }
    }
}
