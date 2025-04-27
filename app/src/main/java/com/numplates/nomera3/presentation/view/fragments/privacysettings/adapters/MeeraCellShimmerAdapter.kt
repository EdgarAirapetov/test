package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters

import android.content.res.Resources
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.meera.core.extensions.toInt
import com.numplates.nomera3.databinding.MeeraCellShimmerItemBinding

class MeeraCellShimmerAdapter
    : ListAdapter<String, MeeraCellShimmerAdapter.MeeraCellShimmerHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraCellShimmerHolder {
        return MeeraCellShimmerHolder(parent.toBinding(), parent.resources)
    }

    override fun onBindViewHolder(holder: MeeraCellShimmerHolder, position: Int) {
        holder.bind(currentList.lastIndex != position)
    }

    inner class MeeraCellShimmerHolder(
        val binding: MeeraCellShimmerItemBinding,
        val resources: Resources
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dividerVisibility: Boolean) {
            binding.vgCellShimmer.startShimmer()
            binding.vShimmerDivider.visibility = dividerVisibility.toInt()
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
