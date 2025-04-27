package com.numplates.nomera3.modules.communities.ui.fragment.list

import android.content.res.Resources
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.meera.core.extensions.toInt
import com.numplates.nomera3.databinding.MeeraCommunitiesShimmerItemBinding

class MeeraCommunitiesListShimmerAdapter
    : ListAdapter<String, MeeraCommunitiesListShimmerAdapter.MeeraUserSettingsSearchShimmerHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraUserSettingsSearchShimmerHolder {
        return MeeraUserSettingsSearchShimmerHolder(parent.toBinding(), parent.resources)
    }

    override fun onBindViewHolder(holder: MeeraUserSettingsSearchShimmerHolder, position: Int) {
        holder.bind(currentList.lastIndex != position)
    }

    inner class MeeraUserSettingsSearchShimmerHolder(
        val binding: MeeraCommunitiesShimmerItemBinding,
        val resources: Resources
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dividerVisibility: Boolean) {
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
