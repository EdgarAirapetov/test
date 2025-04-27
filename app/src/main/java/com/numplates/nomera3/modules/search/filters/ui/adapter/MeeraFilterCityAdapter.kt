package com.numplates.nomera3.modules.search.filters.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.numplates.nomera3.databinding.MeeraItemFilterCityBinding
import com.numplates.nomera3.modules.search.filters.ui.viewholder.MeeraFilterCityViewHolder
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityfilter.FoundCityModel
import com.numplates.nomera3.presentation.view.utils.inflateBinding

class MeeraFilterCityAdapter(
    private val listener: (FoundCityModel) -> Unit
) : Adapter<MeeraFilterCityViewHolder>() {

    private val foundCityList: MutableList<FoundCityModel> = mutableListOf()

    fun submitList(list: MutableList<FoundCityModel>) {
        foundCityList.clear()
        foundCityList.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = foundCityList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraFilterCityViewHolder {
        val binding = parent.inflateBinding(MeeraItemFilterCityBinding::inflate)
        return MeeraFilterCityViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: MeeraFilterCityViewHolder, position: Int) {
        holder.bind(foundCityList[position])
    }

}
