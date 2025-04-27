package com.numplates.nomera3.presentation.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.data.network.City


class SearchCityAdapterNew : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var cityList: MutableList<City> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return CityViewHolder(view)
    }

    override fun getItemId(position: Int): Long {
        return cityList[position].cityId.toLong()
    }

    override fun getItemCount(): Int = cityList.size

    override fun getItemViewType(position: Int): Int {
        return 0
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CityViewHolder)
            holder.bind(cityList[position])
    }

    fun setData(list: MutableList<City>) {
        cityList.clear()
        cityList.addAll(list)
        notifyDataSetChanged()
    }


    inner class CityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(city: City) {
            textView.text = city.name
        }
    }
}
