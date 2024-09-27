import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.notifyme.Reminder
import com.example.notifyme.ReminderWidgetProvider

class ReminderPreferences(private val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("ReminderPrefs", Context.MODE_PRIVATE)

    // Save a reminder
    fun saveReminder(id: Int, title: String, description: String, date: String, time: String) {
        val editor = sharedPreferences.edit()
        editor.putString("reminder_${id}_title", title)
        editor.putString("reminder_${id}_description", description)
        editor.putString("reminder_${id}_date", date)
        editor.putString("reminder_${id}_time", time)
        editor.apply()
    }

    // Retrieve a single reminder
    fun getReminder(id: Int): Reminder? {
        val title = sharedPreferences.getString("reminder_${id}_title", null)
        val description = sharedPreferences.getString("reminder_${id}_description", null)
        val date = sharedPreferences.getString("reminder_${id}_date", null)
        val time = sharedPreferences.getString("reminder_${id}_time", null)

        return if (title != null && description != null && date != null && time != null) {
            Reminder(id, title, description, date, time)
        } else {
            null
        }
    }

    // Retrieve all reminders
    fun getAllReminders(): List<Reminder> {
        val reminders = mutableListOf<Reminder>()
        val allEntries = sharedPreferences.all
        val reminderKeys = allEntries.keys.filter { it.startsWith("reminder_") }

        // Process the keys to extract unique reminder IDs
        val uniqueIds = reminderKeys.mapNotNull {
            it.split("_")[1].toIntOrNull()
        }.distinct()

        uniqueIds.forEach { id ->
            val title = sharedPreferences.getString("reminder_${id}_title", null)
            val description = sharedPreferences.getString("reminder_${id}_description", null)
            val date = sharedPreferences.getString("reminder_${id}_date", null)
            val time = sharedPreferences.getString("reminder_${id}_time", null)

            if (title != null && description != null && date != null && time != null) {
                reminders.add(Reminder(id, title, description, date, time))
            }
        }

        return reminders
    }

    // Update a reminder
    fun updateReminder(reminder: Reminder) {
        val editor = sharedPreferences.edit()
        editor.putString("reminder_${reminder.id}_title", reminder.title)
        editor.putString("reminder_${reminder.id}_description", reminder.description)
        editor.putString("reminder_${reminder.id}_date", reminder.date)
        editor.putString("reminder_${reminder.id}_time", reminder.time)
        editor.apply()

        // Notify widget provider to update the widget
        updateWidget()
    }

    // Delete a reminder and update widget
    fun deleteReminder(id: Int) {
        val editor = sharedPreferences.edit()

        // Remove reminder entries from shared preferences
        editor.remove("reminder_${id}_title")
        editor.remove("reminder_${id}_description")
        editor.remove("reminder_${id}_date")
        editor.remove("reminder_${id}_time")
        editor.apply()

        // Notify widget provider to update the widget
        updateWidget()
    }

    // Update the widget after a reminder is deleted or updated
    private fun updateWidget() {
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