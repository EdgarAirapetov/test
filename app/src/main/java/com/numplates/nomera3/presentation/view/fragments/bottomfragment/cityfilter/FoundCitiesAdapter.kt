package com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityfilter

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp
import com.meera.core.extensions.inflate
import com.numplates.nomera3.R

class FoundCitiesAdapter(
        private val foundCityList: MutableList<FoundCityModel> = mutableListOf(),
        private val callback: FoundCitiesAdapterCallback? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FoundCityViewHolder(parent.inflate(R.layout.found_city_list_item))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as FoundCityViewHolder
        holder.bind(foundCityList[position])
    }

    override fun getItemCount(): Int = foundCityList.size


    fun updateFoundCitiesList(foundCities: List<FoundCityModel>) {
        foundCityList.clear()
        foundCityList.addAll(foundCities)
        notifyDataSetChanged()
    }

    inner class FoundCityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val name: TextView = itemView.findViewById(R.id.found_city_name)
        private val countryOfCity: TextView = itemView.findViewById(R.id.found_city_name_country)
        private val citySelector: CheckBox = itemView.findViewById(R.id.found_city_checkbox)
        private val llRoot: LinearLayout = itemView.findViewById(R.id.ll_root_city)

        fun bind(model: FoundCityModel) {
            with(model) {
                name.text = title
                countryOfCity.text = countryName
                citySelector.isChecked = isSelected
            }

            llRoot.setOnClickListener {
                callback?.onCityClicked(model, foundCityList)
            }
            citySelector.setOnClickListener {
                callback?.onCityClicked(model, foundCityList)
            }
        }
    }
}

class FirstItemMarginTop(private val marginTop: Int = 24.dp) : RecyclerView.ItemDecoration() {
    // add margin top = 24 dp for first item
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        // is first item
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = marginTop
        }
    }
}
