package app.appsuccessor.sandtimer.view.fragment

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import app.appsuccessor.sandtimer.R
import app.appsuccessor.sandtimer.databinding.DialogAlarmDayBinding
import app.appsuccessor.sandtimer.databinding.DialogAlarmNightBinding
import app.appsuccessor.sandtimer.databinding.FragmentAlarmBinding
import app.appsuccessor.sandtimer.datasource.local.AppDatabase
import app.appsuccessor.sandtimer.model.Alarm
import app.appsuccessor.sandtimer.view.adapter.AlarmAdapter
import app.appsuccessor.sandtimer.view.util.AlarmReceiver
import app.appsuccessor.sandtimer.view.util.clickTo
import app.appsuccessor.sandtimer.view.util.isNightTime
import app.appsuccessor.sandtimer.viewModel.AlarmViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.util.Calendar

class AlarmFragment : Fragment(), (Alarm) -> Unit, AlarmAdapter.OnAlarmToggleListener {
    private lateinit var ui: FragmentAlarmBinding

    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent
    private var alarmTitle: String? = null
    private var selectedRingtoneUri: String? = null
    private var mediaPlayer: MediaPlayer? = null
    val viewModel: AlarmViewModel by viewModels()

    private lateinit var alarmAdapter: AlarmAdapter
    private val alarmList: MutableList<Alarm> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        ui = FragmentAlarmBinding.inflate(layoutInflater)

        if (isNightTime()) {
            ui.mainBgNight.visibility = View.VISIBLE
            ui.mainBgDay.visibility = View.GONE
            ui.mainBgNightBottom.visibility = View.VISIBLE
            ui.mainBgDayBottom.visibility = View.GONE
            ui.addAlarm.setImageDrawable(resources.getDrawable(R.drawable.ic_add_alarm))
            ui.nightStarsTop.visibility = View.VISIBLE
        } else {
            ui.mainBgDay.visibility = View.VISIBLE
            ui.mainBgNight.visibility = View.GONE
            ui.mainBgDayBottom.visibility = View.VISIBLE
            ui.mainBgNightBottom.visibility = View.GONE
            ui.addAlarm.setImageDrawable(resources.getDrawable(R.drawable.ic_add_clock))
            ui.nightStarsTop.visibility = View.GONE
        }

        // recyclerview set up
        viewModel.getAllAlarms().observe(viewLifecycleOwner) { alarmList ->
            ui.rcvAlarms.adapter = AlarmAdapter(requireContext(), alarmList, this, this)
        }

        ui.addAlarm.clickTo {
            if (isNightTime()) {
                showAlarmDialogNight()
            } else {
                showAlarmDialogDay()
            }
        }

