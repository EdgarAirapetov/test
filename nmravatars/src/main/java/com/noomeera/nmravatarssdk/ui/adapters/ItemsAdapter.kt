package com.noomeera.nmravatarssdk.ui.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.noomeera.nmravatarssdk.NMRAvatarsSDK
import com.noomeera.nmravatarssdk.R
import com.noomeera.nmravatarssdk.data.AssetSet
import com.noomeera.nmravatarssdk.data.AvatarState
import com.noomeera.nmravatarssdk.data.LayeredSvg
import com.noomeera.nmravatarssdk.databinding.ItemsItemBinding
import com.noomeera.nmravatarssdk.extensions.SvgCache
import com.noomeera.nmravatarssdk.extensions.dp


private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AssetSet>() {
    override fun areContentsTheSame(
        oldItem: AssetSet,
        newItem: AssetSet
    ): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(
        oldItem: AssetSet,
        newItem: AssetSet
    ): Boolean {
        return oldItem == newItem
    }
}

internal class ItemsAdapter(
    private val context: Context,
    private val clickListener: (AssetSet) -> Unit
) : ListAdapter<AssetSet, ItemsAdapter.ProcessingViewHolder>(
    DIFF_CALLBACK
) {
    private var avatarState: AvatarState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProcessingViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.items_item, parent, false)

        return ProcessingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProcessingViewHolder, position: Int) {
        holder.bind(position)
    }

    private fun indexOfSelectedAsset(): Int {
        return avatarState?.let { state ->
            currentList.indexOfFirst { item ->
                state.allAvatarStateAssets().find { it.id == item.id } != null
            }
        } ?: -1
    }

    fun setState(newState: AvatarState) {
        val oldSelectedAssetId = indexOfSelectedAsset()
        avatarState = newState
        val newSelectedAssetId = indexOfSelectedAsset()

        if (oldSelectedAssetId != -1) notifyItemChanged(oldSelectedAssetId)
        if (newSelectedAssetId != -1) notifyItemChanged(newSelectedAssetId)
    }

    inner class ProcessingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(position: Int) {
            val item = getItem(position)
            if (item != null) {
                val itemViewBinding = ItemsItemBinding.bind(itemView)
                val quality = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 1.0f else 0.5f

                itemViewBinding.vPartTypeItem.quality = quality
                itemViewBinding.root.setOnClickListener {
                    clickListener(item)
                }

                if (item.assets == null && item.variants == null) {
                    itemViewBinding.cvFilterIconContainer.setCardBackgroundColor(
                        context.resources.getColor(
                            android.R.color.white
                        )
                    )
                    itemViewBinding.vPartTypeItemImage.visibility = View.VISIBLE
                    return
                } else {
                    itemViewBinding.cvFilterIconContainer.setCardBackgroundColor(
                        context.resources.getColor(
                            R.color.common_background
                        )
                    )
                    itemViewBinding.vPartTypeItemImage.visibility = View.GONE
                }

                avatarState?.let { state ->
                    itemViewBinding.cvFilterIconContainer.strokeWidth =
                        if (state.allAvatarStateAssets().find { it.id == item.id } == null
                        ) 0 else 3.dp
                } ?: run {
                    itemViewBinding.cvFilterIconContainer.strokeWidth = 0
                }

                val assets = mutableListOf<LayeredSvg>()
                if (item.preview != null) {
                    assets.add(
                        LayeredSvg(
                            SvgCache.fromFile("${NMRAvatarsSDK.getResourcesDirPath(context)}/${item.preview}"),
                            0,
                            "",
                            mask = false,
                            applyMask = true
                        )
                    )
                } else {
                    item.assets?.onEach {
                        if (it.showInSelector) {
                            assets.add(it.toLayeredSvg(context))
                        }
                    }
                    item.variants?.let { variants ->
                        val selectedColor = avatarState?.allAvatarStateAssets()
                            ?.find { it.id == item.id }?.color
                        val variant =
                            variants.find { it.color.id == selectedColor } ?: variants.first()
                        variant.assets.onEach {
                            if (it.showInSelector) {
                                assets.add(it.toLayeredSvg(context))
                            }
                        }
                    }
                }
                itemViewBinding.vPartTypeItem.setAssets(assets)
            }
        }
    }
}
