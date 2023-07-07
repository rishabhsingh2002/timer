package app.appsuccessor.sandtimer.view.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.VibrationEffect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.os.CountDownTimer
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import app.appsuccessor.sandtimer.R
import app.appsuccessor.sandtimer.databinding.DialogPermissonBinding
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
    private lateinit var vibrator: Vibrator

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
//        requestOverlayPermission()
        // Show the action bar
        (requireActivity() as AppCompatActivity).supportActionBar?.show()

        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

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
                vibrateDevice(1000) // Vibrate for 1 second
            }
        }
        countDownTimer?.start()
        ui.start.setImageDrawable(resources.getDrawable(R.drawable.ic_pause_timer))
        ui.progressBar.progressDrawable = resources.getDrawable(R.drawable.progress_drawable)
        timerStarted = true
        updateTimerViews() // Update the timer views with the initial time
    }

    private fun vibrateDevice(duration: Long) {
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // For devices running Android 8.0 and above
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        duration,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                // For devices running below Android 8.0
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        }
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
        dialog.show()
    }

    private fun showPermissionPickerDialog() {
        val dialog = Dialog(requireContext(), R.style.FullScreenDialogTheme)
        val binding = DialogPermissonBinding.inflate(LayoutInflater.from(requireContext()))
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)

        binding.allow.clickTo {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            val uri = Uri.fromParts("package", context?.packageName, null)
            intent.data = uri
            startActivity(intent)

            dialog.dismiss() // Dismiss the dialog after starting the settings activity
        }

        dialog.show()
    }



    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(
                requireContext()
            )
        ) {
            showPermissionPickerDialog()
        } else {
            // Permission Granted - Overlay functionality is available
        }
    }


}