        return ui.root
    }


    private fun showAlarmDialogDay() {
        val bottomSheet = Dialog(requireContext())
        bottomSheet.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val bindingSheet = DialogAlarmDayBinding.inflate(layoutInflater)
        bottomSheet.setContentView(bindingSheet.root)

        bindingSheet.cancel.clickTo {
            bottomSheet.dismiss()
        }
        bindingSheet.delete.clickTo {
            bottomSheet.dismiss()
        }
        daySetUPDay(bindingSheet)
        ringToneSetUpDay(bindingSheet)

        bindingSheet.save.clickTo {
            if (selectedRingtoneUri.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please select alarm sound", Toast.LENGTH_SHORT)
                    .show()
            } else {
                var hour = bindingSheet.timePicker.hour
                var minute = bindingSheet.timePicker.minute
                var formattedTime = String.format("%02d:%02d", hour, minute)
                alarmTitle = bindingSheet.title.text?.toString()
                var selectedTime = getMillisecondsFromFormattedTime(formattedTime)

                val alarm = Alarm(
                    title = alarmTitle ?: "Alarm",
                    time = "$hour:$minute",
                    ringtoneUri = selectedRingtoneUri!!,
                    isEnabled = true
                )
                setAlarm(selectedTime!!)
                viewModel.insertAlarm(alarm)
                bottomSheet.dismiss()
            }
        }
        bottomSheet?.setOnDismissListener {
            stopMediaPlayer()
        }

        bottomSheet.setCanceledOnTouchOutside(true)
        bottomSheet.setCancelable(true)
        bottomSheet.show()
        bottomSheet.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        bottomSheet.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        bottomSheet.window?.attributes?.windowAnimations = R.style.DialogAnimation
        bottomSheet.window?.setGravity(Gravity.BOTTOM)
    }

    private fun updateAlarmDialogDay(alarm: Alarm) {
        val bottomSheet = Dialog(requireContext())
        bottomSheet.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val bindingSheet = DialogAlarmDayBinding.inflate(layoutInflater)
        bottomSheet.setContentView(bindingSheet.root)

        bindingSheet.cancel.clickTo {
            bottomSheet.dismiss()
        }
        bindingSheet.delete.clickTo {
            viewModel.deleteAlarm(alarm.id!!)
            bottomSheet.dismiss()
        }
        daySetUPDay(bindingSheet)
        ringToneSetUpDay(bindingSheet)

        //showing default values
        bindingSheet.apply {
            title.setText(alarm.title)
            alarmTitle = alarm.title
            selectedRingtoneUri = alarm.ringtoneUri
        }

        bindingSheet.save.clickTo {
            var hour = bindingSheet.timePicker.hour
            var minute = bindingSheet.timePicker.minute
            var formattedTime = String.format("%02d:%02d", hour, minute)
            alarmTitle = bindingSheet.title.text?.toString()
            var selectedTime = getMillisecondsFromFormattedTime(formattedTime)

            val updatedAlarm = Alarm(
                id = alarm.id,
                title = alarmTitle ?: "Alarm",
                time = "$hour:$minute",
                ringtoneUri = selectedRingtoneUri!!,
                isEnabled = true
            )
            setAlarm(selectedTime!!)
            viewModel.updateAlarm(updatedAlarm)
            bottomSheet.dismiss()
        }
        bottomSheet?.setOnDismissListener {
            stopMediaPlayer()
        }

        bottomSheet.setCanceledOnTouchOutside(true)
        bottomSheet.setCancelable(true)
        bottomSheet.show()
        bottomSheet.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        bottomSheet.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        bottomSheet.window?.attributes?.windowAnimations = R.style.DialogAnimation
        bottomSheet.window?.setGravity(Gravity.BOTTOM)
    }

    private fun showAlarmDialogNight() {
        val bottomSheet = Dialog(requireContext())
        bottomSheet.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val bindingSheet = DialogAlarmNightBinding.inflate(layoutInflater)
        bottomSheet.setContentView(bindingSheet.root)

        bindingSheet.cancel.clickTo {
            bottomSheet.dismiss()
        }
        bindingSheet.delete.clickTo {
            bottomSheet.dismiss()
        }
        daySetUP(bindingSheet)
        ringToneSetUp(bindingSheet)

        bindingSheet.save.clickTo {
            if (selectedRingtoneUri.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please select alarm sound", Toast.LENGTH_SHORT)
                    .show()
            } else {
                var hour = bindingSheet.timePicker.hour
                var minute = bindingSheet.timePicker.minute
                var formattedTime = String.format("%02d:%02d", hour, minute)
                alarmTitle = bindingSheet.title.text?.toString()
                var selectedTime = getMillisecondsFromFormattedTime(formattedTime)

                val alarm = Alarm(
                    title = alarmTitle ?: "Alarm",
                    time = "$hour:$minute",
                    ringtoneUri = selectedRingtoneUri!!,
                    isEnabled = true
                )
                setAlarm(selectedTime!!)
                viewModel.insertAlarm(alarm)
                bottomSheet.dismiss()
            }
        }
        bottomSheet?.setOnDismissListener {
            stopMediaPlayer()
        }

        bottomSheet.setCanceledOnTouchOutside(true)
        bottomSheet.setCancelable(true)
        bottomSheet.show()
        bottomSheet.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        bottomSheet.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        bottomSheet.window?.attributes?.windowAnimations = R.style.DialogAnimation
        bottomSheet.window?.setGravity(Gravity.BOTTOM)
    }

    private fun updateAlarmDialogNight(alarm: Alarm) {
        val bottomSheet = Dialog(requireContext())
        bottomSheet.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val bindingSheet = DialogAlarmNightBinding.inflate(layoutInflater)
        bottomSheet.setContentView(bindingSheet.root)

        bindingSheet.cancel.clickTo {
            bottomSheet.dismiss()
        }
        bindingSheet.delete.clickTo {
            viewModel.deleteAlarm(alarm.id!!)
            bottomSheet.dismiss()
        }
        daySetUP(bindingSheet)
        ringToneSetUp(bindingSheet)

        //showing default values
        bindingSheet.apply {
            title.setText(alarm.title)
            alarmTitle = alarm.title
            selectedRingtoneUri = alarm.ringtoneUri
        }

        bindingSheet.save.clickTo {
            var hour = bindingSheet.timePicker.hour
            var minute = bindingSheet.timePicker.minute
            var formattedTime = String.format("%02d:%02d", hour, minute)
            alarmTitle = bindingSheet.title.text?.toString()
            var selectedTime = getMillisecondsFromFormattedTime(formattedTime)

            val updatedAlarm = Alarm(
                id = alarm.id,
                title = alarmTitle ?: "Alarm",
                time = "$hour:$minute",
                ringtoneUri = selectedRingtoneUri!!,
                isEnabled = true
            )
            setAlarm(selectedTime!!)
            viewModel.updateAlarm(updatedAlarm)
            bottomSheet.dismiss()
        }
        bottomSheet?.setOnDismissListener {
            stopMediaPlayer()
        }

        bottomSheet.setCanceledOnTouchOutside(true)
        bottomSheet.setCancelable(true)
        bottomSheet.show()
        bottomSheet.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        bottomSheet.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        bottomSheet.window?.attributes?.windowAnimations = R.style.DialogAnimation
        bottomSheet.window?.setGravity(Gravity.BOTTOM)
    }

    private fun ringToneSetUpDay(bindingSheet: DialogAlarmDayBinding) {
        bindingSheet.apply {
            ringOne.clickTo {
                stopMediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_one)
                mediaPlayer?.start()
                selectedRingtoneUri =
                    "android.resource://${requireContext().packageName}/${R.raw.alarm_one}"
            }
            ringTwo.clickTo {
                stopMediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_two)
                mediaPlayer?.start()
                selectedRingtoneUri =
                    "android.resource://${requireContext().packageName}/${R.raw.alarm_two}"
            }
            ringThree.clickTo {
                stopMediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_three)
                mediaPlayer?.start()
                selectedRingtoneUri =
                    "android.resource://${requireContext().packageName}/${R.raw.alarm_three}"
            }
            ringFour.clickTo {
                stopMediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_four)
                mediaPlayer?.start()
                selectedRingtoneUri =
                    "android.resource://${requireContext().packageName}/${R.raw.alarm_four}"
            }
            ringFive.clickTo {
                stopMediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_five)
                mediaPlayer?.start()
                selectedRingtoneUri =
                    "android.resource://${requireContext().packageName}/${R.raw.alarm_five}"
            }
            ringSix.clickTo {
                stopMediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_six)
                mediaPlayer?.start()
                selectedRingtoneUri =
                    "android.resource://${requireContext().packageName}/${R.raw.alarm_six}"
            }
            ringSeven.clickTo {
                stopMediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_seven)
                mediaPlayer?.start()
                selectedRingtoneUri =
                    "android.resource://${requireContext().packageName}/${R.raw.alarm_seven}"
            }
            ringEight.clickTo {
                stopMediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_eight)
                mediaPlayer?.start()
                selectedRingtoneUri =
                    "android.resource://${requireContext().packageName}/${R.raw.alarm_eight}"
            }
            ringNine.clickTo {
                stopMediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_nine)
                mediaPlayer?.start()
                selectedRingtoneUri =
                    "android.resource://${requireContext().packageName}/${R.raw.alarm_nine}"
            }
            ringTen.clickTo {
                stopMediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_ten)
                mediaPlayer?.start()
                selectedRingtoneUri =
                    "android.resource://${requireContext().packageName}/${R.raw.alarm_ten}"
            }
        }
    }

    private fun ringToneSetUp(bindingSheet: DialogAlarmNightBinding) {
        bindingSheet.apply {
            ringOne.clickTo {
                stopMediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_one)
                mediaPlayer?.start()
                selectedRingtoneUri =
                    "android.resource://${requireContext().packageName}/${R.raw.alarm_one}"
            }
            ringTwo.clickTo {
                stopMediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_two)
                mediaPlayer?.start()
                selectedRingtoneUri =
                    "android.resource://${requireContext().packageName}/${R.raw.alarm_two}"
            }
            ringThree.clickTo {
                stopMediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_three)
                mediaPlayer?.start()
                selectedRingtoneUri =
                    "android.resource://${requireContext().packageName}/${R.raw.alarm_three}"
            }
            ringFour.clickTo {
                stopMediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_four)
                mediaPlayer?.start()
                selectedRingtoneUri =
                    "android.resource://${requireContext().packageName}/${R.raw.alarm_four}"
            }
            ringFive.clickTo {
                stopMediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_five)
                mediaPlayer?.start()
                selectedRingtoneUri =
                    "android.resource://${requireContext().packageName}/${R.raw.alarm_five}"
            }
            ringSix.clickTo {
                stopMediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_six)
                mediaPlayer?.start()
                selectedRingtoneUri =
                    "android.resource://${requireContext().packageName}/${R.raw.alarm_six}"
            }
            ringSeven.clickTo {
                stopMediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_seven)
                mediaPlayer?.start()
                selectedRingtoneUri =
                    "android.resource://${requireContext().packageName}/${R.raw.alarm_seven}"
            }
            ringEight.clickTo {
                stopMediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_eight)
                mediaPlayer?.start()
                selectedRingtoneUri =
                    "android.resource://${requireContext().packageName}/${R.raw.alarm_eight}"
            }
            ringNine.clickTo {
                stopMediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_nine)
                mediaPlayer?.start()
                selectedRingtoneUri =
                    "android.resource://${requireContext().packageName}/${R.raw.alarm_nine}"
            }
            ringTen.clickTo {
                stopMediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_ten)
                mediaPlayer?.start()
                selectedRingtoneUri =
                    "android.resource://${requireContext().packageName}/${R.raw.alarm_ten}"
            }
        }
    }

    private fun stopMediaPlayer() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
    }

    private fun daySetUPDay(bindingSheet: DialogAlarmDayBinding) {
        var isBackground1 = true
        bindingSheet.monday.setBackgroundResource(R.drawable.day_not_selected)
        bindingSheet.monday.setTextColor(resources.getColor(R.color.black))
        bindingSheet.monday.clickTo {
            if (isBackground1) {
                bindingSheet.monday.setBackgroundResource(R.drawable.day_selected_day)
                bindingSheet.monday.setTextColor(resources.getColor(R.color.white))
                isBackground1 = false
            } else {
                bindingSheet.monday.setBackgroundResource(R.drawable.day_not_selected)
                bindingSheet.monday.setTextColor(resources.getColor(R.color.black))
                isBackground1 = true
            }
        }
        var isBackground2 = true
        bindingSheet.tuesday.setBackgroundResource(R.drawable.day_not_selected)
        bindingSheet.tuesday.setTextColor(resources.getColor(R.color.black))
        bindingSheet.tuesday.clickTo {
            if (isBackground2) {
                bindingSheet.tuesday.setBackgroundResource(R.drawable.day_selected_day)
                bindingSheet.tuesday.setTextColor(resources.getColor(R.color.white))
                isBackground2 = false
            } else {
                bindingSheet.tuesday.setBackgroundResource(R.drawable.day_not_selected)
                bindingSheet.tuesday.setTextColor(resources.getColor(R.color.black))
                isBackground2 = true
            }
        }

        var isBackground3 = true
        bindingSheet.wednesday.setBackgroundResource(R.drawable.day_not_selected)
        bindingSheet.wednesday.setTextColor(resources.getColor(R.color.black))
        bindingSheet.wednesday.clickTo {
            if (isBackground3) {
                bindingSheet.wednesday.setBackgroundResource(R.drawable.day_selected_day)
                bindingSheet.wednesday.setTextColor(resources.getColor(R.color.white))
                isBackground3 = false
            } else {
                bindingSheet.wednesday.setBackgroundResource(R.drawable.day_not_selected)
                bindingSheet.wednesday.setTextColor(resources.getColor(R.color.black))
                isBackground3 = true
            }
        }

        var isBackground4 = true
        bindingSheet.thursday.setBackgroundResource(R.drawable.day_not_selected)
        bindingSheet.thursday.setTextColor(resources.getColor(R.color.black))
        bindingSheet.thursday.clickTo {
            if (isBackground4) {
                bindingSheet.thursday.setBackgroundResource(R.drawable.day_selected_day)
                bindingSheet.thursday.setTextColor(resources.getColor(R.color.white))
                isBackground4 = false
            } else {
                bindingSheet.thursday.setBackgroundResource(R.drawable.day_not_selected)
                bindingSheet.thursday.setTextColor(resources.getColor(R.color.black))
                isBackground4 = true
            }
        }
        var isBackground5 = true
        bindingSheet.friday.setBackgroundResource(R.drawable.day_not_selected)
        bindingSheet.friday.setTextColor(resources.getColor(R.color.black))
        bindingSheet.friday.clickTo {
            if (isBackground5) {
                bindingSheet.friday.setBackgroundResource(R.drawable.day_selected_day)
                bindingSheet.friday.setTextColor(resources.getColor(R.color.white))
                isBackground5 = false
            } else {
                bindingSheet.friday.setBackgroundResource(R.drawable.day_not_selected)
                bindingSheet.friday.setTextColor(resources.getColor(R.color.black))
                isBackground5 = true
            }
        }

        var isBackground6 = true
        bindingSheet.saturday.setBackgroundResource(R.drawable.day_not_selected)
        bindingSheet.saturday.setTextColor(resources.getColor(R.color.black))
        bindingSheet.saturday.clickTo {
            if (isBackground6) {
                bindingSheet.saturday.setBackgroundResource(R.drawable.day_selected_day)
                bindingSheet.saturday.setTextColor(resources.getColor(R.color.white))
                isBackground6 = false
            } else {
                bindingSheet.saturday.setBackgroundResource(R.drawable.day_not_selected)
                bindingSheet.saturday.setTextColor(resources.getColor(R.color.black))
                isBackground6 = true
            }
        }
        var isBackground7 = true
        bindingSheet.sunday.setBackgroundResource(R.drawable.day_not_selected)
        bindingSheet.sunday.setTextColor(resources.getColor(R.color.black))
        bindingSheet.sunday.clickTo {
            if (isBackground7) {
                bindingSheet.sunday.setBackgroundResource(R.drawable.day_selected_day)
                bindingSheet.sunday.setTextColor(resources.getColor(R.color.white))
                isBackground7 = false
            } else {
                bindingSheet.sunday.setBackgroundResource(R.drawable.day_not_selected)
                bindingSheet.sunday.setTextColor(resources.getColor(R.color.black))
                isBackground7 = true
            }
        }
    }

    private fun daySetUP(bindingSheet: DialogAlarmNightBinding) {
        var isBackground1 = true
        bindingSheet.monday.setBackgroundResource(R.drawable.day_not_selected)
        bindingSheet.monday.setTextColor(resources.getColor(R.color.black))
        bindingSheet.monday.clickTo {
            if (isBackground1) {
                bindingSheet.monday.setBackgroundResource(R.drawable.day_selected)
                bindingSheet.monday.setTextColor(resources.getColor(R.color.white))
                isBackground1 = false
            } else {
                bindingSheet.monday.setBackgroundResource(R.drawable.day_not_selected)
                bindingSheet.monday.setTextColor(resources.getColor(R.color.black))
                isBackground1 = true
            }
        }
        var isBackground2 = true
        bindingSheet.tuesday.setBackgroundResource(R.drawable.day_not_selected)
        bindingSheet.tuesday.setTextColor(resources.getColor(R.color.black))
        bindingSheet.tuesday.clickTo {
            if (isBackground2) {
                bindingSheet.tuesday.setBackgroundResource(R.drawable.day_selected)
                bindingSheet.tuesday.setTextColor(resources.getColor(R.color.white))
                isBackground2 = false
            } else {
                bindingSheet.tuesday.setBackgroundResource(R.drawable.day_not_selected)
                bindingSheet.tuesday.setTextColor(resources.getColor(R.color.black))
                isBackground2 = true
            }
        }

        var isBackground3 = true
        bindingSheet.wednesday.setBackgroundResource(R.drawable.day_not_selected)
        bindingSheet.wednesday.setTextColor(resources.getColor(R.color.black))
        bindingSheet.wednesday.clickTo {
            if (isBackground3) {
                bindingSheet.wednesday.setBackgroundResource(R.drawable.day_selected)
                bindingSheet.wednesday.setTextColor(resources.getColor(R.color.white))
                isBackground3 = false
            } else {
                bindingSheet.wednesday.setBackgroundResource(R.drawable.day_not_selected)
                bindingSheet.wednesday.setTextColor(resources.getColor(R.color.black))
                isBackground3 = true
            }
        }

        var isBackground4 = true
        bindingSheet.thursday.setBackgroundResource(R.drawable.day_not_selected)
        bindingSheet.thursday.setTextColor(resources.getColor(R.color.black))
        bindingSheet.thursday.clickTo {
            if (isBackground4) {
                bindingSheet.thursday.setBackgroundResource(R.drawable.day_selected)
                bindingSheet.thursday.setTextColor(resources.getColor(R.color.white))
                isBackground4 = false
            } else {
                bindingSheet.thursday.setBackgroundResource(R.drawable.day_not_selected)
                bindingSheet.thursday.setTextColor(resources.getColor(R.color.black))
                isBackground4 = true
            }
        }
        var isBackground5 = true
        bindingSheet.friday.setBackgroundResource(R.drawable.day_not_selected)
        bindingSheet.friday.setTextColor(resources.getColor(R.color.black))
        bindingSheet.friday.clickTo {
            if (isBackground5) {
                bindingSheet.friday.setBackgroundResource(R.drawable.day_selected)
                bindingSheet.friday.setTextColor(resources.getColor(R.color.white))
                isBackground5 = false
            } else {
                bindingSheet.friday.setBackgroundResource(R.drawable.day_not_selected)
                bindingSheet.friday.setTextColor(resources.getColor(R.color.black))
                isBackground5 = true
            }
        }

        var isBackground6 = true
        bindingSheet.saturday.setBackgroundResource(R.drawable.day_not_selected)
        bindingSheet.saturday.setTextColor(resources.getColor(R.color.black))
        bindingSheet.saturday.clickTo {
            if (isBackground6) {
                bindingSheet.saturday.setBackgroundResource(R.drawable.day_selected)
                bindingSheet.saturday.setTextColor(resources.getColor(R.color.white))
                isBackground6 = false
            } else {
                bindingSheet.saturday.setBackgroundResource(R.drawable.day_not_selected)
                bindingSheet.saturday.setTextColor(resources.getColor(R.color.black))
                isBackground6 = true
            }
        }
        var isBackground7 = true
        bindingSheet.sunday.setBackgroundResource(R.drawable.day_not_selected)
        bindingSheet.sunday.setTextColor(resources.getColor(R.color.black))
        bindingSheet.sunday.clickTo {
            if (isBackground7) {
                bindingSheet.sunday.setBackgroundResource(R.drawable.day_selected)
                bindingSheet.sunday.setTextColor(resources.getColor(R.color.white))
                isBackground7 = false
            } else {
                bindingSheet.sunday.setBackgroundResource(R.drawable.day_not_selected)
                bindingSheet.sunday.setTextColor(resources.getColor(R.color.black))
                isBackground7 = true
            }
        }
    }

    private fun setAlarm(time: Long) {
        alarmManager = context?.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        intent.putExtra("title", alarmTitle)
        intent.putExtra("audioResId", selectedRingtoneUri)
        intent.putExtra("alarmTime", time)

        pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, time, pendingIntent
            )
        } else {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(time, pendingIntent), pendingIntent
            )
        }
    }

    private fun getMillisecondsFromFormattedTime(formattedTime: String): Long? {
        val parts = formattedTime.split(":")
        if (parts.size == 2) {
            val hour = parts[0].toIntOrNull()
            val minute = parts[1].toIntOrNull()

            if (hour != null && minute != null) {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                return calendar.timeInMillis
            }
        }

        return null
    }

    override fun invoke(alarm: Alarm) {
        if (isNightTime()) {
            updateAlarmDialogNight(alarm)
        } else {
            updateAlarmDialogDay(alarm)
        }
    }

    override fun onToggle(alarm: Alarm, isChecked: Boolean) {
        alarm.isEnabled = isChecked
        // Update the alarm in the storage mechanism (e.g., Room database)
        viewModel.updateAlarm(alarm)
        // Update the alarm in the RecyclerView
        val position = alarmList.indexOf(alarm)
        if (position != -1) {
            alarmList[position] = alarm
            alarmAdapter.notifyItemChanged(position)
        }
    }
}