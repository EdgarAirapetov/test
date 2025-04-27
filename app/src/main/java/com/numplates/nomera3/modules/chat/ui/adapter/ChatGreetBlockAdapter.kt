package com.numplates.nomera3.modules.chat.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toInt
import com.meera.core.extensions.visible
import com.numplates.nomera3.databinding.ItemChatGreetingBlockBinding
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerUiModel
import com.numplates.nomera3.presentation.view.utils.inflateBinding
import timber.log.Timber

class ChatGreetBlockAdapter(
    private val listener: (stickerId: Int?) -> Unit
) : RecyclerView.Adapter<ChatGreetBlockAdapter.GreetBlockViewHolder>() {

    var text: String = String.empty()
    var needToShowGreeting: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var sticker: MediaKeyboardStickerUiModel? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GreetBlockViewHolder {
        val binding = parent.inflateBinding(ItemChatGreetingBlockBinding::inflate)
        return GreetBlockViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: GreetBlockViewHolder, position: Int) {
        holder.bind(text, sticker)
    }

    override fun getItemCount(): Int = needToShowGreeting.toInt()

    inner class GreetBlockViewHolder(
        private val binding: ItemChatGreetingBlockBinding,
        private val listener: (stickerId: Int?) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(text: String, sticker: MediaKeyboardStickerUiModel?) {
            binding.apply {
                tvSayHi.text = text
                root.setThrottledClickListener { listener.invoke(sticker?.id) }
                when {
                    !sticker?.lottieUrl.isNullOrBlank() -> {
                        lavGreetingSticker.visible()
                        ivGreetingSticker.gone()
                        tvGreetingEmoji.gone()
                        lavGreetingSticker.setFailureListener { Timber.e(it) }
                        lavGreetingSticker.setAnimationFromUrl(sticker?.lottieUrl)
                        lavGreetingSticker.playAnimation()
                    }
                    !sticker?.url.isNullOrBlank() -> {
                        lavGreetingSticker.gone()
                        ivGreetingSticker.visible()
                        tvGreetingEmoji.gone()
                        ivGreetingSticker.loadGlide(sticker?.url)
                    }
                    else -> {
                        lavGreetingSticker.gone()
                        ivGreetingSticker.gone()
                        tvGreetingEmoji.visible()
                    }
                }
            }
        }

    }

}
