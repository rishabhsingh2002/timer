package app.appsuccessor.sandtimer.view.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.NumberPicker
import androidx.viewpager2.widget.ViewPager2
import app.appsuccessor.sandtimer.R
import app.appsuccessor.sandtimer.databinding.FragmentBedTimeBinding
import app.appsuccessor.sandtimer.view.util.clickTo
import app.appsuccessor.sandtimer.view.util.clickToShow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.min

class BedTimeFragment : Fragment() {
    private lateinit var ui: FragmentBedTimeBinding

    private var selectedTimeForWakeUp: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        ui = FragmentBedTimeBinding.inflate(layoutInflater)

        setUpNumberPickup()

        setUpSpinners()

        ui.apply {
            calculate.clickToShow {
                calculate.visibility = View.INVISIBLE
                begin.visibility = View.VISIBLE

                titleOne.visibility = View.INVISIBLE
                titleTwo.visibility = View.VISIBLE
                if (!selectedTimeForWakeUp.isNullOrEmpty()) {
                    titleTwo.text = "For a $selectedTimeForWakeUp go to bed, you should wake up at:"
                }
                seekBarLayout.visibility = View.GONE
                firstSuggestion.visibility = View.VISIBLE
                secondSuggestion.visibility = View.VISIBLE
                thirdSuggestion.visibility = View.VISIBLE
            }
            begin.clickToShow {
                begin.visibility = View.INVISIBLE
                calculate.visibility = View.VISIBLE

                titleOne.visibility = View.VISIBLE
                titleTwo.visibility = View.INVISIBLE

                seekBarLayout.visibility = View.VISIBLE
                firstSuggestion.visibility = View.GONE
                secondSuggestion.visibility = View.GONE
                thirdSuggestion.visibility = View.GONE
            }

        }

        return ui.root
    }

    private fun setUpSpinners() {
        ui.apply {
            spinnerReason.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {
                    override fun onItemSelected(
                        adapterView: AdapterView<*>?, view: View?, position: Int, id: Long
                    ) {


                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }

                    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                    }
                }
            spinnerTime.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {
                    override fun onItemSelected(
                        adapterView: AdapterView<*>?, view: View?, position: Int, id: Long
                    ) {

                        selectedTimeForWakeUp = adapterView?.getItemAtPosition(position).toString()
                        Log.d("djfhald", "$selectedTimeForWakeUp")
                        if (!selectedTimeForWakeUp.isNullOrEmpty()) {
                            calculateTimes(selectedTimeForWakeUp!!)
                        }

                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }

                    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                    }
                }
        }
    }

    private fun setUpNumberPickup() {
        ui.apply {
            val calendar = Calendar.getInstance()
            val hourDay = calendar.get(Calendar.HOUR_OF_DAY)
            val minuteDay = calendar.get(Calendar.MINUTE)
            hour.text = String.format("%02d", hourDay)
            minute.text = String.format("%02d", minuteDay)
        }
    }

    private fun calculateTimes(selectedTime: String) {
        val timeFormat = SimpleDateFormat("hh:mma", Locale.getDefault())
        val parsedSelectedTime = timeFormat.parse(selectedTime)

        val calendar = Calendar.getInstance()
        calendar.time = parsedSelectedTime!!

        calendar.add(Calendar.HOUR, 9)
        val timeAfter9Hours = timeFormat.format(calendar.time)

        calendar.time = parsedSelectedTime
        calendar.add(Calendar.MINUTE, (7.5 * 60).toInt())
        val timeAfter7_5Hours = timeFormat.format(calendar.time)

        calendar.time = parsedSelectedTime
        calendar.add(Calendar.MINUTE, (6 * 60).toInt())
        val timeAfter6Hours = timeFormat.format(calendar.time)

        val timeAfter9HoursValue = timeAfter9Hours.substring(0, 5) // Extract the time part (hh:mm)
        val timeAfter7_5HoursValue = timeAfter7_5Hours.substring(0, 5)
        val timeAfter6HoursValue = timeAfter6Hours.substring(0, 5)

        val amPmAfter9Hours = timeAfter9Hours.substring(5) // Extract the AM/PM part
        val amPmAfter7_5Hours = timeAfter7_5Hours.substring(5)
        val amPmAfter6Hours = timeAfter6Hours.substring(5)

        ui.apply {
            timeInDigitFirst.text = timeAfter9HoursValue
            amPmTextFirst.text = amPmAfter9Hours.uppercase()

            timeInDigitSecond.text = timeAfter7_5HoursValue
            amPmTextSecond.text = amPmAfter7_5Hours.uppercase()

            timeInDigitThird.text = timeAfter6HoursValue
            amPmTextThird.text = amPmAfter6Hours.uppercase()
        }

        Log.d("BedTimeFragment", "Time after 9 hours: $timeAfter9HoursValue $amPmAfter9Hours")
        Log.d("BedTimeFragment", "Time after 7.5 hours: $timeAfter7_5HoursValue $amPmAfter7_5Hours")
        Log.d("BedTimeFragment", "Time after 6 hours: $timeAfter6HoursValue $amPmAfter6Hours")
    }

}