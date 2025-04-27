package com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.inflate
import com.meera.core.extensions.loadGlideCenterCrop
import com.meera.core.extensions.loadGlideFitCenter
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemMediakeyboardFavoriteBinding
import com.numplates.nomera3.modules.chat.SECONDS_IN_MINUTE
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.entity.MediaKeyboardRecentAdapterItem

class MediaKeyboardRecentsAdapter(
    private val longClickListener: (MediakeyboardFavoriteRecentUiModel) -> Unit,
    private val clickListener: (MediakeyboardFavoriteRecentUiModel) -> Unit
) :
    ListAdapter<MediaKeyboardRecentAdapterItem, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            MediaKeyboardRecentAdapterItem.ItemType.RECENT.ordinal -> {
                val binding = ItemMediakeyboardFavoriteBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return MediaKeyboardRecentViewHolder(binding, longClickListener, clickListener)
            }
            MediaKeyboardRecentAdapterItem.ItemType.SHIMMER.ordinal -> {
                return MediaKeyboardRecentShimmerViewHolder(
                    parent.inflate(R.layout.item_mediakeyboard_recent_shimmer)
                )
            }
            else -> error("No such a view type.")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MediaKeyboardRecentViewHolder -> {
                getItem(position).model?.let { holder.bind(it) }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.ordinal
    }

    private class MediaKeyboardRecentViewHolder(
        private val binding: ItemMediakeyboardFavoriteBinding,
        private val longClickListener: (MediakeyboardFavoriteRecentUiModel) -> Unit,
        private val clickListener: (MediakeyboardFavoriteRecentUiModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MediakeyboardFavoriteRecentUiModel) {
            with(binding) {
                root.setThrottledClickListener { clickListener.invoke(item) }
                root.setOnLongClickListener {
                    longClickListener.invoke(item)
                    true
                }
                if (item.type == MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.STICKER) {
                    ivFavoritePreview.loadGlideFitCenter(item.preview)
                } else {
                    ivFavoritePreview.loadGlideCenterCrop(item.preview)
                }
                if (item.duration != null) {
                    val formattedDuration = String.format(
                        "%02d:%02d",
                        item.duration / SECONDS_IN_MINUTE,
                        item.duration % SECONDS_IN_MINUTE
                    )
                    tvMediaPreviewVideoDuration.text = formattedDuration
                    vgMediaPreviewVideoDuration.visible()
                } else {
                    vgMediaPreviewVideoDuration.gone()
                }
            }
        }

    }

    private class MediaKeyboardRecentShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private class DiffCallback : DiffUtil.ItemCallback<MediaKeyboardRecentAdapterItem>() {
        override fun areItemsTheSame(
            oldItem: MediaKeyboardRecentAdapterItem,
            newItem: MediaKeyboardRecentAdapterItem
        ): Boolean {
            return oldItem.type == newItem.type && oldItem.model?.id == newItem.model?.id
        }

        override fun areContentsTheSame(
            oldItem: MediaKeyboardRecentAdapterItem,
            newItem: MediaKeyboardRecentAdapterItem
        ): Boolean {
            return oldItem == newItem
        }
    }

}
