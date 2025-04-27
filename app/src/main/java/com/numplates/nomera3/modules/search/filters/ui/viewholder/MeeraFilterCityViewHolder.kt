package com.numplates.nomera3.modules.search.filters.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraItemFilterCityBinding
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityfilter.FoundCityModel

class MeeraFilterCityViewHolder(
    private val binding: MeeraItemFilterCityBinding,
    private val listener: (FoundCityModel) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.apply {
            ukcFilterCity.cellCityText = true
            ukcFilterCity.setCellRightElementClickable(false)
            ukcFilterCity.setRightElementContainerClickable(false)
        }
    }

    fun bind(model: FoundCityModel) {
        binding.apply {
            ukcFilterCity.setTitleValue(model.title)
            ukcFilterCity.setCityValue(model.countryName)
            ukcFilterCity.setCellRightElementChecked(model.isSelected)
            ukcFilterCity.setThrottledClickListener { listener.invoke(model) }
        }
    }
}
