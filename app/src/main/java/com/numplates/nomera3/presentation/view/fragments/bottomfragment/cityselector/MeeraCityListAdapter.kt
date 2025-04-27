package com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityselector

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.cell.UiKitCell
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.City

private const val MARGIN_START_DIVIDER = 16

class MeeraCityListAdapter(
    private val predefinedCityList: MutableList<City> = mutableListOf(),
    private val onClickListener: CityListAdapterOnClickListener? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FoundCityViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.meera_city_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as FoundCityViewHolder
        holder.bind(predefinedCityList[position], position == predefinedCityList.lastIndex)
    }

    override fun getItemCount(): Int = predefinedCityList.size


    @SuppressLint("NotifyDataSetChanged")
    fun updateCityList(foundCities: List<City>) {
        predefinedCityList.clear()
        predefinedCityList.addAll(foundCities)
        notifyDataSetChanged()
    }

    inner class FoundCityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cityName: UiKitCell = itemView.findViewById(R.id.city_name)

        fun bind(city: City, lastCountry: Boolean) {
            cityName.setTitleValue(city.title_ ?: "")
            cityName.setMarginStartDivider(MARGIN_START_DIVIDER)
            if (lastCountry) cityName.cellPosition = CellPosition.BOTTOM
            itemView.setOnClickListener {
                onClickListener?.onItemClicked(city)
            }
        }
    }
}
