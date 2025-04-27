package com.noomeera.nmravatarssdk.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.noomeera.nmravatarssdk.R
import com.noomeera.nmravatarssdk.data.AssetSet
import com.noomeera.nmravatarssdk.databinding.CategoriesItemBinding

internal data class CategoriesItem(
    val typeResource: Int,
    val icon: Int,
    val selectedIcon: Int,
    val assetSets: List<AssetSet>
)

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CategoriesItem>() {
    override fun areContentsTheSame(
        oldItem: CategoriesItem,
        newItem: CategoriesItem
    ): Boolean {
        return oldItem.typeResource == newItem.typeResource
    }

    override fun areItemsTheSame(
        oldItem: CategoriesItem,
        newItem: CategoriesItem
    ): Boolean {
        return oldItem == newItem
    }
}

internal class CategoriesAdapter(
    private val clickListener: (CategoriesItem) -> Unit
) : ListAdapter<CategoriesItem, CategoriesAdapter.ProcessingViewHolder>(
    DIFF_CALLBACK
) {
    var selectedCategoryTypeResource: Int = -1
        private set

    fun setSelectedCategoryTypeResource(selectedType: Int) {
        if (selectedCategoryTypeResource != selectedType) {
            notifyItemChanged(currentList.indexOf(currentList.find { it.typeResource == selectedCategoryTypeResource }))
            selectedCategoryTypeResource = selectedType
            notifyItemChanged(currentList.indexOf(currentList.find { it.typeResource == selectedCategoryTypeResource }))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ProcessingViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.categories_item, parent, false)
        )

    override fun onBindViewHolder(holder: ProcessingViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ProcessingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(position: Int) {
            val item = getItem(position)

            if (item != null) {
                val itemViewBinding = CategoriesItemBinding.bind(itemView)

                itemViewBinding.vPartTypeSelectorItem.setImageResource(if (selectedCategoryTypeResource == item.typeResource) item.selectedIcon else item.icon)
                itemViewBinding.root.setOnClickListener {
                    clickListener(item)
                }
            }
        }
    }
}
