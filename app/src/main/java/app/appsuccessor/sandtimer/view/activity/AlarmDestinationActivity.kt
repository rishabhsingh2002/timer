package app.appsuccessor.sandtimer.view.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.viewModels
import app.appsuccessor.sandtimer.R
import app.appsuccessor.sandtimer.databinding.ActivityAlarmDestinationBinding
import app.appsuccessor.sandtimer.view.adapter.AlarmAdapter
import app.appsuccessor.sandtimer.view.util.AlarmReceiver
import app.appsuccessor.sandtimer.view.util.clickTo
import app.appsuccessor.sandtimer.viewModel.AlarmViewModel
import java.util.*

class AlarmDestinationActivity : AppCompatActivity() {
    private lateinit var ui: ActivityAlarmDestinationBinding

    private lateinit var alarmManager: AlarmManager
    private var pendingIntent: PendingIntent? = null
    private lateinit var mediaPlayer: MediaPlayer
    val viewModel: AlarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityAlarmDestinationBinding.inflate(layoutInflater)
        setContentView(ui.root)

        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_one)

        alarmUpdateCheckBox()

        if (isNightTime()) {
            ui.mainBgNight.visibility = View.VISIBLE
            ui.mainBgNightBottom.visibility = View.VISIBLE
            ui.nightStarsTop.visibility = View.VISIBLE
            ui.mainBgDay.visibility = View.INVISIBLE
            ui.mainBgDayBottom.visibility = View.INVISIBLE
            ui.time.setTextColor(resources.getColor(R.color.white))
            ui.alarm.setTextColor(resources.getColor(R.color.white))
            ui.snoozeNight.visibility = View.VISIBLE
        } else {
            ui.mainBgDay.visibility = View.VISIBLE
            ui.mainBgDayBottom.visibility = View.VISIBLE
            ui.mainBgNight.visibility = View.INVISIBLE
            ui.mainBgNightBottom.visibility = View.INVISIBLE
            ui.nightStarsTop.visibility = View.INVISIBLE
            ui.time.setTextColor(resources.getColor(R.color.black))
            ui.alarm.setTextColor(resources.getColor(R.color.black))
            ui.snoozeDay.visibility = View.VISIBLE
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

        ui.apply {
            snooze.clickTo {
                snoozeLayout.visibility = View.VISIBLE
            }
            twoMinute.clickTo {
                try {
                    snoozeAlarm(2)
                    mediaPlayer.stop()
                    mediaPlayer.release()
                } catch (e: Exception) {
                }
            }
            fiveMinute.clickTo {
                try {
                    snoozeAlarm(5)
                    mediaPlayer.stop()
                    mediaPlayer.release()
                } catch (e: Exception) {
                }
            }
            tenMinute.clickTo {
                try {
                    snoozeAlarm(10)
                    mediaPlayer.stop()
                    mediaPlayer.release()
                } catch (e: Exception) {
                }
            }
            fifteenMinute.clickTo {
                try {
                    snoozeAlarm(15)
                    mediaPlayer.stop()
                    mediaPlayer.release()
                } catch (e: Exception) {
                }
            }
            stop.clickTo {
                stopAlarm()
            }
            val currentTime = getCurrentTime()
            time.text = "${currentTime.first}:${currentTime.second}"
            alarm.text = title ?: "ALARM"
        }

    }

    private fun alarmUpdateCheckBox() {
        // recyclerview set up
        viewModel.getAllAlarms().observe(this) { alarmList ->
            Log.d("dafadf", "$alarmList")
            for (alarm in alarmList) {
                if (alarm.days.isEmpty()) {
                    alarm.isEnabled = false
                    // Update the alarm in the storage mechanism (e.g., Room database)
                    viewModel.updateAlarm(alarm)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun stopAlarm() {
        // Cancel the pending intent if it is not null
        pendingIntent?.let { pi ->
            alarmManager?.cancel(pi)
        }

        // Close the activity
        finish()

        // Stop any ongoing ringtone playback
        if (::mediaPlayer.isInitialized) {
            try {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.release()
            } catch (e: Exception) {
                // Handle any exceptions that might occur
                e.printStackTrace()
            }
        }
    }

    private fun snoozeAlarm(time: Int) {
        // Cancel the current alarm
//        alarmManager.cancel(pendingIntent)

        val title = intent?.getStringExtra("title")
        val audioResId = intent?.getStringExtra("audioResId")
        val alarmTime = intent?.getLongExtra("alarmTime", 1)
        val snoozeTimeInMillis = System.currentTimeMillis() + (time * 60 * 1000) // 1 minute

        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        intent.putExtra("title", title)
        intent.putExtra("audioResId", audioResId)
        intent.putExtra("alarmTime", snoozeTimeInMillis)

        pendingIntent = PendingIntent.getBroadcast(
            this, 1, intent, PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, snoozeTimeInMillis, pendingIntent
            )
        } else {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(snoozeTimeInMillis, pendingIntent), pendingIntent
            )
        }
        Toast.makeText(this, "Alarm Snooze for $time minutes", Toast.LENGTH_SHORT).show()
        ui.snoozeLayout.visibility = View.GONE
        finishActivityAfterDelay()
    }

    private val handler = Handler(Looper.getMainLooper())

    // Call this function when you want to finish the activity after 1 second
    private fun finishActivityAfterDelay() {
        handler.postDelayed({
            finish()
        }, 1000) // 1000 milliseconds = 1 second
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
