package com.example.labexam03

import android.app.AlertDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.lab03ex.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class TimerActivity : AppCompatActivity() {

    private lateinit var tvHours: TextView
    private lateinit var tvMinutes: TextView
    private lateinit var tvSeconds: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnReset: Button
    private lateinit var btnBack: Button

    private var handler = Handler()
    private var startTime = 0L
    private var timeInMilliseconds = 0L
    private var timeSwapBuff = 0L
    private var updatedTime = 0L
    private var totalTimeInMillis = 0L
    private var isTimerRunning = false
    private var mediaPlayer: MediaPlayer? = null // MediaPlayer for playing sound

    private val updateTimerThread: Runnable = object : Runnable {
        override fun run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime
            updatedTime = totalTimeInMillis - timeInMilliseconds

            if (updatedTime <= 0) {
                updatedTime = 0
                handler.removeCallbacks(this)
                showTimeUpDialog()  // Show the dialog when the timer is up
                return
            }

            val secs = (updatedTime / 1000).toInt()
            val mins = secs / 60
            val hrs = mins / 60
            val seconds = secs % 60

            tvHours.text = String.format("%02d", hrs)
            tvMinutes.text = String.format("%02d", mins % 60)
            tvSeconds.text = String.format("%02d", seconds)

            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        tvHours = findViewById(R.id.tvHours)
        tvMinutes = findViewById(R.id.tvMinutes)
        tvSeconds = findViewById(R.id.tvSeconds)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        btnReset = findViewById(R.id.btnReset)
        btnBack = findViewById(R.id.btnBack)

        // Initialize total time to 0 at start
        resetTimer(false)

        btnStart.setOnClickListener {
            if (!isTimerRunning) {
                startTime = SystemClock.uptimeMillis()
                handler.postDelayed(updateTimerThread, 0)
                isTimerRunning = true
            }
        }

        btnStop.setOnClickListener {
            if (isTimerRunning) {
                timeSwapBuff += timeInMilliseconds
                handler.removeCallbacks(updateTimerThread)
                isTimerRunning = false
            }
        }

        btnReset.setOnClickListener {
            resetTimer()
        }

        btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Handle navigation button clicks
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_tips -> {
                    startActivity(Intent(this, TipsPageActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfilePageActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun showTimeUpDialog() {
        // Play the alert sound when the timer is up
        mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound) // Assuming the sound file is in res/raw folder
        mediaPlayer?.start() // Start playing the sound

        AlertDialog.Builder(this)
            .setTitle("Timer Up")
            .setMessage("Timer is up!")
            .setPositiveButton("OK") { _, _ ->
                resetTimer(false)
                stopSound() // Stop the sound when the user acknowledges the alert
            }
            .show()
    }

    // Stop the sound if it's playing
    private fun stopSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun onEditTimeClicked(view: View) {
        val id = view.id
        val timeText = (view as TextView).text.toString()
        val timeValue = timeText.toInt()

        val builder = AlertDialog.Builder(this)
        val editText = EditText(this).apply {
            setText(timeValue.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        builder.setTitle("Edit Time")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                val newValue = editText.text.toString().toInt()
                when (id) {
                    R.id.tvHours -> tvHours.text = String.format("%02d", newValue)
                    R.id.tvMinutes -> tvMinutes.text = String.format("%02d", newValue)
                    R.id.tvSeconds -> tvSeconds.text = String.format("%02d", newValue)
                }
                updateTotalTime()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateTotalTime() {
        val hours = tvHours.text.toString().toInt()
        val minutes = tvMinutes.text.toString().toInt()
        val seconds = tvSeconds.text.toString().toInt()
        totalTimeInMillis = (hours * 3600 + minutes * 60 + seconds) * 1000L
    }

    private fun resetTimer(reset: Boolean = true) {
        if (reset) {
            startTime = 0L
            timeSwapBuff = 0L
            timeInMilliseconds = 0L
            totalTimeInMillis = 0L
            tvHours.text = "00"
            tvMinutes.text = "00"
            tvSeconds.text = "00"
        }
        handler.removeCallbacks(updateTimerThread)
        isTimerRunning = false

        // Stop the sound if the timer is reset
        stopSound()
    }
}
