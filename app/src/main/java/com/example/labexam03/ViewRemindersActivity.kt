package com.example.labexam03

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.lab03ex.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

class ViewRemindersActivity : AppCompatActivity() {

    private lateinit var remindersLayout: LinearLayout
    private lateinit var searchBar: EditText
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private var mediaPlayer: MediaPlayer? = null
    private var allReminders: MutableList<ReminderTask> = mutableListOf() // Store all reminders

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_reminders)

        remindersLayout = findViewById(R.id.remindersLayout)
        searchBar = findViewById(R.id.search_bar)
        sharedPreferences = getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)

        // Request exact alarm permission for Android 12+
        requestExactAlarmPermission()

        allReminders = getRemindersList() // Get all reminders
        displayReminders(allReminders) // Display all reminders initially

        // Set up the search functionality
        searchBar.addTextChangedListener { text ->
            val filteredReminders = allReminders.filter { reminder ->
                reminder.description.contains(text.toString(), ignoreCase = true)
            }
            displayReminders(filteredReminders)
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

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }

    private fun displayReminders(reminders: List<ReminderTask>) {
        remindersLayout.removeAllViews()

        if (reminders.isEmpty()) {
            val noRemindersText = TextView(this)
            noRemindersText.text = "No reminders set."
            remindersLayout.addView(noRemindersText)
            return
        }

        reminders.forEachIndexed { index, reminder ->
            val reminderView = LayoutInflater.from(this).inflate(R.layout.item_reminder, remindersLayout, false)
            val textViewDescription = reminderView.findViewById<TextView>(R.id.textViewDescription)
            val buttonEdit = reminderView.findViewById<Button>(R.id.buttonEdit)
            val buttonDelete = reminderView.findViewById<Button>(R.id.buttonDelete)
            val checkBoxDone = reminderView.findViewById<CheckBox>(R.id.checkBoxDone)

            textViewDescription.text = "${reminder.description} at ${reminder.hour}:${String.format("%02d", reminder.minute)}"
            checkBoxDone.isChecked = reminder.isDone

            buttonEdit.setOnClickListener {
                openEditReminder(reminder, index)
            }

            buttonDelete.setOnClickListener {
                deleteReminder(index)
            }

            checkBoxDone.setOnCheckedChangeListener { _, isChecked ->
                reminder.isDone = isChecked
                saveReminders(allReminders) // Save the full list

                // Stop the sound if marked done
                if (isChecked) {
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    mediaPlayer = null
                }
            }

            remindersLayout.addView(reminderView)
        }

        checkForDueReminders()
    }

    private fun checkForDueReminders() {
        val reminders = getRemindersList()
        val currentTime = Calendar.getInstance()

        reminders.forEach { reminder ->
            val reminderTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, reminder.hour)
                set(Calendar.MINUTE, reminder.minute)
                set(Calendar.SECOND, 0)
            }

            if (currentTime.after(reminderTime) && !reminder.isDone) {
                showAlert("${reminder.description} is due!")
                reminder.isDone = true
                saveReminders(reminders)
            }
        }
    }

    private fun updateWidget() {
        val intent = Intent(this, ReminderWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val widgetIds = AppWidgetManager.getInstance(this).getAppWidgetIds(ComponentName(this, ReminderWidgetProvider::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
        sendBroadcast(intent)
    }

    private fun getRemindersList(): MutableList<ReminderTask> {
        val json = sharedPreferences.getString("reminders_list", null)
        val type = object : TypeToken<MutableList<ReminderTask>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    private fun saveReminders(reminders: List<ReminderTask>) {
        val json = gson.toJson(reminders)
        with(sharedPreferences.edit()) {
            putString("reminders_list", json)
            apply()
        }

        // Update the widget after saving reminders
        updateWidget()

        // Schedule an alarm for the newly added reminder
        reminders.forEach { reminder ->
            if (!reminder.isDone) {
                scheduleReminder(reminder)
            }
        }
    }

    private fun scheduleReminder(reminder: ReminderTask) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("reminder_description", reminder.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(this, reminder.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    private fun openEditReminder(reminder: ReminderTask, position: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_reminder, null)
        val editTextDescription = dialogView.findViewById<EditText>(R.id.editTextDescription)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)

        editTextDescription.setText(reminder.description)
        timePicker.setHour(reminder.hour)
        timePicker.setMinute(reminder.minute)

        AlertDialog.Builder(this)
            .setTitle("Edit Reminder")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                reminder.description = editTextDescription.text.toString()
                reminder.hour = timePicker.hour
                reminder.minute = timePicker.minute
                saveReminders(allReminders)
                displayReminders(allReminders) // Refresh the list after editing
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteReminder(position: Int) {
        allReminders.removeAt(position)
        saveReminders(allReminders) // Save the updated list
        displayReminders(allReminders) // Refresh the list after deleting
    }

    private fun showAlert(message: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage(message)
        alertDialog.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        alertDialog.show()

        // Play sound for alert
        mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound)
        mediaPlayer?.start()

        // Vibration for alert
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(500)
    }
}
