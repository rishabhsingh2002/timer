package app.appsuccessor.sandtimer.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import app.appsuccessor.sandtimer.R
import app.appsuccessor.sandtimer.model.LapTime
import app.appsuccessor.sandtimer.databinding.ItemLapBinding

class LapTimesAdapter(
    private val context: Context,
    private val lapTimesLiveData: ArrayList<LapTime>
) :
    RecyclerView.Adapter<LapTimesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLapBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lapTime = lapTimesLiveData[position]
        holder.bind(lapTime)
    }


    override fun getItemCount(): Int {
        return lapTimesLiveData.size ?: 0
    }

    inner class ViewHolder(private val binding: ItemLapBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(lapTime: LapTime) {
            val duration = lapTime.duration
            val formattedTime = formatTime(duration)

            if (adapterPosition == 0) {
                binding.lapNumber.setTextColor(context.resources.getColor(R.color.black))
                binding.time.setTextColor(context.resources.getColor(R.color.black))
            } else if (adapterPosition == 1) {
                binding.lapNumber.setTextColor(context.resources.getColor(R.color.primary_green))
                binding.time.setTextColor(context.resources.getColor(R.color.primary_green))
            } else if (adapterPosition == 2) {
                binding.lapNumber.setTextColor(context.resources.getColor(R.color.primary_red))
                binding.time.setTextColor(context.resources.getColor(R.color.primary_red))
            } else {
                binding.lapNumber.setTextColor(context.resources.getColor(R.color.black))
                binding.time.setTextColor(context.resources.getColor(R.color.black))
            }

            binding.lapNumber.text = "LAP ${(adapterPosition + 1)}"
            binding.time.text = formattedTime
        }
    }


    private fun formatTime(duration: Long): String {
        val hours = (duration / 3600).toInt()
        val minutes = ((duration % 3600) / 60).toInt()
        val seconds = (duration % 60).toInt()
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}

