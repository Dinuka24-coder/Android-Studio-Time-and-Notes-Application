package com.example.labexam03

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.lab03ex.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class SetReminderActivity : AppCompatActivity() {

    private lateinit var editTextTaskDescription: EditText
    private lateinit var timePicker: TimePicker
    private lateinit var btnSaveReminder: Button
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_reminders)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        // Create notification channel when the activity starts
        NotificationUtils.createNotificationChannel(this)

        editTextTaskDescription = findViewById(R.id.editTextTaskDescription)
        timePicker = findViewById(R.id.timePicker)
        btnSaveReminder = findViewById(R.id.btnSaveReminder)
        sharedPreferences = getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)

        btnSaveReminder.setOnClickListener {
            saveReminder()
        }

        // Set up the bottom navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_tips -> {
                    startActivity(Intent(this, TipsPageActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfilePageActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun saveReminder() {
        val description = editTextTaskDescription.text.toString()
        val hour = timePicker.hour
        val minute = timePicker.minute

        if (description.isBlank()) {
            showAlert("Reminder description cannot be empty")
            return
        }

        // Get the next ID based on the existing reminders
        val reminders = getRemindersList()
        val nextId = if (reminders.isNotEmpty()) {
            reminders.maxOf { it.id } + 1
        } else {
            1 // Start from 1 if no reminders exist
        }

        val reminder = ReminderTask(nextId, description, hour, minute, false)
        reminders.add(reminder)

        val json = gson.toJson(reminders)
        with(sharedPreferences.edit()) {
            putString("reminders_list", json)
            apply()
        }

        // Schedule the reminder
        scheduleReminder(reminder)

        // Send a notification that the reminder has been created
        sendNotification(description)

        // Update the widget after adding a new reminder
        val widgetUpdateIntent = Intent("com.example.labexam03.ACTION_UPDATE_REMINDER_WIDGET")
        sendBroadcast(widgetUpdateIntent)

        showAlert("Reminder set successfully")
    }

    private fun scheduleReminder(reminder: ReminderTask) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("reminder_description", reminder.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(this, reminder.id, intent, PendingIntent.FLAG_IMMUTABLE)

        val reminderTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
        }

        // Check if the app can schedule exact alarms (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime.timeInMillis, pendingIntent)
        } catch (e: SecurityException) {
            e.printStackTrace()
            showAlert("Unable to schedule exact alarm. Please enable exact alarms in system settings.")
        }
    }

    private fun sendNotification(taskDescription: String) {
        val intent = Intent(this, SetReminderActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = NotificationCompat.Builder(this, NotificationUtils.getChannelId())
            .setSmallIcon(R.drawable.ic_notification)  // Make sure you have a valid drawable resource
            .setContentTitle("Reminder Created")
            .setContentText("Reminder for task: $taskDescription has been created")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }

    private fun getRemindersList(): MutableList<ReminderTask> {
        val json = sharedPreferences.getString("reminders_list", null)
        val type = object : TypeToken<MutableList<ReminderTask>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    private fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Reminder")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
