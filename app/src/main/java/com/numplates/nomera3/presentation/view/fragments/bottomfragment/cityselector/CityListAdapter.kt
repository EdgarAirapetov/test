package com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityselector

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.City

/**
 * Адаптер для выбора города на экране Регистрации / Личных данных
 * */
class CityListAdapter(
        private val predefinedCityList: MutableList<City> = mutableListOf(),
        private val onClickListener: CityListAdapterOnClickListener? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FoundCityViewHolder(
                LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.city_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as FoundCityViewHolder
        holder.bind(predefinedCityList[position])
    }

    override fun getItemCount(): Int = predefinedCityList.size


    fun updateCityList(foundCities: List<City>) {
        predefinedCityList.clear()
        predefinedCityList.addAll(foundCities)
        notifyDataSetChanged()
    }

    inner class FoundCityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cityName: TextView = itemView.findViewById(R.id.city_name)
        private val countryName: TextView = itemView.findViewById(R.id.country_name)

        fun bind(city: City) {
            cityName.text = city.title_ ?: ""
            countryName.text = city.countryName ?: ""

            itemView.setOnClickListener {
                onClickListener?.onItemClicked(city)
            }
        }
    }
}

