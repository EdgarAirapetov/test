package com.numplates.nomera3.presentation.view.fragments.dialogs

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.inflate
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.City

/*
* Адаптер для CityPickerDialogFragment
* */
class CityPickerDialogAdapter(
        private val cities: MutableList<City>,
        private val callback: CityPickerDialogCallback? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PersonalInfoCityItem(parent.inflate(R.layout.city_picker_dialog_adapter_item))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as PersonalInfoCityItem
        holder.bind(cities[position])
    }

    override fun getItemCount(): Int = cities.size

    fun updateList(newCities: List<City>) {
        cities.clear()
        cities.addAll(newCities)
        notifyDataSetChanged()
    }

    inner class PersonalInfoCityItem(private val itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cityName: TextView = itemView.findViewById(R.id.city_name)
        fun bind(city: City) {
            city.title_?.let { cityName.text = it }
            itemView.setOnClickListener {
                callback?.onCityClicked(city)
            }
        }
    }
}
