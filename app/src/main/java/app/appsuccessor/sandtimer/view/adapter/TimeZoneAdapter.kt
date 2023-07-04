package app.appsuccessor.sandtimer.view.adapter

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.appsuccessor.sandtimer.R
import app.appsuccessor.sandtimer.datasource.remote.CityApiClient
import app.appsuccessor.sandtimer.datasource.remote.TimeZoneResponse
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TimeZoneAdapter(private val timezoneResponses: MutableList<TimeZoneResponse>) :
    RecyclerView.Adapter<TimeZoneAdapter.TimeZoneViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(timeZoneResponse: TimeZoneResponse)
    }

    interface OnItemDeleteListener {
        fun onItemDelete(timeZoneResponse: TimeZoneResponse)
    }

    private var itemClickListener: WeakReference<OnItemClickListener>? = null
    private var itemDeleteListener: WeakReference<OnItemDeleteListener>? = null

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        itemClickListener = if (listener != null) {
            WeakReference(listener)
        } else {
            null
        }
    }

    fun setOnItemDeleteListener(listener: OnItemDeleteListener?) {
        itemDeleteListener = if (listener != null) {
            WeakReference(listener)
        } else {
            null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeZoneViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_zones, parent, false)
        return TimeZoneViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TimeZoneViewHolder, position: Int) {
        val timeZoneResponse = timezoneResponses[position]
        val datetimeString = timeZoneResponse.datetime
        val datetime = LocalDateTime.parse(datetimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        // Format time in 12-hour clock format (hh:mm a)
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm")
        val time12HourClock = datetime.format(timeFormatter)
        // Determine AM or PM
        val amPmFormatter = DateTimeFormatter.ofPattern("a")
        val amPm = datetime.format(amPmFormatter)

        holder.dateTimeTextView.text = time12HourClock
        holder.amPm.text = amPm.uppercase()

        val timeZone = timeZoneResponse.timezone
        val cityName = timeZone.substringAfterLast('/')

        holder.city.text = cityName
        holder.timeComparative.text = "Today, ${timeZoneResponse.utc_offset}"

        // Set click listener for the item
        holder.itemView.setOnClickListener {
            itemClickListener?.get()?.onItemClick(timeZoneResponse)
        }

        // Set long click listener for the item
//        holder.itemView.setOnLongClickListener {
//            showDeleteButton(holder)
//            true // Return true to consume the long click event
//        }

        holder.deleteButton.setOnClickListener {
            removeItem(holder.adapterPosition)
        }
    }



    private fun removeItem(position: Int) {
        val removedItem = timezoneResponses.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
        itemDeleteListener?.get()?.onItemDelete(removedItem)
    }

    override fun getItemCount(): Int {
        return timezoneResponses.size
    }

    class TimeZoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTimeTextView: TextView = itemView.findViewById(R.id.timeInDigit)
        val amPm: TextView = itemView.findViewById(R.id.amPm)
        val city: TextView = itemView.findViewById(R.id.city)
        val timeComparative: TextView = itemView.findViewById(R.id.time)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete)
    }
}