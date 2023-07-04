package app.appsuccessor.sandtimer.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.appsuccessor.sandtimer.R

class CityListAdapter(
    private val cities: List<String>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<CityListAdapter.CityViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(city: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_city, parent, false)
        return CityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val city = cities[position]
        holder.bind(city)
        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(city)
        }
    }

    override fun getItemCount(): Int {
        return cities.size
    }

    inner class CityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textViewCityName: TextView = view.findViewById(R.id.city)

        fun bind(city: String) {
            textViewCityName.text = city
        }
    }
}
