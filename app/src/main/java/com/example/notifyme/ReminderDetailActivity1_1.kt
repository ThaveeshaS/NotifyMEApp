package com.example.notifyme

import ReminderPreferences
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ReminderDetailActivity1_1 : AppCompatActivity() {

    private lateinit var reminderPreferences: ReminderPreferences
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var selectDateButton: Button
    private lateinit var selectTimeButton: Button
    private lateinit var addReminderButton: Button
    private var reminderId: Int = -1
    private var selectedDate: String = ""
    private var selectedTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_detail1_1)

        reminderPreferences = ReminderPreferences(this)
        titleEditText = findViewById(R.id.reminderTitle)
        descriptionEditText = findViewById(R.id.reminderDescription)
        selectDateButton = findViewById(R.id.selectDateButton)
        selectTimeButton = findViewById(R.id.selectTimeButton)
        addReminderButton = findViewById(R.id.AddRbutton)

        reminderId = intent.getIntExtra("reminder_id", -1)

        if (reminderId != -1) {
            val reminder = reminderPreferences.getReminder(reminderId)
            reminder?.let {
                titleEditText.setText(it.title)
                descriptionEditText.setText(it.description)
                selectedDate = it.date
                selectedTime = it.time
                selectDateButton.text = selectedDate
                selectTimeButton.text = selectedTime
            } ?: run {
                Log.e("ReminderDetailActivity1_1", "Reminder with ID $reminderId not found.")
            }
        }

        selectDateButton.setOnClickListener { showDatePicker() }
        selectTimeButton.setOnClickListener { showTimePicker() }
        addReminderButton.setOnClickListener { saveReminder() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            selectDateButton.text = selectedDate
        }, year, month, day)
        datePicker.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            selectTimeButton.text = selectedTime
        }, hour, minute, true)
        timePicker.show()
    }

    private fun saveReminder() {
        val title = titleEditText.text.toString()
        val description = descriptionEditText.text.toString()

        if (title.isBlank() || description.isBlank()) {
            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (reminderId == -1) {
            reminderId = generateUniqueId()
        } else {
            reminderPreferences.deleteReminder(reminderId)
        }

        reminderPreferences.saveReminder(reminderId, title, description, selectedDate, selectedTime)

        val reminderTimeMillis = getReminderTimeInMillis(selectedDate, selectedTime)
        scheduleNotification(reminderId, title, description, reminderTimeMillis)

        addReminderButton.isEnabled = false

        sendResult(title, description) // Call sendResult with title and description
        updateWidget()

        finish()
    }

    private fun sendResult(title: String, description: String) {
        val resultIntent = Intent().apply {
            putExtra("reminder_id", reminderId) // ID of the reminder being edited
            putExtra("reminder_title", title)
            putExtra("reminder_description", description)
            putExtra("reminder_date", selectedDate)
            putExtra("reminder_time", selectedTime)
        }
        setResult(RESULT_OK, resultIntent)
    }

    private fun updateWidget() {
        val intent = Intent(this, ReminderWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }

        val appWidgetIds = AppWidgetManager.getInstance(this)
            .getAppWidgetIds(ComponentName(this, ReminderWidgetProvider::class.java))

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        sendBroadcast(intent)
    }

    private fun scheduleNotification(reminderId: Int, title: String, description: String, timeInMillis: Long) {
        val notificationIntent = Intent(this, MainActivity2_1::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, reminderId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, "reminderChannel")
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(reminderId, builder.build())
    }

    private fun getReminderTimeInMillis(date: String, time: String): Long {
        val calendar = Calendar.getInstance()

        val (day, month, year) = date.split("/").map { it.toInt() }
        val (hour, minute) = time.split(":").map { it.toInt() }

        calendar.set(year, month - 1, day, hour, minute)
        return calendar.timeInMillis
    }

    private fun calculateCountdown(date: String, time: String): String {
        val currentTimeMillis = System.currentTimeMillis()

        val (day, month, year) = date.split("/").map { it.toInt() }
        val (hour, minute) = time.split(":").map { it.toInt() }

        val calendar = Calendar.getInstance().apply {
            set(year, month - 1, day, hour, minute)
        }

        val reminderTimeMillis = calendar.timeInMillis
        val remainingMillis = reminderTimeMillis - currentTimeMillis

        if (remainingMillis < 0) return "00:00:00:00"

        val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis)
        val days = totalMinutes / (60 * 24)
        val hours = (totalMinutes % (60 * 24)) / 60
        val minutes = totalMinutes % 60

        return String.format("%02d:%02d:%02d", days, hours, minutes)
    }

    private fun generateUniqueId(): Int {
        val existingIds = reminderPreferences.getAllReminders().map { it.id }
        return (existingIds.maxOrNull() ?: 0) + 1
    }
}