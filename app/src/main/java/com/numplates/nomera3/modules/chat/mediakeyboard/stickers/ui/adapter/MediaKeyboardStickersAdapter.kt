package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlideFitCenter
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemMediaKeyboardStickerBinding
import com.numplates.nomera3.databinding.ItemMediaKeyboardStickerPackHeaderBinding
import com.numplates.nomera3.databinding.ItemMediaKeyboardWidgetBinding
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerAdapterItem
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardWidget
import com.numplates.nomera3.presentation.view.utils.inflateBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val TYPE_HEADER = 0
const val TYPE_STICKER = 1
const val TYPE_RECENT_STICKERS_HEADER = 2
const val TYPE_RECENT_STICKER = 3
const val TYPE_WIDGETS = 4
private const val TIME_PATTERN = "HH:mm"

class MediaKeyboardStickersAdapter(
    private val stickerClickListener: (MediaKeyboardStickerUiModel) -> Unit,
    private val stickerLongClickListener: (MediaKeyboardStickerUiModel) -> Unit,
    private val recentStickerClickListener: (MediakeyboardFavoriteRecentUiModel) -> Unit,
    private val recentStickerLongClickListener: (MediakeyboardFavoriteRecentUiModel) -> Unit,
    private val widgetClickListener: (MediaKeyboardWidget) -> Unit,
    private val clearRecentStickersClickListener: () -> Unit
) : ListAdapter<MediaKeyboardStickerAdapterItem, ViewHolder>(StickersDiffUtilCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is MediaKeyboardStickerAdapterItem.StickerItem -> TYPE_STICKER
            is MediaKeyboardStickerAdapterItem.StickerPackHeaderItem -> TYPE_HEADER
            is MediaKeyboardStickerAdapterItem.RecentStickerItem -> TYPE_RECENT_STICKER
            is MediaKeyboardStickerAdapterItem.RecentStickersHeaderItem -> TYPE_RECENT_STICKERS_HEADER
            is MediaKeyboardStickerAdapterItem.WidgetsItem -> TYPE_WIDGETS
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> StickerPackHeaderViewHolder(ItemMediaKeyboardStickerPackHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ))
            TYPE_STICKER -> StickerViewHolder(
                binding = ItemMediaKeyboardStickerBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                clickListener = stickerClickListener,
                longClickListener = stickerLongClickListener
            )
            TYPE_RECENT_STICKERS_HEADER -> RecentsStickersHeaderViewHolder(
                binding = parent.inflateBinding(ItemMediaKeyboardStickerPackHeaderBinding::inflate),
                clearRecentStickersListener = clearRecentStickersClickListener
            )
            TYPE_RECENT_STICKER -> RecentStickerViewHolder(
                binding = parent.inflateBinding(ItemMediaKeyboardStickerBinding::inflate),
                clickListener = recentStickerClickListener,
                longClickListener = recentStickerLongClickListener
            )
            TYPE_WIDGETS -> WidgetsViewHolder(
                binding = parent.inflateBinding(ItemMediaKeyboardWidgetBinding::inflate),
                clickListener = widgetClickListener
            )
            else -> error("No such a view type.")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is StickerViewHolder ->
                holder.bind(getItem(position) as MediaKeyboardStickerAdapterItem.StickerItem)
            is StickerPackHeaderViewHolder ->
                holder.bind(getItem(position) as MediaKeyboardStickerAdapterItem.StickerPackHeaderItem)
            is RecentStickerViewHolder ->
                holder.bind(getItem(position) as MediaKeyboardStickerAdapterItem.RecentStickerItem)
        }
    }

    private class StickerViewHolder(
        private val binding: ItemMediaKeyboardStickerBinding,
        private val clickListener: (MediaKeyboardStickerUiModel) -> Unit,
        private val longClickListener: (MediaKeyboardStickerUiModel) -> Unit
    ) : ViewHolder(binding.root) {

        fun bind(item: MediaKeyboardStickerAdapterItem.StickerItem) {
            binding.ivSticker.loadGlideFitCenter(item.sticker.url)
            binding.root.setThrottledClickListener { clickListener.invoke(item.sticker) }
            binding.root.setOnLongClickListener {
                longClickListener.invoke(item.sticker)
                return@setOnLongClickListener true
            }
        }

    }

    private class WidgetsViewHolder(
        binding: ItemMediaKeyboardWidgetBinding,
        private val clickListener: (MediaKeyboardWidget) -> Unit,
    ) : ViewHolder(binding.root) {

        init {
            binding.llWidgetTime.setOnClickListener {
                clickListener(MediaKeyboardWidget.TIME_WIDGET)
            }
            binding.llWidgetMusic.setOnClickListener {
                clickListener(MediaKeyboardWidget.MUSIC_WIDGET)
            }
            binding.tvWidgetTime.text = SimpleDateFormat(
                TIME_PATTERN,
                Locale.getDefault()
            ).format(Date())
        }
    }

    private class StickerPackHeaderViewHolder(
        private val binding: ItemMediaKeyboardStickerPackHeaderBinding
    ) : ViewHolder(binding.root) {

        fun bind(item: MediaKeyboardStickerAdapterItem.StickerPackHeaderItem) {
            binding.tvStickerPackTitle.text = item.stickerPack.title
            binding.ivClearRecentStickers.gone()
        }

    }

    private class RecentStickerViewHolder(
        private val binding: ItemMediaKeyboardStickerBinding,
        private val clickListener: (MediakeyboardFavoriteRecentUiModel) -> Unit,
        private val longClickListener: (MediakeyboardFavoriteRecentUiModel) -> Unit
    ) : ViewHolder(binding.root) {

        fun bind(item: MediaKeyboardStickerAdapterItem.RecentStickerItem) {
            binding.ivSticker.loadGlideFitCenter(item.sticker.url)
            binding.root.setThrottledClickListener { clickListener.invoke(item.sticker) }
            binding.root.setOnLongClickListener {
                longClickListener.invoke(item.sticker)
                return@setOnLongClickListener true
            }
        }

    }

    private class RecentsStickersHeaderViewHolder(
        private val binding: ItemMediaKeyboardStickerPackHeaderBinding,
        private val clearRecentStickersListener: () -> Unit
    ) : ViewHolder(binding.root) {

        init {
            binding.tvStickerPackTitle.text = binding.root.context.getString(R.string.recent)
            binding.ivClearRecentStickers.visible()
            binding.ivClearRecentStickers.setThrottledClickListener { clearRecentStickersListener.invoke() }
        }

    }

    private class StickersDiffUtilCallback : DiffUtil.ItemCallback<MediaKeyboardStickerAdapterItem>() {
        override fun areItemsTheSame(
            oldItem: MediaKeyboardStickerAdapterItem,
            newItem: MediaKeyboardStickerAdapterItem
        ): Boolean {
            if (oldItem is MediaKeyboardStickerAdapterItem.StickerItem &&
                newItem is MediaKeyboardStickerAdapterItem.StickerItem) {
                return oldItem.sticker.id == newItem.sticker.id
            }
            if (oldItem is MediaKeyboardStickerAdapterItem.StickerPackHeaderItem &&
                newItem is MediaKeyboardStickerAdapterItem.StickerPackHeaderItem) {
                return oldItem.stickerPack.id == newItem.stickerPack.id
            }
            if (oldItem is MediaKeyboardStickerAdapterItem.RecentStickersHeaderItem &&
                newItem is MediaKeyboardStickerAdapterItem.RecentStickersHeaderItem) {
                return true
            }
            if (oldItem is MediaKeyboardStickerAdapterItem.RecentStickerItem &&
                newItem is MediaKeyboardStickerAdapterItem.RecentStickerItem) {
                return oldItem.sticker.stickerId == newItem.sticker.stickerId
            }
            if (oldItem is MediaKeyboardStickerAdapterItem.WidgetsItem &&
                newItem is MediaKeyboardStickerAdapterItem.WidgetsItem) {
                return true
            }
            return false
        }

        override fun areContentsTheSame(
            oldItem: MediaKeyboardStickerAdapterItem,
            newItem: MediaKeyboardStickerAdapterItem
        ): Boolean {
            if (oldItem is MediaKeyboardStickerAdapterItem.StickerItem &&
                newItem is MediaKeyboardStickerAdapterItem.StickerItem) {
                return oldItem.sticker == newItem.sticker
            }
            if (oldItem is MediaKeyboardStickerAdapterItem.StickerPackHeaderItem &&
                newItem is MediaKeyboardStickerAdapterItem.StickerPackHeaderItem) {
                return oldItem.stickerPack == newItem.stickerPack
            }
            if (oldItem is MediaKeyboardStickerAdapterItem.RecentStickersHeaderItem &&
                newItem is MediaKeyboardStickerAdapterItem.RecentStickersHeaderItem) {
                return true
            }
            if (oldItem is MediaKeyboardStickerAdapterItem.RecentStickerItem &&
                newItem is MediaKeyboardStickerAdapterItem.RecentStickerItem) {
                return oldItem.sticker == newItem.sticker
            }
            return false
        }
    }

}
