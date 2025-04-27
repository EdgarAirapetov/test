package com.noomeera.nmravatarssdk.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.noomeera.nmravatarssdk.NMRAvatarsSDK
import com.noomeera.nmravatarssdk.R
import com.noomeera.nmravatarssdk.data.AssetColor
import com.noomeera.nmravatarssdk.databinding.ColorsItemBinding
import com.noomeera.nmravatarssdk.extensions.SvgCache
import com.noomeera.nmravatarssdk.extensions.drawable

internal data class ColorsItem(
    val color: AssetColor
)

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ColorsItem>() {
    override fun areContentsTheSame(
        oldItem: ColorsItem,
        newItem: ColorsItem
    ): Boolean {
        return oldItem.color == newItem.color
    }

    override fun areItemsTheSame(
        oldItem: ColorsItem,
        newItem: ColorsItem
    ): Boolean {
        return oldItem == newItem
    }
}

internal class ColorsAdapter(
    private val context: Context,
    private val clickListener: (ColorsItem) -> Unit
) : ListAdapter<ColorsItem, ColorsAdapter.ProcessingViewHolder>(
    DIFF_CALLBACK
) {
    var selectedColorId: String = ""
        set(value) {
            if (field != value) {
                notifyItemChanged(currentList.indexOf(currentList.find { it.color.id == field }))
                field = value
                notifyItemChanged(currentList.indexOf(currentList.find { it.color.id == value }))
            }
        }

    companion object {
        const val COLOR_CIRCLE_MIN_SIZE = 100
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProcessingViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.colors_item, parent, false)
        return ProcessingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProcessingViewHolder, position: Int) {
        holder.bind(position)
    }

    // To disable recycling on colors (see fragment)
    override fun getItemViewType(position: Int): Int {
        return 0
    }

    inner class ProcessingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(position: Int) {
            val item = getItem(position)

            if (item != null) {
                val itemViewBinding = ColorsItemBinding.bind(itemView)

                itemViewBinding.vColorSelectorItem.background =
                    if (selectedColorId == item.color.id) ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.color_selector_background_selected,
                        null
                    ) else ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.color_selector_background,
                        null
                    )


                val width = itemViewBinding.vColorSelectorItemInner.measuredWidth.let { value ->
                    if (value != 0)
                        return@let value
                    else
                        return@let COLOR_CIRCLE_MIN_SIZE
                }
                val height = itemViewBinding.vColorSelectorItemInner.measuredHeight.let { value ->
                    if (value != 0)
                        return@let value
                    else
                        return@let COLOR_CIRCLE_MIN_SIZE
                }
                itemViewBinding.vColorSelectorItemInner.background =
                    SvgCache.fromFile("${NMRAvatarsSDK.getResourcesDirPath(context)}/${item.color.path}")
                        .drawable(
                            context,
                            width,
                            height
                        ).apply {
                            isCircular = true
                        }

                itemViewBinding.root.setOnClickListener {
                    clickListener(item)
                }
            }
        }
    }
}
