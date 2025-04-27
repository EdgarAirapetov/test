package com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlideCenterCrop
import com.meera.core.extensions.loadGlideFitCenter
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.databinding.ItemMediakeyboardFavoriteBinding
import com.numplates.nomera3.modules.chat.SECONDS_IN_MINUTE
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel

class MediaKeyboardFavoritesRecentsAdapter(
    private val longCLickListener: (MediakeyboardFavoriteRecentUiModel) -> Unit,
    private val clickListener: (MediakeyboardFavoriteRecentUiModel) -> Unit
) : ListAdapter<
    MediakeyboardFavoriteRecentUiModel, MediaKeyboardFavoritesRecentsAdapter.MediaKeyboardFavoritesRecentsViewHolder
    >(MediaKeyboardFavoritesDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaKeyboardFavoritesRecentsViewHolder {
        val binding = ItemMediakeyboardFavoriteBinding.inflate(LayoutInflater.from(parent.context))
        return MediaKeyboardFavoritesRecentsViewHolder(binding, longCLickListener, clickListener)
    }

    override fun onBindViewHolder(holder: MediaKeyboardFavoritesRecentsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MediaKeyboardFavoritesRecentsViewHolder(
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

    class MediaKeyboardFavoritesDiffUtil : DiffUtil.ItemCallback<MediakeyboardFavoriteRecentUiModel>() {
        override fun areItemsTheSame(
            oldItem: MediakeyboardFavoriteRecentUiModel,
            newItem: MediakeyboardFavoriteRecentUiModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: MediakeyboardFavoriteRecentUiModel,
            newItem: MediakeyboardFavoriteRecentUiModel
        ): Boolean {
            return oldItem == newItem
        }
    }

}
