package app.appsuccessor.sandtimer.view.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import app.appsuccessor.sandtimer.R
import app.appsuccessor.sandtimer.databinding.FragmentStopWatchBinding
import app.appsuccessor.sandtimer.model.LapTime
import app.appsuccessor.sandtimer.view.adapter.LapTimesAdapter
import app.appsuccessor.sandtimer.view.util.clickTo
import app.appsuccessor.sandtimer.view.util.clickToShow

class StopWatchFragment : Fragment() {
    private lateinit var ui: FragmentStopWatchBinding

    private val lapTimes = ArrayList<LapTime>()

    private var timerStarted = false
    private var time = 0L
    private var countDownTimer: CountDownTimer? = null
    private lateinit var lapTimesAdapter: LapTimesAdapter
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        ui = FragmentStopWatchBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("LapTimes", Context.MODE_PRIVATE)

        lapTimesAdapter = LapTimesAdapter(requireContext(), lapTimes)
//        setUpTabText()
        setUpSlider()
        retrieveLapTimes() // Retrieve stored lap times
        setUpRecyclerView()

        return ui.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        saveLapTimes() // Save lap times before destroying the view
    }


    private fun retrieveLapTimes() {
        val lapTimesString = sharedPreferences.getString("LapTimes", null)
        lapTimesString?.let {
            if (it.isNotEmpty()) {
                val lapTimesArray = it.split(",").mapNotNull { duration ->
                    if (duration.isNotEmpty()) {
                        LapTime(duration.toLong())
                    } else {
                        null
                    }
                }
                lapTimes.addAll(lapTimesArray)
                lapTimesAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun saveLapTimes() {
        val lapTimesString = lapTimes.joinToString(",") { lapTime ->
            lapTime.duration.toString()
        }
        sharedPreferences.edit().putString("LapTimes", lapTimesString).apply()
    }

    private fun setUpRecyclerView() {
        ui.rvLaps.layoutManager = LinearLayoutManager(requireContext()).apply {
            reverseLayout = true
        }
        ui.rvLaps.adapter = lapTimesAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui.start.clickToShow {
            ui.start.visibility = View.GONE
            ui.cancel.visibility = View.VISIBLE
            ui.linearLayout.visibility = View.VISIBLE
            startStopTimer()
        }

        ui.lap.clickToShow {
            if (timerStarted) {
                createLap()
            }
        }

        ui.cancel.clickToShow {
            ui.cancel.visibility = View.GONE
            ui.start.visibility = View.VISIBLE
            ui.reset.visibility = View.VISIBLE
            ui.lap.visibility = View.GONE
            if (timerStarted) {
                resetTimer()
            } else {
                startStopTimer()
            }
        }

        ui.reset.clickToShow {
            ui.reset.visibility = View.GONE
            ui.lap.visibility = View.VISIBLE
            ui.linearLayout.visibility = View.INVISIBLE
            lapTimes.clear()
            lapTimesAdapter.notifyDataSetChanged()
            if (timerStarted) {
                resetTimer()
            }
        }
    }

    private fun createLap() {
        val lapTime = LapTime(time)
        lapTimes.add(lapTime)
        lapTimesAdapter.notifyDataSetChanged()
    }

    private fun startStopTimer() {
        if (!timerStarted) {
            // Start the timer
            startTimer()
            ui.lap.visibility = View.VISIBLE // Show the lap button
            ui.reset.visibility = View.GONE // Hide the reset button
        } else {
            // Stop the timer
            stopTimer()
            ui.lap.visibility = View.GONE // Hide the lap button
            ui.reset.visibility = View.VISIBLE // Show the reset button
        }
    }


    private fun resetTimer() {
        stopTimer()
        time = 0
        updateTimerViews() // Update the timer views with the reset time
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                time++
                updateTimerViews()
            }

            override fun onFinish() {
                // Not used in this case
            }
        }
        countDownTimer?.start()
        timerStarted = true
        updateTimerViews() // Update the timer views with the initial time
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        timerStarted = false
        updateTimerViews() // Update the timer views with the current time
    }

    private fun updateTimerViews() {
        if (timerStarted) {
            ui.start.visibility = View.GONE // Hide the start button
            ui.cancel.visibility = View.VISIBLE // Show the cancel button
        } else {
            ui.start.visibility = View.VISIBLE // Show the start button
            ui.cancel.visibility = View.GONE // Hide the cancel button
        }

        val hours = (time / 3600).toInt()
        val minutes = ((time % 3600) / 60).toInt()
        val seconds = (time % 60).toInt()

        ui.hour.text = String.format("%02d", hours)
        ui.minute.text = String.format("%02d", minutes)
        ui.second.text = String.format("%02d", seconds)
    }

    private fun setUpSlider() {
        ui.apply {
            next.visibility = View.VISIBLE
            digital.visibility = View.VISIBLE
            next.clickToShow {
                previous.visibility = View.VISIBLE
                next.visibility = View.GONE
                digital.visibility = View.GONE
                analogClock.visibility = View.VISIBLE
            }
            previous.clickToShow {
                next.visibility = View.VISIBLE
                previous.visibility = View.GONE
                analogClock.visibility = View.GONE
                digital.visibility = View.VISIBLE
            }
        }
    }

//    private fun setUpTabText() {
//        ui.tab.left.text = "ALARM"
//        ui.tab.left.setTextColor(resources.getColor(R.color.black_light))
//        ui.tab.center.text = "STOPWATCH"
//        ui.tab.center.setTextColor(resources.getColor(R.color.black))
//        ui.tab.right.text = "BEDTIME"
//        ui.tab.right.setTextColor(resources.getColor(R.color.black_light))
//        ui.tab.left.clickTo {
//            val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
//            viewPager.currentItem = 2
//        }
//
//        ui.tab.right.clickTo {
//            val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
//            viewPager.currentItem = 4
//        }
//    }
}
