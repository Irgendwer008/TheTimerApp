package de.fuenfpfeile.thetimerapp

import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.RemoteViews
import com.example.thetimerapp.R
import java.time.Duration
import java.time.LocalDateTime


class UpdateService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 1000L // 1000ms = 1 second (for a real-time clock)

    // Runnable to update the clock
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateWidgetTime()
            handler.postDelayed(this, updateInterval) // Repeat every second
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Start the handler when the service is created
        handler.post(updateRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the handler when the service is destroyed
        handler.removeCallbacks(updateRunnable)
    }

    // Function to update the widget's time
    private fun updateWidgetTime() {

        val sharedPref: SharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        var targetTime = if (sharedPref.getString("targetTimeStr", null) != null) {
            LocalDateTime.parse(
                sharedPref.getString(
                    "targetTimeStr",
                    LocalDateTime.now().toString()
                )
            )
        } else {
            LocalDateTime.now()
        }
        val currentTime = LocalDateTime.now()

        // Update the widget layout
        val views = RemoteViews(packageName, R.layout.widget)

        // Check if the selected time is in the future
        if (currentTime.isBefore(targetTime)) {
            val millisUntilFinished = Duration.between(currentTime, targetTime).seconds * 1000  // Duration in milliseconds

            // Calculate days, hours, minutes, and seconds from millis
            val sec = millisUntilFinished / 1000
            val days = sec / (24 * 3600)
            val hours = (sec % (24 * 3600)) / 3600
            val minutes = (sec % 3600) / 60
            val seconds = sec % 60

            val day_string = days.toString()
            val hour_string = hours.toString().padStart(2, '0')
            val minute_string = minutes.toString().padStart(2, '0')
            val second_string = seconds.toString().padStart(2, '0')

            if (days > 1L) {
                views.setTextViewText(R.id.appwidget_text, "$day_string Tage $hour_string:$minute_string:$second_string")
            }
            else if (days == 1L) {
                views.setTextViewText(R.id.appwidget_text, "1 Tag $hour_string:$minute_string:$second_string")
            }
            else {
                views.setTextViewText(R.id.appwidget_text, "$hour_string:$minute_string:$second_string")
            }
        } else {
            // If the selected date is in the past
            views.setTextViewText(R.id.appwidget_text, "Datum liegt in der Vergangenheit!")
        }

        // Create an Intent to launch ExampleActivity
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)


        // Get the layout for the App Widget and attach an on-click listener
        // to the button
        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent)

        // Push the update to the widget
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val thisWidget = ComponentName(this, WidgetProvider::class.java)
        appWidgetManager.updateAppWidget(thisWidget, views)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // No binding required
    }
}