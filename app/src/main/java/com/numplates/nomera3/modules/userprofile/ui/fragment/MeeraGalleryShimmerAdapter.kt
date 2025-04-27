package com.numplates.nomera3.modules.userprofile.ui.fragment

import android.content.res.Resources
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.numplates.nomera3.databinding.MeeraItemGalleryPreviewShimmerBinding

class MeeraGalleryShimmerAdapter
    : ListAdapter<String, MeeraGalleryShimmerAdapter.MeeraGalleryShimmerHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraGalleryShimmerHolder {
        return MeeraGalleryShimmerHolder(parent.toBinding(), parent.resources)
    }

    override fun onBindViewHolder(holder: MeeraGalleryShimmerHolder, position: Int) {
        holder.bind()
    }

    inner class MeeraGalleryShimmerHolder(
        val binding: MeeraItemGalleryPreviewShimmerBinding,
        val resources: Resources
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind() = Unit
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
