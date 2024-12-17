package com.example.labexam03

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.lab03ex.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTimeSpent: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTimeSpent = findViewById(R.id.tvTimeSpent)

        // Start the timer
        TimerManager.startTimer()

        // Load previous session time
        loadPreviousSessionTime()

        // Button listeners
        findViewById<Button>(R.id.btnTaskList).setOnClickListener {
            // Navigate to Task List activity
            startActivity(Intent(this, TaskListActivity::class.java))
        }

        findViewById<Button>(R.id.btnTimer).setOnClickListener {
            // Navigate to Timer activity
            startActivity(Intent(this, TimerActivity::class.java))
        }

        findViewById<Button>(R.id.btnSetReminders).setOnClickListener {
            // Navigate to Set Reminders activity
            startActivity(Intent(this, SetReminderActivity::class.java))
        }

        findViewById<Button>(R.id.btnViewReminders).setOnClickListener {
            // Navigate to View Reminders activity
            startActivity(Intent(this, ViewRemindersActivity::class.java))
        }

        // Set up the BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Handle navigation item selections
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_tips -> {
                    val intent = Intent(this, TipsPageActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfilePageActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Update timer display every second
        updateTimerDisplay()
    }

    private fun updateTimerDisplay() {
        val hours = TimerManager.elapsedTime / 3600
        val minutes = (TimerManager.elapsedTime % 3600) / 60
        val seconds = TimerManager.elapsedTime % 60
        tvCurrentTime.text = "Time spent in this session: $hours hrs $minutes min $seconds sec"

        // Update display every second
        Handler().postDelayed({ updateTimerDisplay() }, 1000)
    }

    private fun loadPreviousSessionTime() {
        val sharedPreferences = getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)
        val timeSpent = sharedPreferences.getLong("time_spent", 0)
        val hours = timeSpent / 3600
        val minutes = (timeSpent % 3600) / 60
        tvTimeSpent.text = "You have spent $hours hrs $minutes min in the app."
    }

    override fun onPause() {
        super.onPause()
        saveSessionTime()
    }

    private fun saveSessionTime() {
        val sharedPreferences = getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putLong("time_spent", TimerManager.elapsedTime)
            apply()
        }
    }

    override fun onResume() {
        super.onResume()
        loadPreviousSessionTime()
        // No need to start the timer again as it's already managed by TimerManager
    }

    override fun onDestroy() {
        super.onDestroy()
        TimerManager.stopTimer()
    }
}
