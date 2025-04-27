package com.numplates.nomera3.modules.places.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.inflate
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemPlaceBinding
import com.numplates.nomera3.modules.places.ui.model.PlaceItemUiModel

class PlacesAdapter(
    val onItemSelected: (PlaceItemUiModel) -> Unit
) : ListAdapter<PlaceItemUiModel, PlacesAdapter.PlaceItemViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceItemViewHolder {
        return PlaceItemViewHolder(parent.inflate(R.layout.item_place))
    }

    override fun onBindViewHolder(holder: PlaceItemViewHolder, position: Int) {
        holder.bind(currentList[position], onItemSelected)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    class PlaceItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(itemUiModel: PlaceItemUiModel, onItemSelected: (PlaceItemUiModel) -> Unit) {
            val binding = ItemPlaceBinding.bind(itemView)
            binding.tvItemPlaceTitle.text = itemUiModel.title
            binding.tvItemPlaceAddress.text = itemUiModel.address
            binding.root.setOnClickListener { onItemSelected(itemUiModel) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<PlaceItemUiModel>() {
        override fun areItemsTheSame(oldItem: PlaceItemUiModel, newItem: PlaceItemUiModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: PlaceItemUiModel, newItem: PlaceItemUiModel): Boolean {
            return oldItem == newItem
        }
    }
}
