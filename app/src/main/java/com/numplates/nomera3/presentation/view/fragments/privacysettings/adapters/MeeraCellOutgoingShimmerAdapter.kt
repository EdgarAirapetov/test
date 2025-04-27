package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters

import android.content.res.Resources
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.numplates.nomera3.databinding.MeeraCellShimmerOutgoingItemBinding

class MeeraCellOutgoingShimmerAdapter
    : ListAdapter<String, MeeraCellOutgoingShimmerAdapter.MeeraCellShimmerHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraCellShimmerHolder {
        return MeeraCellShimmerHolder(parent.toBinding(), parent.resources)
    }

    override fun onBindViewHolder(holder: MeeraCellShimmerHolder, position: Int) {
        holder.bind()
    }

    inner class MeeraCellShimmerHolder(
        val binding: MeeraCellShimmerOutgoingItemBinding,
        val resources: Resources
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind() = {
            binding.vgCellShimmer.startShimmer()
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
