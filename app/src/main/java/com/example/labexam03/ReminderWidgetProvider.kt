package com.example.labexam03

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews
import com.example.lab03ex.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ReminderWidgetProvider : AppWidgetProvider() {

    private val gson = Gson()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE == intent.action ||
            "com.example.labexam03.ACTION_UPDATE_REMINDER_WIDGET" == intent.action) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, ReminderWidgetProvider::class.java)
            )
            for (widgetId in widgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        val reminders = getRemindersList(sharedPreferences)

        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        val reminderText = reminders.joinToString("\n") { it.description }

        views.setTextViewText(R.id.tvReminder, if (reminderText.isNotEmpty()) reminderText else "No upcoming reminders")

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getRemindersList(sharedPreferences: SharedPreferences): List<ReminderTask> {
        val json = sharedPreferences.getString("reminders_list", null)
        val type = object : TypeToken<List<ReminderTask>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}
