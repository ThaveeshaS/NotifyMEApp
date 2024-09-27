package com.example.notifyme

import ReminderPreferences
import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderBroadcastReceiver : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        val reminderId = intent?.getIntExtra("reminder_id", 0) ?: return
        val title = intent?.getStringExtra("reminder_title")
        val description = intent?.getStringExtra("reminder_description")

        // Delete the reminder
        deleteReminder(context, reminderId)

        // Create and display the notification
        showNotification(context, reminderId, title, description)
    }

    private fun deleteReminder(context: Context?, reminderId: Int) {
        val reminderPreferences = ReminderPreferences(context!!)
        reminderPreferences.deleteReminder(reminderId)

        // Trigger widget update
        val widgetManager = AppWidgetManager.getInstance(context)
        val thisWidget = ComponentName(context, ReminderWidgetProvider::class.java)
        val appWidgetIds = widgetManager.getAppWidgetIds(thisWidget)

        // Update all widgets
        for (widgetId in appWidgetIds) {
            widgetManager.updateAppWidget(widgetId, RemoteViews(context.packageName, R.layout.widget_layout))
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(context: Context?, reminderId: Int, title: String?, description: String?) {
        // Create and display the notification
        val builder = NotificationCompat.Builder(context!!, "reminderChannel")
            .setSmallIcon(R.drawable.notification) // Replace with your app's icon
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)  // Default vibration and sound

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(reminderId, builder.build())
    }
}