package com.example.notifyme

import ReminderAdapter
import ReminderPreferences
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity2_1 : AppCompatActivity() {

    private lateinit var reminderAdapter: ReminderAdapter
    private lateinit var recyclerView: RecyclerView
    private val remindersList = mutableListOf<Reminder>()
    private lateinit var reminderPreferences: ReminderPreferences

    // Launchers for adding and editing reminders
    private val editReminderLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                updateReminder(result.data)
            }
        }

    private val addReminderLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                addReminder(result.data)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2_1)

        // Initialize preferences and RecyclerView
        reminderPreferences = ReminderPreferences(this)
        setupRecyclerView()

        // Setup Floating Action Button to add a reminder
        findViewById<FloatingActionButton>(R.id.btn_add_reminder).setOnClickListener {
            val intent = Intent(this, ReminderDetailActivity1_1::class.java)
            addReminderLauncher.launch(intent)
        }

        // Load and display reminders
        loadReminders()

        // Create notification channel for reminders
        createNotificationChannel()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.rv_reminder)
        reminderAdapter = ReminderAdapter(
            remindersList,
            onEditClick = { reminder, position -> editReminder(reminder) },
            onDeleteClick = { position -> deleteReminder(position) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = reminderAdapter
    }

    private fun loadReminders() {
        remindersList.clear()
        remindersList.addAll(reminderPreferences.getAllReminders())
        reminderAdapter.notifyDataSetChanged() // Notify adapter to refresh
    }

    private fun editReminder(reminder: Reminder) {
        val intent = Intent(this, ReminderDetailActivity1_1::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("reminder_title", reminder.title)
            putExtra("reminder_description", reminder.description)
            putExtra("reminder_date", reminder.date)
            putExtra("reminder_time", reminder.time)
        }
        editReminderLauncher.launch(intent)
    }

    private fun addReminder(data: Intent?) {
        data?.let {
            val title = it.getStringExtra("reminder_title") ?: return
            val description = it.getStringExtra("reminder_description") ?: return
            val date = it.getStringExtra("reminder_date") ?: return
            val time = it.getStringExtra("reminder_time") ?: return

            // Check for duplicate reminders
            if (remindersList.any { reminder ->
                    reminder.title == title && reminder.date == date && reminder.time == time
                }) return

            // Create new reminder with unique ID
            val newReminder = Reminder(generateUniqueId(), title, description, date, time)
            remindersList.add(newReminder)
            reminderAdapter.notifyItemInserted(remindersList.size - 1)
            reminderPreferences.saveReminder(newReminder.id, title, description, date, time)
        }
    }

    private fun updateReminder(data: Intent?) {
        data?.let {
            val id = it.getIntExtra("reminder_id", -1)
            if (id != -1) {
                val title = it.getStringExtra("reminder_title") ?: return
                val description = it.getStringExtra("reminder_description") ?: return
                val date = it.getStringExtra("reminder_date") ?: return
                val time = it.getStringExtra("reminder_time") ?: return

                // Update the existing reminder in the list
                val index = remindersList.indexOfFirst { reminder -> reminder.id == id }
                if (index != -1) {
                    remindersList[index] = Reminder(id, title, description, date, time)
                    reminderAdapter.notifyItemChanged(index) // Notify adapter to refresh specific item
                    reminderPreferences.saveReminder(id, title, description, date, time)
                }
            }
        }
    }

    private fun deleteReminder(position: Int) {
        val reminderId = remindersList[position].id
        reminderPreferences.deleteReminder(reminderId)

        // Remove the reminder and update RecyclerView
        remindersList.removeAt(position)
        reminderAdapter.notifyItemRemoved(position)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ReminderChannel"
            val descriptionText = "Channel for Reminder Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("reminderChannel", name, importance).apply {
                description = descriptionText
                enableVibration(true) // Enable vibration
                enableLights(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun generateUniqueId(): Int {
        return (remindersList.maxOfOrNull { it.id } ?: 0) + 1
    }
}