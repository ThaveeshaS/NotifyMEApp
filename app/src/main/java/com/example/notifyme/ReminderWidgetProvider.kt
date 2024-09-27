package com.example.notifyme

import ReminderPreferences
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.util.Calendar

class ReminderWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        context?.let { ctx ->
            val reminderPreferences = ReminderPreferences(ctx)
            val reminders = reminderPreferences.getAllReminders() // Get all reminders from preferences

            // Loop through each app widget ID and update them
            appWidgetIds?.forEach { appWidgetId ->
                // Create RemoteViews for widget layout
                val views = RemoteViews(ctx.packageName, R.layout.widget_layout)

                // Display up to 5 reminders
                val maxReminders = 5
                for (i in 0 until maxReminders) {
                    if (i < reminders.size) {
                        val reminder = reminders[i]
                        when (i) {
                            0 -> {
                                views.setTextViewText(R.id.widget_title_1, reminder.title)
                                views.setTextViewText(R.id.widget_description_1, reminder.description)
                                views.setTextViewText(R.id.widget_date_1, "${reminder.date} ${reminder.time}")
                            }
                            1 -> {
                                views.setTextViewText(R.id.widget_title_2, reminder.title)
                                views.setTextViewText(R.id.widget_description_2, reminder.description)
                                views.setTextViewText(R.id.widget_date_2, "${reminder.date} ${reminder.time}")
                            }
                            2 -> {
                                views.setTextViewText(R.id.widget_title_3, reminder.title)
                                views.setTextViewText(R.id.widget_description_3, reminder.description)
                                views.setTextViewText(R.id.widget_date_3, "${reminder.date} ${reminder.time}")
                            }
                            3 -> {
                                views.setTextViewText(R.id.widget_title_4, reminder.title)
                                views.setTextViewText(R.id.widget_description_4, reminder.description)
                                views.setTextViewText(R.id.widget_date_4, "${reminder.date} ${reminder.time}")
                            }
                            4 -> {
                                views.setTextViewText(R.id.widget_title_5, reminder.title)
                                views.setTextViewText(R.id.widget_description_5, reminder.description)
                                views.setTextViewText(R.id.widget_date_5, "${reminder.date} ${reminder.time}")
                            }
                        }
                    } else {
                        // Clear unused views if there are fewer than 5 reminders
                        when (i) {
                            0 -> {
                                views.setTextViewText(R.id.widget_title_1, "")
                                views.setTextViewText(R.id.widget_description_1, "")
                                views.setTextViewText(R.id.widget_date_1, "")
                            }
                            1 -> {
                                views.setTextViewText(R.id.widget_title_2, "")
                                views.setTextViewText(R.id.widget_description_2, "")
                                views.setTextViewText(R.id.widget_date_2, "")
                            }
                            2 -> {
                                views.setTextViewText(R.id.widget_title_3, "")
                                views.setTextViewText(R.id.widget_description_3, "")
                                views.setTextViewText(R.id.widget_date_3, "")
                            }
                            3 -> {
                                views.setTextViewText(R.id.widget_title_4, "")
                                views.setTextViewText(R.id.widget_description_4, "")
                                views.setTextViewText(R.id.widget_date_4, "")
                            }
                            4 -> {
                                views.setTextViewText(R.id.widget_title_5, "")
                                views.setTextViewText(R.id.widget_description_5, "")
                                views.setTextViewText(R.id.widget_date_5, "")
                            }
                        }
                    }
                }

                // Update the widget using appWidgetManager
                appWidgetManager?.updateAppWidget(appWidgetId, views)
            }
        }
    }

    // Manually call this to update widgets when reminders change
    fun updateWidget(context: Context) {
        val intent = Intent(context, ReminderWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val ids = appWidgetManager.getAppWidgetIds(
            ComponentName(context, ReminderWidgetProvider::class.java)
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }
}