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

    private var selectedTime: String? = null
    private var selectedTimeForWakeUp: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        ui = FragmentBedTimeBinding.inflate(layoutInflater)

//        setUpTabText()

        setUpNumberPickup()

        setUpSpinners()

        ui.apply {
            calculate.clickToShow {
                calculate.visibility = View.INVISIBLE
                begin.visibility = View.VISIBLE

                titleOne.visibility = View.INVISIBLE
                titleTwo.visibility = View.VISIBLE
                titleTwo.text = "For a $selectedTime go to bed, you should wake up at:"

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
            hour.minValue = 1
            hour.maxValue = 12

            minute.minValue = 0
            minute.maxValue = 59

            // am pm
            // Define the string values
            val string1 = "AM"
            val string2 = "PM"
            // Set the displayed values
            amPm.displayedValues = arrayOf(string1, string2)
            // Set the custom display format
            amPm.setFormatter { value ->
                if (value == 0) {
                    string1
                } else {
                    string2
                }
            }
            // Set the range and default value
            amPm.value = 0
            amPm.minValue = 0
            amPm.maxValue = 1

            // Set up the listeners for hour, minute, and am/pm pickers
            val timePickerListener = object : NumberPicker.OnValueChangeListener {
                override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
                    val selectedHour = hour.value
                    val selectedMinute = minute.value
                    val selectedAmPm = amPm.value

                    val formattedHour = String.format("%02d", selectedHour)
                    val formattedMinute = String.format("%02d", selectedMinute)
                    val selectedTime =
                        "$formattedHour:$formattedMinute${if (selectedAmPm == 0) "AM" else "PM"}"

                    calculateTimes(selectedTime)
                }
            }

            hour.setOnValueChangedListener(timePickerListener)
            minute.setOnValueChangedListener(timePickerListener)
            amPm.setOnValueChangedListener(timePickerListener)

            // Get the initial selected time
            val selectedHour = hour.value
            val selectedMinute = minute.value
            val selectedAmPm = amPm.value

            val formattedHour = String.format("%02d", selectedHour)
            val formattedMinute = String.format("%02d", selectedMinute)
            selectedTime = "$formattedHour:$formattedMinute${if (selectedAmPm == 0) "AM" else "PM"}"

            calculateTimes(selectedTime!!)
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


//    private fun setUpTabText() {
//        ui.tab.left.text = "STOP WH"
//        ui.tab.left.setTextColor(resources.getColor(R.color.grey_light))
//        ui.tab.center.text = "BEDTIME"
//        ui.tab.center.setTextColor(resources.getColor(R.color.white))
//        ui.tab.right.text = "         "
//        ui.tab.left.clickTo {
//            val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
//            viewPager.currentItem = 3
//        }
//    }

}