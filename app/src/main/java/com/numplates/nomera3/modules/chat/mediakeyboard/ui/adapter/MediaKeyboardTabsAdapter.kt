package com.numplates.nomera3.modules.chat.mediakeyboard.ui.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setBackgroundTint
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setTint
import com.meera.core.extensions.setVisible
import com.meera.core.extensions.updatePadding
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemMediaKeyboardTabBinding
import com.numplates.nomera3.databinding.ItemMediaKeyboardTabDividerBinding
import com.numplates.nomera3.databinding.ItemMediaKeyboardTabStickerBinding
import com.numplates.nomera3.modules.chat.mediakeyboard.data.entity.MediaKeyboardTab
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerPackUiModel
import com.numplates.nomera3.modules.gifservice.ui.DRAWABlE_RECENT_STICKERS

private const val TYPE_REGULAR_TAB = 0
private const val TYPE_DIVIDER = 1
private const val TYPE_STICKER_PACK = 2

private const val RECENT_STICKERS_TOP_PADDING = 5
private const val RECENT_STICKERS_BOTTOM_PADDING = 11
private const val DEFAULT_TOP_PADDING = 7
private const val DEFAULT_BOTTOM_PADDING = 13

class MediaKeyboardTabsAdapter(
    private val callback: (MediaKeyboardTab) -> Unit,
    private val stickerPackViewedCallback: (MediaKeyboardStickerPackUiModel) -> Unit,
    private val useDarkMode: Boolean
) : ListAdapter<MediaKeyboardTab, ViewHolder>(DIFF_CALLBACK) {

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when {
            item.drawableId != null -> TYPE_REGULAR_TAB
            item.isDivider -> TYPE_DIVIDER
            item.stickerPack != null -> TYPE_STICKER_PACK
            else -> error("No such an item type.")
        }
    }

    override fun submitList(list: MutableList<MediaKeyboardTab>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_REGULAR_TAB -> {
                val binding = ItemMediaKeyboardTabBinding.inflate(inflater, parent, false)
                MediaKeyboardTabViewHolder(binding, useDarkMode, callback)
            }
            TYPE_DIVIDER -> {
                val binding = ItemMediaKeyboardTabDividerBinding.inflate(inflater, parent, false)
                MediaKeyboardDividerViewHolder(binding)
            }
            TYPE_STICKER_PACK -> {
                val binding = ItemMediaKeyboardTabStickerBinding.inflate(inflater, parent, false)
                MediaKeyboardStickerPackViewHolder(binding, useDarkMode, callback, stickerPackViewedCallback)
            }
            else -> error("No such a view type.")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is MediaKeyboardTabViewHolder -> holder.bind(getItem(position))
            is MediaKeyboardStickerPackViewHolder -> holder.bind(getItem(position))
        }
    }

    class MediaKeyboardTabViewHolder(
        private val binding: ItemMediaKeyboardTabBinding,
        private val useDarkMode: Boolean,
        private val callback: (MediaKeyboardTab) -> Unit,
    ) : ViewHolder(binding.root) {

        fun bind(item: MediaKeyboardTab) {
            with(binding) {
                ivTabImage.setThrottledClickListener { callback.invoke(item) }
                item.drawableId?.let { drawableId ->
                    ivTabImage.setImageResource(drawableId)
                    setupPaddings(drawableId)
                }
                if (item.checked) {
                    root.background = ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.bg_mediakeyboard_tab
                    )
                    if (useDarkMode) {
                        root.setBackgroundTint(R.color.editor_widgets_content)
                        ivTabImage.setTint(R.color.ui_white)
                    } else {
                        root.setBackgroundTint(R.color.ui_white)
                        ivTabImage.setTint(R.color.ui_purple)
                    }
                } else {
                    root.background = null
                    ivTabImage.setTint(R.color.ui_gray)
                }

                lavAddToFavorites.addAnimatorListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        ivTabImage.visible()
                        lavAddToFavorites.gone()
                        item.playAnimation = false
                    }
                })
                if (item.playAnimation) {
                    lavAddToFavorites.visible()
                    ivTabImage.gone()
                    lavAddToFavorites.playAnimation()
                }
            }
        }

        private fun setupPaddings(drawableId: Int) {
            val isRecentStickers = drawableId == DRAWABlE_RECENT_STICKERS
            if (isRecentStickers) {
                binding.ivTabImage.updatePadding(
                    paddingTop = RECENT_STICKERS_TOP_PADDING.dp,
                    paddingBottom = RECENT_STICKERS_BOTTOM_PADDING.dp
                )
            } else {
                binding.ivTabImage.updatePadding(
                    paddingTop = DEFAULT_TOP_PADDING.dp,
                    paddingBottom = DEFAULT_BOTTOM_PADDING.dp
                )
            }
        }

    }

    class MediaKeyboardDividerViewHolder(
        binding: ItemMediaKeyboardTabDividerBinding
    ) : ViewHolder(binding.root)

    class MediaKeyboardStickerPackViewHolder(
        private val binding: ItemMediaKeyboardTabStickerBinding,
        private val useDarkMode: Boolean,
        private val callback: (MediaKeyboardTab) -> Unit,
        private val stickerPackViewedCallback: (MediaKeyboardStickerPackUiModel) -> Unit
    ) : ViewHolder(binding.root) {

        fun bind(item: MediaKeyboardTab) {
            with(binding) {
                ivStickerIcon.setThrottledClickListener { callback.invoke(item) }
                ivStickerIcon.loadGlide(item.stickerPack?.preview)
                ivStickerPackNotViewed.setVisible(item.stickerPack?.viewed == false)
                if (item.checked) {
                    vgStickerBackground.background = ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.bg_mediakeyboard_tab
                    )
                    if (useDarkMode) {
                        vgStickerBackground.setBackgroundTint(
                            R.color.editor_widgets_content
                        )
                    }
                    if (item.stickerPack?.viewed == false) {
                        stickerPackViewedCallback.invoke(item.stickerPack)
                    }
                } else {
                    vgStickerBackground.background = null
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MediaKeyboardTab>() {
            override fun areItemsTheSame(
                oldItem: MediaKeyboardTab,
                newItem: MediaKeyboardTab
            ): Boolean {
                return oldItem.drawableId == newItem.drawableId
            }

            override fun areContentsTheSame(
                oldItem: MediaKeyboardTab,
                newItem: MediaKeyboardTab
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}
