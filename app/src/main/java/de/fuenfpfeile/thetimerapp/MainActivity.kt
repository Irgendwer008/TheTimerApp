package de.fuenfpfeile.thetimerapp

import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.CalendarView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.thetimerapp.R
import java.time.LocalDateTime
import java.time.Duration
import java.time.ZoneId

class MainActivity : AppCompatActivity() {

    private var countdownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("MyPreferences", MODE_PRIVATE)

        var targetTime: LocalDateTime

        sharedPref.getString("targetTimeStr", null)?.let { Log.d("string::::::::", it) }

        targetTime = if (sharedPref.getString("targetTimeStr", null) != null) {
            LocalDateTime.parse(sharedPref.getString("targetTimeStr", LocalDateTime.now().toString()))
        } else {
            LocalDateTime.now()
        }

        val tv_countdown = findViewById<TextView>(R.id.countdown)
        val calendarView = findViewById<CalendarView>(R.id.calendarView)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            startTimer(tv_countdown, year, month + 1, dayOfMonth, sharedPref)
        }

        calendarView.setDate(targetTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), true, true)
        startTimer(tv_countdown, targetTime.year, targetTime.monthValue, targetTime.dayOfMonth, sharedPref)
    }

    fun startTimer(textView: TextView, year: Int, month: Int, dayOfMonth: Int, sharedPref: SharedPreferences) {
        // Cancel any existing countdown
        countdownTimer?.cancel()

        // Target time is set to midnight of the selected day
        val targetTime = LocalDateTime.of(year, month, dayOfMonth, 0, 0)
        val currentTime = LocalDateTime.now()

        // Check if the selected time is in the future
        if (currentTime.isBefore(targetTime)) {
            val duration = Duration.between(currentTime, targetTime).seconds * 1000  // Duration in milliseconds

            val editor = sharedPref.edit()
            editor.putString("targetTimeStr", targetTime.toString())
            editor.apply()

            // Start a new countdown
            countdownTimer = object : CountDownTimer(duration, 1000) {
                override fun onTick(millisUntilFinished: Long) {
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
                        runOnUiThread {
                            textView.text = "$day_string Tage, $hour_string:$minute_string:$second_string"
                        }
                    }
                    else if (days == 1L) {
                        runOnUiThread {
                            textView.text = "1 Tag, $hour_string:$minute_string:$second_string"
                        }
                    }
                    else {
                        runOnUiThread {
                            textView.text = "$hour_string:$minute_string:$second_string"
                        }
                    }
                }

                override fun onFinish() {
                    // When the countdown is done, display "Ziel erreicht!"
                    runOnUiThread {
                        textView.text = "Ziel erreicht!"
                    }
                }
            }.start()
        } else {
            // If the selected date is in the past
            textView.text = "Datum liegt in der Vergangenheit!"
        }
    }
}
