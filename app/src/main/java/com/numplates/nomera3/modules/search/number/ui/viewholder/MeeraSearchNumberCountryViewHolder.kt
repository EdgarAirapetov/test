package com.numplates.nomera3.modules.search.number.ui.viewholder

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.noomeera.nmrmediatools.utils.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraItemSeachNumberCountryBinding
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.getCountryFlag
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.getCountryString
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.CountryFilterItem

class MeeraSearchNumberCountryViewHolder(
    private val binding: MeeraItemSeachNumberCountryBinding,
    private val callback: (CountryFilterItem) -> Unit
) : ViewHolder(binding.root) {

    fun bind(item: CountryFilterItem, showDivider: Boolean) {
        binding.tvCountryName.text = itemView.context.getCountryString(item)
        getCountryFlag(item)?.let { binding.ukpCountryFlag.setImageResource(it) }
        binding.vDivider.isVisible = showDivider
        binding.root.setThrottledClickListener { callback.invoke(item) }
    }

}
