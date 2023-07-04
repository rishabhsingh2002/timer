package app.appsuccessor.sandtimer.view.fragment

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Point
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import app.appsuccessor.sandtimer.R
import app.appsuccessor.sandtimer.databinding.DialogTimePickerBinding
import app.appsuccessor.sandtimer.databinding.FragmentTimerBinding
import app.appsuccessor.sandtimer.view.util.clickTo
import app.appsuccessor.sandtimer.viewModel.TimerService


class TimerFragment : Fragment() {
    private lateinit var ui: FragmentTimerBinding

    private var timerStarted = false
    private lateinit var serviceIntent: Intent
    private var time = 0L
    private var countDownTimer: CountDownTimer? = null

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            time = intent.getLongExtra(TimerService.TIME_EXTRA, 0L)
            requireActivity().runOnUiThread {
                updateTimerViews() // Update the timer views on the UI thread
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        ui = FragmentTimerBinding.inflate(inflater, container, false)

//        setUpTabText()

        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui.start.clickTo { startStopTimer() }
        ui.cancel.clickTo { resetTimer() }

        ui.progressBar.clickTo {
            showTimePickerDialog()
        }

        serviceIntent = Intent(context?.applicationContext, TimerService::class.java)
        requireContext().registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            updateTime,
            IntentFilter(TimerService.TIMER_UPDATED)
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(updateTime)
    }

    private fun startStopTimer() {
        if (timerStarted) {
            stopTimer()
        } else {
            if (time > 0) {
                startTimer()
            } else {
                showTimePickerDialog()
//                Toast.makeText(requireContext(), "Please select a time", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun startTimer() {
        val milliseconds = time * 1000
        countDownTimer = object : CountDownTimer(milliseconds, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                time = millisUntilFinished / 1000
                updateTimerViews()
            }

            override fun onFinish() {
                stopTimer()
                time = 0
                updateTimerViews()
            }
        }

        countDownTimer?.start()

        ui.start.setImageDrawable(resources.getDrawable(R.drawable.ic_pause_timer))
        ui.progressBar.progressDrawable = resources.getDrawable(R.drawable.progress_drawable)
        timerStarted = true
        updateTimerViews() // Update the timer views with the initial time
    }

    private fun resetTimer() {
        stopTimer()
        time = 0
        ui.start.setImageDrawable(resources.getDrawable(R.drawable.ic_start_timer))
        updateTimerViews() // Update the timer views with the reset time
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        countDownTimer = null

        requireContext().stopService(serviceIntent)
        ui.start.setImageDrawable(resources.getDrawable(R.drawable.ic_resume_timer))
        ui.progressBar.progressDrawable =
            resources.getDrawable(R.drawable.progress_drawable_inactive)
        timerStarted = false
    }

    private fun updateTimerViews() {
        if (time <= 0) {
            ui.start.setImageDrawable(resources.getDrawable(R.drawable.ic_start_timer))
            timerStarted = false
        }

        val hours = (time / 3600).toInt()
        val minutes = ((time % 3600) / 60).toInt()
        val seconds = (time % 60).toInt()

        ui.hour.text = String.format("%02d", hours)
        ui.minute.text = String.format("%02d", minutes)
        ui.second.text = String.format("%02d", seconds)
    }


    private fun showTimePickerDialog() {
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).create()
        val binding = DialogTimePickerBinding.inflate(LayoutInflater.from(requireContext()))
        dialog.setView(binding.root)
        dialog.setCancelable(true)

        // Set up the number pickers
        binding.numberPickerHours.minValue = 0
        binding.numberPickerHours.maxValue = 23

        binding.numberPickerMinutes.minValue = 0
        binding.numberPickerMinutes.maxValue = 59

        binding.numberPickerSeconds.minValue = 0
        binding.numberPickerSeconds.maxValue = 59

        binding.ok.clickTo {
            val selectedTimeInSeconds =
                binding.numberPickerHours.value * 3600L + binding.numberPickerMinutes.value * 60L + binding.numberPickerSeconds.value
            time = selectedTimeInSeconds
            updateTimerViews()
            dialog.dismiss()
        }
        binding.cancel.clickTo {
            dialog.dismiss()
        }

//        // Adjust the dialog position and size after it is shown
//        val attributes = dialog.window?.attributes
//        attributes?.apply {
//            gravity = Gravity.CENTER
//            x = (32 * resources.displayMetrics.density).toInt()
//            y = 0
//            width = (getDisplayWidth() - 64 * resources.displayMetrics.density).toInt()
//            height = WindowManager.LayoutParams.WRAP_CONTENT
//        }
//        dialog.window?.attributes = attributes

        dialog.show()
    }

    private fun getDisplayWidth(): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager =
            requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }


//    private fun setUpTabText() {
////        ui.tab.left.text = "             "
////        ui.tab.center.text = "SAND TIMER"
////        ui.tab.center.setTextColor(resources.getColor(R.color.black))
////        ui.tab.right.text = "CLOCK"
////        ui.tab.right.setTextColor(resources.getColor(R.color.black_light))
////
////        ui.tab.right.clickTo {
////            val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
////            viewPager.currentItem = 1
////        }
//    }
}
