package app.appsuccessor.sandtimer.view.fragment

import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import app.appsuccessor.sandtimer.R
import app.appsuccessor.sandtimer.databinding.DialogCitiesListBinding
import app.appsuccessor.sandtimer.databinding.FragmentClockBinding
import app.appsuccessor.sandtimer.datasource.remote.CityApiClient
import app.appsuccessor.sandtimer.datasource.remote.TimeZoneResponse
import app.appsuccessor.sandtimer.view.adapter.CityListAdapter
import app.appsuccessor.sandtimer.view.adapter.TimeZoneAdapter
import app.appsuccessor.sandtimer.view.util.clickTo
import app.appsuccessor.sandtimer.viewModel.AlarmViewModel
import app.appsuccessor.sandtimer.viewModel.ClockViewModel
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ClockFragment : Fragment(), CityListAdapter.OnItemClickListener,
    TimeZoneAdapter.OnItemDeleteListener {
    private lateinit var ui: FragmentClockBinding

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var cityListAdapter: TimeZoneAdapter
    private val clickedItems: MutableList<String> = mutableListOf()
    private val timezoneResponses: MutableList<TimeZoneResponse> = mutableListOf()
    private lateinit var bottomSheet: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ui = FragmentClockBinding.inflate(layoutInflater)
        sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", MODE_PRIVATE)

        cityListAdapter = TimeZoneAdapter(timezoneResponses)
        cityListAdapter.setOnItemDeleteListener(this)
        ui.rcvTimeZones.adapter = cityListAdapter

        // Retrieve stored timezoneResponses from shared preferences
        val timeZonesJson = sharedPreferences.getString("timeZoneResponses", null)
        if (!timeZonesJson.isNullOrEmpty()) {
            val savedTimeZoneResponses =
                Gson().fromJson(timeZonesJson, Array<TimeZoneResponse>::class.java)
            timezoneResponses.addAll(savedTimeZoneResponses)
            Log.d("kdhflajsdk", "$timezoneResponses")
        }

        // Show dialog with list of time zones and city names
        ui.addTime.clickTo {
            showCityListDialog()
        }

        return ui.root
    }

    private fun showCityListDialog() {
        val timeZones = sharedPreferences.getStringSet("timeZones", null)?.toList()
        if (timeZones.isNullOrEmpty()) {
            // Handle the case when timeZones is null or empty
            return
        }

        bottomSheet = Dialog(requireContext())
        bottomSheet.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val bindingSheet = DialogCitiesListBinding.inflate(layoutInflater)
        bottomSheet.setContentView(bindingSheet.root)

        bindingSheet.cancel.clickTo {
            bottomSheet.dismiss()
        }


        val filteredTimeZoneList = ArrayList(timeZones)
        val timeZoneListAdapter = CityListAdapter(filteredTimeZoneList, this)
        bindingSheet.recyclerViewCityList.layoutManager = LinearLayoutManager(requireContext())
        bindingSheet.recyclerViewCityList.adapter = timeZoneListAdapter

        bindingSheet.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No implementation needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filteredTimeZoneList.clear()
                if (s.isNullOrEmpty()) {
                    filteredTimeZoneList.addAll(timeZones ?: emptyList())
                } else {
                    val searchQuery = s.toString().trim()
                    for (timeZone in timeZones ?: emptyList()) {
                        if (timeZone.contains(searchQuery, ignoreCase = true)) {
                            filteredTimeZoneList.add(timeZone)
                        }
                    }
                }
                timeZoneListAdapter.notifyDataSetChanged()
            }

            override fun afterTextChanged(s: Editable?) {
                // No implementation needed
            }
        })

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

    override fun onItemClick(city: String) {
        clickedItems.add(city)
        removeTimeZone(city) // Remove the timezone from the list and shared preferences
        fetchCurrentTime(listOf(city)) // Fetch current time for the remaining timezones
        bottomSheet.dismiss()
    }

    override fun onItemDelete(timeZoneResponse: TimeZoneResponse) {
        removeTimeZone(timeZoneResponse.timezone)
    }

    private fun removeTimeZone(city: String) {
        val index = timezoneResponses.indexOfFirst { it.timezone == city }
        if (index != -1) {
            timezoneResponses.removeAt(index)
            cityListAdapter.notifyItemRemoved(index)
            saveTimeZoneResponsesToSharedPreferences()
        }
    }

    private fun fetchCurrentTime(timeZones: List<String>) {
        val cityApiClient = CityApiClient.create()

        timeZones.forEach { timeZone ->
            val call = cityApiClient.getTimeZone(timeZone)
            call.enqueue(object : Callback<TimeZoneResponse> {
                override fun onResponse(
                    call: Call<TimeZoneResponse>,
                    response: Response<TimeZoneResponse>
                ) {
                    if (response.isSuccessful) {
                        val timeZoneResponse = response.body()

                        // Update the response in the list
                        if (timeZoneResponse != null) {
                            val index = timezoneResponses.indexOfFirst { it.timezone == timeZone }
                            if (index != -1) {
                                timezoneResponses[index] = timeZoneResponse
                                cityListAdapter.notifyItemChanged(index)
                            } else {
                                timezoneResponses.add(timeZoneResponse)
                                cityListAdapter.notifyItemInserted(timezoneResponses.size - 1)
                            }

                            // Store the updated list in shared preferences
                            saveTimeZoneResponsesToSharedPreferences()
                        }
                    }
                }

                override fun onFailure(call: Call<TimeZoneResponse>, t: Throwable) {
                    // Handle the failure case here
                    Log.e("fetchCurrentTime", "API call failed: ${t.message}")
                }
            })
        }
    }

    private fun saveTimeZoneResponsesToSharedPreferences() {
        val timeZonesJson = Gson().toJson(timezoneResponses)
        sharedPreferences.edit().putString("timeZoneResponses", timeZonesJson).apply()
    }

    private fun loadTimeZoneResponsesFromSharedPreferences() {
        val timeZonesJson = sharedPreferences.getString("timeZoneResponses", null)
        if (!timeZonesJson.isNullOrEmpty()) {
            val savedTimeZoneResponses =
                Gson().fromJson(timeZonesJson, Array<TimeZoneResponse>::class.java)
            timezoneResponses.clear()
            timezoneResponses.addAll(savedTimeZoneResponses)
            cityListAdapter.notifyDataSetChanged()
        }
    }

    override fun onPause() {
        super.onPause()
        saveTimeZoneResponsesToSharedPreferences()
    }

    override fun onResume() {
        super.onResume()
        loadTimeZoneResponsesFromSharedPreferences()
    }
}