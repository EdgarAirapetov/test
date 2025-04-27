package com.numplates.nomera3.modules.userprofile.ui.fragment

import android.content.res.Resources
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.numplates.nomera3.databinding.MeeraItemProfileSuggestionShimmerBinding

class MeeraSuggestionShimmerAdapter
    : ListAdapter<String, MeeraSuggestionShimmerAdapter.MeeraSuggestionShimmerHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraSuggestionShimmerHolder {
        return MeeraSuggestionShimmerHolder(parent.toBinding(), parent.resources)
    }

    override fun onBindViewHolder(holder: MeeraSuggestionShimmerHolder, position: Int) = Unit

    inner class MeeraSuggestionShimmerHolder(
        val binding: MeeraItemProfileSuggestionShimmerBinding,
        val resources: Resources
    ) : RecyclerView.ViewHolder(binding.root)

    private class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
