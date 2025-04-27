package com.numplates.nomera3.modules.chat.mediakeyboard.ui.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setBackgroundTint
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setTint
import com.meera.core.extensions.setVisible
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemMediaKeyboardTabBinding
import com.numplates.nomera3.databinding.ItemMediaKeyboardTabDividerBinding
import com.numplates.nomera3.databinding.ItemMediaKeyboardTabStickerBinding
import com.numplates.nomera3.modules.chat.mediakeyboard.data.entity.MediaKeyboardTab
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerPackUiModel
import timber.log.Timber

private const val TYPE_REGULAR_TAB = 0
private const val TYPE_DIVIDER = 1
private const val TYPE_STICKER_PACK = 2

class MeeraMediaKeyboardTabsAdapter(
    private val callback: (MediaKeyboardTab) -> Unit,
    private val stickerPackViewedCallback: (MediaKeyboardStickerPackUiModel) -> Unit,
    private val useDarkMode: Boolean
) : ListAdapter<MediaKeyboardTab, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

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
        Timber.d("CHAT SC submit list $list")
        super.submitList(list.orEmpty())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_REGULAR_TAB -> {
                val binding = ItemMediaKeyboardTabBinding.inflate(inflater, parent, false)
                MediaKeyboardTabViewHolder(binding, useDarkMode, ::onItemClicked)
            }

            TYPE_DIVIDER -> {
                val binding = ItemMediaKeyboardTabDividerBinding.inflate(inflater, parent, false)
                MediaKeyboardDividerViewHolder(binding)
            }

            TYPE_STICKER_PACK -> {
                val binding = ItemMediaKeyboardTabStickerBinding.inflate(inflater, parent, false)
                MediaKeyboardStickerPackViewHolder(binding, useDarkMode, ::onItemClicked, stickerPackViewedCallback)
            }

            else -> error("No such a view type.")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MediaKeyboardTabViewHolder -> holder.bind(getItem(position))
            is MediaKeyboardStickerPackViewHolder -> holder.bind(getItem(position))
        }
    }

    private fun onItemClicked(adapterPosition: Int) {
        callback.invoke(getItem(adapterPosition))
    }

    class MediaKeyboardTabViewHolder(
        private val binding: ItemMediaKeyboardTabBinding,
        private val useDarkMode: Boolean,
        private val callback: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setThrottledClickListener { callback.invoke(bindingAdapterPosition) }
        }

        fun bind(item: MediaKeyboardTab) {
            with(binding) {
                Timber.d("CHAT SC $item")
                item.drawableId?.let(ivTabImage::setImageResource)
                ivTabImage.setTint(R.color.uiKitColorForegroundSecondary)
                if (item.checked) {
                    root.background = ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.bg_meera_mediakeyboard_tab
                    )
                    if (useDarkMode) {
                        root.setBackgroundTint(R.color.editor_widgets_content)
                    } else {
                        root.setBackgroundTint(R.color.uiKitColorBackgroundSecondary)
                    }
                } else {
                    root.background = null
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
    }

    class MediaKeyboardDividerViewHolder(
        binding: ItemMediaKeyboardTabDividerBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class MediaKeyboardStickerPackViewHolder(
        private val binding: ItemMediaKeyboardTabStickerBinding,
        private val useDarkMode: Boolean,
        private val callback: (Int) -> Unit,
        private val stickerPackViewedCallback: (MediaKeyboardStickerPackUiModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setThrottledClickListener { callback.invoke(bindingAdapterPosition) }
        }

        fun bind(item: MediaKeyboardTab) {
            with(binding) {
                ivStickerIcon.loadGlide(item.stickerPack?.preview)
                ivStickerPackNotViewed.setVisible(item.stickerPack?.viewed == false)
                if (item.checked) {
                    vgStickerBackground.background = ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.bg_meera_mediakeyboard_tab
                    )
                    if (useDarkMode) {
                        vgStickerBackground.setBackgroundTint(R.color.editor_widgets_content)
                    } else {
                        vgStickerBackground.setBackgroundTint(R.color.transparent)
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
