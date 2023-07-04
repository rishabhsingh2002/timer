package app.appsuccessor.sandtimer.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.appsuccessor.sandtimer.R
import app.appsuccessor.sandtimer.databinding.ItemAlarmBinding
import app.appsuccessor.sandtimer.model.Alarm
import app.appsuccessor.sandtimer.view.util.isNightTime

class AlarmAdapter(
    private val context: Context,
    private val alarmList: List<Alarm>,
    private val onItemClick: (Alarm) -> Unit,
    private val onAlarmToggleListener: OnAlarmToggleListener
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    inner class AlarmViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemAlarmBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        return AlarmViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_alarm, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val item = alarmList[position]
        holder.binding.apply {
            title.text = item.title
            timeInDigit.text = item.time.toString()
            onOFFSwitch.isChecked = item.isEnabled == true
            if (isNightTime()) {
                onOFFSwitch.thumbDrawable =
                    context.resources.getDrawable(R.drawable.custom_switch_alarm)
            } else {
                onOFFSwitch.thumbDrawable =
                    context.resources.getDrawable(R.drawable.custom_switch_alarm_day)
            }
        }
        // Set click listener on the item view
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
        holder.binding.onOFFSwitch.setOnClickListener {
            val isChecked = holder.binding.onOFFSwitch.isChecked
            onAlarmToggleListener.onToggle(item, isChecked)
        }
    }

    interface OnAlarmToggleListener {
        fun onToggle(alarm: Alarm, isChecked: Boolean)
    }

    override fun getItemCount(): Int {
        return alarmList.size
    }
}

