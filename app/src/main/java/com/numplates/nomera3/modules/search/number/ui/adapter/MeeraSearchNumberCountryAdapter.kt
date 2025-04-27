package com.numplates.nomera3.modules.search.number.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.numplates.nomera3.databinding.MeeraItemSeachNumberCountryBinding
import com.numplates.nomera3.modules.search.number.ui.viewholder.MeeraSearchNumberCountryViewHolder
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.CountryFilterItem
import com.numplates.nomera3.presentation.view.utils.inflateBinding

class MeeraSearchNumberCountryAdapter(
    private val callback: (CountryFilterItem) -> Unit
) : ListAdapter<CountryFilterItem, MeeraSearchNumberCountryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraSearchNumberCountryViewHolder {
        val binding = parent.inflateBinding(MeeraItemSeachNumberCountryBinding::inflate)
        return MeeraSearchNumberCountryViewHolder(binding, callback)
    }

    override fun onBindViewHolder(holder: MeeraSearchNumberCountryViewHolder, position: Int) {
        holder.bind(getItem(position), position != itemCount - 1)
    }

    private class DiffCallback : DiffUtil.ItemCallback<CountryFilterItem>() {
        override fun areItemsTheSame(oldItem: CountryFilterItem, newItem: CountryFilterItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: CountryFilterItem, newItem: CountryFilterItem): Boolean {
            return oldItem == newItem
        }
    }
}
