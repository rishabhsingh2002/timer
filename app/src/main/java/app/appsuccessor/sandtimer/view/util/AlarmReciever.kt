package app.appsuccessor.sandtimer.view.util

import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import app.appsuccessor.sandtimer.view.activity.AlarmDestinationActivity
import app.appsuccessor.sandtimer.view.activity.MainActivity


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // Retrieve the alarm data from the intent
        val title = intent?.getStringExtra("title")
        val audioResId = intent?.getStringExtra("audioResId")
        val alarmTime = intent?.getLongExtra("alarmTime", 0)

        try {
            Log.d("ONRELKRLE", "navigatied")
            val activity = Intent(context, AlarmDestinationActivity::class.java)
            activity.putExtra("title", title)
            activity.putExtra("audioResId", audioResId)
            activity.putExtra("alarmTime", alarmTime)
            activity.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context!!.startActivity(activity)
        } catch (e: Exception) {
            Log.d(TAG, e.message + "")
        }
    }
}
