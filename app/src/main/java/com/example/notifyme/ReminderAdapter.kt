import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.notifyme.MainActivity2_1
import com.example.notifyme.R
import com.example.notifyme.Reminder
import java.text.SimpleDateFormat
import java.util.Locale

class ReminderAdapter(
    private var reminders: List<Reminder>,
    private val onEditClick: (Reminder, Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    private val timers = mutableMapOf<Int, CountDownTimer>()

    // ViewHolder definition
    class ReminderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tv_title)
        val description: TextView = view.findViewById(R.id.tv_description)
        val date: TextView = view.findViewById(R.id.tv_date)
        val time: TextView = view.findViewById(R.id.tv_time)
        val countdown: TextView = view.findViewById(R.id.tv_countdown)
        val deleteButton: ImageView = view.findViewById(R.id.RDelete)

        fun bind(reminder: Reminder, onDeleteClick: (Int) -> Unit, onEditClick: (Reminder, Int) -> Unit) {
            title.text = reminder.title
            description.text = reminder.description
            date.text = reminder.date
            time.text = reminder.time

            deleteButton.setOnClickListener {
                onDeleteClick(adapterPosition)
            }

            itemView.setOnClickListener {
                onEditClick(reminder, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]
        holder.bind(reminder, onDeleteClick, onEditClick)

        val timeDifference = calculateTimeDifference(reminder.date, reminder.time)

        // Cancel any existing timer
        timers[holder.adapterPosition]?.cancel()

        if (timeDifference > 0) {
            startCountdown(holder, timeDifference, reminder)
        } else {
            holder.countdown.text = "Expired"
            // Optionally handle expired reminders (like deleting or notifying)
            showNotification(holder.itemView.context, reminder)
        }
    }

    override fun onViewRecycled(holder: ReminderViewHolder) {
        super.onViewRecycled(holder)
        timers[holder.adapterPosition]?.cancel() // Cancel timers for recycled views
    }

    override fun getItemCount(): Int = reminders.size

    // DiffUtil callback to update reminders list efficiently
    fun updateReminderList(newReminders: List<Reminder>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = reminders.size
            override fun getNewListSize(): Int = newReminders.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return reminders[oldItemPosition].id == newReminders[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return reminders[oldItemPosition] == newReminders[newItemPosition]
            }
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        reminders = newReminders
        diffResult.dispatchUpdatesTo(this)

        // Restart timers for all visible reminders
        notifyDataSetChanged()  // Optional, use if necessary
    }

    private fun calculateTimeDifference(date: String, time: String): Long {
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return try {
            val dateTime = "$date $time"
            val reminderTime = format.parse(dateTime)?.time ?: 0L
            reminderTime - System.currentTimeMillis()
        } catch (e: Exception) {
            0L
        }
    }

    private fun startCountdown(holder: ReminderViewHolder, timeDifference: Long, reminder: Reminder) {
        val timer = object : CountDownTimer(timeDifference, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                holder.countdown.text = formatTimeLeft(millisUntilFinished)
            }

            override fun onFinish() {
                holder.countdown.text = "Time's up!"
                showNotification(holder.itemView.context, reminder)
            }
        }.start()
        timers[holder.adapterPosition] = timer
    }

    private fun formatTimeLeft(millisUntilFinished: Long): String {
        val hours = (millisUntilFinished / (1000 * 60 * 60)).toInt()
        val minutes = (millisUntilFinished / (1000 * 60) % 60).toInt()
        val seconds = (millisUntilFinished / 1000 % 60).toInt()
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    // Notification function to trigger when countdown hits 0
    @SuppressLint("MissingPermission")
    private fun showNotification(context: Context, reminder: Reminder) {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationIntent = Intent(context, MainActivity2_1::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, reminder.id ?: 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "reminderChannel")
            .setSmallIcon(R.drawable.notification) // Set your notification icon here
            .setContentTitle("Reminder: ${reminder.title}")
            .setContentText("The time for this reminder has ended!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(reminder.id ?: 0, builder.build())
    }
}