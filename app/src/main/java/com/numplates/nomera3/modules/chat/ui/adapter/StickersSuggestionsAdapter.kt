package com.numplates.nomera3.modules.chat.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlideWithCallback
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.updatePadding
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemStickerSuggestionBinding
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerUiModel
import com.numplates.nomera3.modules.chat.ui.entity.StickerSuggestionUiModel
import com.numplates.nomera3.presentation.view.utils.inflateBinding

class StickersSuggestionsAdapter(
    private val callback: (MediaKeyboardStickerUiModel) -> Unit
) : ListAdapter<StickerSuggestionUiModel,
    StickersSuggestionsAdapter.StickersSuggestionViewHolder>(StickersSuggestionsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickersSuggestionViewHolder {
        val binding = parent.inflateBinding(ItemStickerSuggestionBinding::inflate)
        return StickersSuggestionViewHolder(binding, callback)
    }

    override fun onBindViewHolder(holder: StickersSuggestionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StickersSuggestionViewHolder(
        private val binding: ItemStickerSuggestionBinding,
        private val callback: (MediaKeyboardStickerUiModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StickerSuggestionUiModel) {
            binding.apply {
                vgStickerPlaceholder.visible()
                ivStickerSuggestion.loadGlideWithCallback(item.sticker.url, onReady = {
                    vgStickerPlaceholder.gone()
                })
                root.setThrottledClickListener { callback.invoke(item.sticker) }
                when {
                    item.isFirst -> {
                        vgStickerSuggestion.setBackgroundResource(R.drawable.bg_sticker_suggestion_first)
                        vgStickerSuggestion.updatePadding(paddingStart = 16.dp)
                    }
                    item.isLast -> {
                        vgStickerSuggestion.setBackgroundResource(R.drawable.bg_sticker_suggestion_last)
                        vgStickerSuggestion.updatePadding(paddingStart = 8.dp, paddingEnd = 16.dp)
                    }
                    else -> {
                        vgStickerSuggestion.setBackgroundResource(R.drawable.bg_sticker_suggestion)
                        vgStickerSuggestion.updatePadding(paddingStart = 8.dp)
                    }
                }
            }
        }

    }

    class StickersSuggestionsDiffCallback : DiffUtil.ItemCallback<StickerSuggestionUiModel>() {
        override fun areItemsTheSame(
            oldItem: StickerSuggestionUiModel,
            newItem: StickerSuggestionUiModel
        ): Boolean {
            return oldItem.sticker.id == newItem.sticker.id
        }

        override fun areContentsTheSame(
            oldItem: StickerSuggestionUiModel,
            newItem: StickerSuggestionUiModel
        ): Boolean {
            return oldItem == newItem
        }
    }

}
