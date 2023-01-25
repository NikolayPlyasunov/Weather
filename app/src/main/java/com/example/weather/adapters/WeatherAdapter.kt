package com.example.weather.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.WeatherModel
import com.example.weather.databinding.ListItemBinding
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class WeatherAdapter: ListAdapter<WeatherModel, WeatherAdapter.Holder>(Comparator()) {
    class Holder(view: View) : RecyclerView.ViewHolder(view) {

    val binding = ListItemBinding.bind(view)
        fun bind(item: WeatherModel) = with(binding){
            val sum2 = item.time.toInt() + item.currentTime.toInt() - 18000
            val time2 = getDateTime2(sum2.toString())
            tvDate.text = time2
            tvConditions.text = item.condition
            tvTemp.text = item.currentTemp+"°C"
            Picasso.get().load("https://openweathermap.org/img/wn/"+item.imageUrl+"@2x.png").into(im)

           }
        private fun getDateTime2(s: String): String? {
            try {
                val sdf = SimpleDateFormat("dd.MM  HH-mm")
                val netDate = Date(s.toLong() * 1000)
                return sdf.format(netDate)
            } catch (e: Exception) {
                return e.toString()
            }
        }
     }
    class Comparator : DiffUtil.ItemCallback<WeatherModel>(){
        override fun areItemsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val  view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent,false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(getItem(position))
    }
}