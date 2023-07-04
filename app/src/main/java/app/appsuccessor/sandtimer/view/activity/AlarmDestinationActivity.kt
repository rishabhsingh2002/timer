package app.appsuccessor.sandtimer.view.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import app.appsuccessor.sandtimer.R
import app.appsuccessor.sandtimer.databinding.ActivityAlarmDestinationBinding
import app.appsuccessor.sandtimer.view.util.AlarmReceiver
import app.appsuccessor.sandtimer.view.util.clickTo
import java.util.*

class AlarmDestinationActivity : AppCompatActivity() {
    private lateinit var ui: ActivityAlarmDestinationBinding

    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var snoozePendingIntent: PendingIntent // Add this line
    private lateinit var mediaPlayer: MediaPlayer
    private var snoozeTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityAlarmDestinationBinding.inflate(layoutInflater)
        setContentView(ui.root)

        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_one)

        if (isNightTime()) {
            ui.mainBgNight.visibility = View.VISIBLE
            ui.mainBgNightBottom.visibility = View.VISIBLE
            ui.nightStarsTop.visibility = View.VISIBLE
            ui.mainBgDay.visibility = View.INVISIBLE
            ui.mainBgDayBottom.visibility = View.INVISIBLE
            ui.time.setTextColor(resources.getColor(R.color.white))
            ui.alarm.setTextColor(resources.getColor(R.color.white))
        } else {
            ui.mainBgDay.visibility = View.VISIBLE
            ui.mainBgDayBottom.visibility = View.VISIBLE
            ui.mainBgNight.visibility = View.INVISIBLE
            ui.mainBgNightBottom.visibility = View.INVISIBLE
            ui.nightStarsTop.visibility = View.INVISIBLE
            ui.time.setTextColor(resources.getColor(R.color.black))
            ui.alarm.setTextColor(resources.getColor(R.color.black))
        }

        // Retrieve the alarm data from the intent
        val title = intent?.getStringExtra("title")
        val audioResId = intent?.getStringExtra("audioResId")
        val alarmTime = intent?.getLongExtra("alarmTime", 0)

        // Start playing the ringtone
        if (audioResId != null) {
            val resId = resources.getIdentifier(audioResId, "raw", packageName)
            mediaPlayer = MediaPlayer.create(this, R.raw.alarm_one)
            mediaPlayer.isLooping = true
            mediaPlayer.start()
        }

        val alarmIntent = Intent(this, AlarmReceiver::class.java)
        alarmIntent.putExtra("title", title)
        alarmIntent.putExtra("audioResId", audioResId)
        alarmIntent.putExtra("alarmTime", alarmTime)
        // Create a new pending intent for the alarm
        pendingIntent = PendingIntent.getBroadcast(
            this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE
        )


        ui.apply {
            snooze.clickTo {
                snoozeAlarm()
                mediaPlayer.stop()
                mediaPlayer.release()
            }
            stop.clickTo {
                stopAlarm()
                mediaPlayer.stop()
                mediaPlayer.release()
            }
            spinnerTime.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {
                    override fun onItemSelected(
                        adapterView: AdapterView<*>?, view: View?, position: Int, id: Long
                    ) {
                        snoozeTime = adapterView?.getItemAtPosition(position).toString()

                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }

                    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                    }
                }
            val currentTime = getCurrentTime()
            time.text = "${currentTime.first}:${currentTime.second}"
        }

    }


    override fun onDestroy() {
        mediaPlayer.stop()
        mediaPlayer.release()
        super.onDestroy()
    }


    private fun stopAlarm() {
        // Cancel the pending intent
        alarmManager.cancel(pendingIntent)
        // Stop any ongoing ringtone playback
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        finish()
    }

    private fun snoozeAlarm() {
        // Cancel the current alarm
        alarmManager.cancel(pendingIntent)
        // Calculate the snooze time (1 minute)
        val title = intent?.getStringExtra("title")
        val audioResId = intent?.getStringExtra("audioResId")
        val alarmTime = intent?.getLongExtra("alarmTime", 0)

        val snoozeTimeInMillis = System.currentTimeMillis() + (1 * 60 * 1000) // 1 minute

        // Create a new alarm intent
        val snoozeIntent = Intent(this, AlarmReceiver::class.java)
        snoozeIntent.putExtra("title", title)
        snoozeIntent.putExtra("audioResId", audioResId)
        snoozeIntent.putExtra("alarmTime", snoozeTimeInMillis)

        // Create a new pending intent for the snooze alarm
        snoozePendingIntent = PendingIntent.getBroadcast(
            this, 0, snoozeIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the snooze alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, snoozeTimeInMillis, snoozePendingIntent
            )
        } else {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(snoozeTimeInMillis, snoozePendingIntent),
                snoozePendingIntent
            )
        }

        finish()
    }

    private fun getCurrentTime(): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return Pair(hour, minute)
    }

    private fun isNightTime(): Boolean {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        return hour < 6 || hour >= 18
    }
}
