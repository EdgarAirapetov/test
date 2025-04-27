package com.numplates.nomera3.presentation.view.adapter

import android.content.res.Resources
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.numplates.nomera3.databinding.MeeraCellShimmerVehicleItemBinding

private const val COUNT_SHIMMER_ITEM = 3

class MeeraVehicleListShimmerAdapter : RecyclerView.Adapter<MeeraVehicleListShimmerAdapter.MeeraCellShimmerHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraCellShimmerHolder {
        return MeeraCellShimmerHolder(parent.toBinding(), parent.resources)
    }

    override fun onBindViewHolder(holder: MeeraCellShimmerHolder, position: Int) = holder.bind()

    override fun getItemCount(): Int = COUNT_SHIMMER_ITEM

    inner class MeeraCellShimmerHolder(
        val binding: MeeraCellShimmerVehicleItemBinding, val resources: Resources
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind() = binding.vgCellShimmer.startShimmer()
    }
}
