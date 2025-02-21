package com.example.valuefy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var etTranscribedText: EditText
    private lateinit var btnStartRecording: Button
    private lateinit var btnStopRecording: Button
    private lateinit var tvStatus: TextView
    private lateinit var btnShare: Button
    private lateinit var btnAddEvent: Button
    private lateinit var btnSaveTask: Button
    private lateinit var btnViewTasks: Button
    private lateinit var taskExtractor: TaskExtractor
    private lateinit var taskManager: TaskManager
    private lateinit var actionExtractor: ActionExtractor
    private lateinit var calendarHelper: CalendarHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        etTranscribedText = findViewById(R.id.etTranscribedText)
        btnStartRecording = findViewById(R.id.btnStartRecording)
        btnStopRecording = findViewById(R.id.btnStopRecording)
        tvStatus = findViewById(R.id.tvStatus)
        btnShare = findViewById(R.id.btnShare)
        btnAddEvent = findViewById(R.id.btnAddEvent)
        btnSaveTask = findViewById(R.id.btnSaveTask)
        btnViewTasks = findViewById(R.id.btnViewTasks) // ✅ Initialize View Tasks Button

        // Initialize helpers
        taskExtractor = TaskExtractor()
        taskManager = TaskManager(this)
        actionExtractor = ActionExtractor()
        calendarHelper = CalendarHelper(this)

        checkAndRequestPermissions()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(
            SpeechToTextHelper(etTranscribedText, btnStopRecording, btnStartRecording, tvStatus, taskExtractor, taskManager)
        )

        // Start Recording Button
        btnStartRecording.setOnClickListener {
            startSpeechRecognition()
        }

        // Stop Recording Button
        btnStopRecording.setOnClickListener {
            stopSpeechRecognition()
        }

        // Share Text Button
        btnShare.setOnClickListener {
            val textToShare = etTranscribedText.text.toString().trim()
            if (textToShare.isNotEmpty()) {
                val shareHelper = ShareHelper(this)
                shareHelper.shareText(textToShare)
            } else {
                Toast.makeText(this, "Nothing to share!", Toast.LENGTH_SHORT).show()
            }
        }

        // Add Event Button
        btnAddEvent.setOnClickListener {
            val transcribedText = etTranscribedText.text.toString().trim()
            val (timestamp, timeText) = actionExtractor.extractMeetingDetails(transcribedText)

            if (timestamp != null) {
                calendarHelper.addEvent("Meeting", "Scheduled automatically by Valuefy", timestamp)
                Toast.makeText(this, "Meeting added to calendar!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No valid meeting details found!", Toast.LENGTH_SHORT).show()
            }
        }

        // Save Task Button
        btnSaveTask.setOnClickListener {
            val extractedTasks = taskExtractor.extractTasks(etTranscribedText.text.toString())
            if (extractedTasks.isNotEmpty()) {
                extractedTasks.forEach { taskManager.saveTask(it) }
                Toast.makeText(this, "Tasks saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No tasks found!", Toast.LENGTH_SHORT).show()
            }
        }

        // View Tasks Button
        btnViewTasks.setOnClickListener {
            val savedTasks = taskManager.getTasks()
            if (savedTasks.isNotEmpty()) {
                val taskList = savedTasks.joinToString("\n") // Show tasks in readable format
                etTranscribedText.setText(taskList) // Display in EditText
            } else {
                Toast.makeText(this, "No tasks saved yet!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
        }
        speechRecognizer.startListening(intent)
        btnStartRecording.isEnabled = false
        btnStopRecording.isEnabled = true
        tvStatus.text = "Listening..."
    }

    private fun stopSpeechRecognition() {
        speechRecognizer.stopListening()
        btnStartRecording.isEnabled = true
        btnStopRecording.isEnabled = false
        tvStatus.text = "Tap to Start Recording"
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}
