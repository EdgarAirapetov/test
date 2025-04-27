package com.numplates.nomera3.modules.chat.helpers

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.GetMediaKeyboardStickersUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerPackUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.mapper.MediaKeyboardStickerUiMapper
import com.numplates.nomera3.modules.chat.ui.adapter.StickersSuggestionsAdapter
import com.numplates.nomera3.modules.chat.ui.entity.StickerSuggestionUiModel
import com.numplates.nomera3.modules.featuretoggles.ChatStickerSuggestionsFeatureToggle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ITEM_SPACING = 16
private const val POSITION_START = 0

class StickersSuggestionsDelegate(
    private val vStickersSuggestionsBackground: View,
    private val rvStickersSuggestions: RecyclerView,
    private val coroutineScope: CoroutineScope,
    private val chatStickerSuggestionsFeatureToggle: ChatStickerSuggestionsFeatureToggle?,
    private val clickListener: (MediaKeyboardStickerUiModel, String?) -> Unit
) {

    @Inject
    lateinit var stickersUiMapper: MediaKeyboardStickerUiMapper

    @Inject
    lateinit var getMediaKeyboardStickersUseCase: GetMediaKeyboardStickersUseCase

    private val stickerPacksList = mutableListOf<MediaKeyboardStickerPackUiModel>()
    private var currentEmoji: String? = null
    private var suggestionsEnabled: Boolean = true

    private var layoutManager: LinearLayoutManager? = null
    private val adapter by lazy {
        StickersSuggestionsAdapter { sticker ->
            clickListener.invoke(sticker, currentEmoji)
        }
    }

    init {
        App.component.inject(this)
        initStickersSuggestions()
    }

    fun enableSuggestions(enabled: Boolean) {
        this.suggestionsEnabled = enabled
        if (!enabled) rvStickersSuggestions.gone()
    }

    fun messageTextChanged(text: String) {
        if (chatStickerSuggestionsFeatureToggle?.isEnabled != true) return
        if (!suggestionsEnabled) return
        currentEmoji = null
        val stickers = getStickersFromMessageText(text)
        val stickersSuggestions = stickers.map { StickerSuggestionUiModel(it) }
        stickersSuggestions.firstOrNull()?.apply { isFirst = true }
        stickersSuggestions.lastOrNull()?.apply { isLast = true }
        if (stickers.isNotEmpty()) {
            adapter.submitList(stickersSuggestions)
            adapter.notifyDataSetChanged()
            layoutManager?.scrollToPositionWithOffset(POSITION_START, POSITION_START)
            rvStickersSuggestions.post {
                rvStickersSuggestions.visible()
                vStickersSuggestionsBackground.visible()
            }
        } else {
            vStickersSuggestionsBackground.gone()
            rvStickersSuggestions.gone()
        }
    }

    private fun initStickersSuggestions() {
        loadStickerPacks()
        initStickersSuggestionsRecyclerView()
    }

    private fun loadStickerPacks() {
        coroutineScope.launch {
            stickerPacksList.clear()
            stickerPacksList += runCatching {
                getMediaKeyboardStickersUseCase.invoke().stickerPacks.map(stickersUiMapper::mapStickerPackDomainToUiModel)
            }.getOrDefault(emptyList())
        }
    }

    private fun initStickersSuggestionsRecyclerView() {
        layoutManager = LinearLayoutManager(rvStickersSuggestions.context, LinearLayoutManager.HORIZONTAL, false)
        rvStickersSuggestions.layoutManager = layoutManager
        rvStickersSuggestions.adapter = adapter
        rvStickersSuggestions.addItemDecoration(object : ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                when (parent.getChildAdapterPosition(view)) {
                    0 -> outRect.left = ITEM_SPACING.dp
                    adapter.itemCount - 1 -> outRect.right = ITEM_SPACING.dp
                }
            }
        })
    }

    private fun getStickersFromMessageText(text: String): List<MediaKeyboardStickerUiModel> {
        val stickers = mutableListOf<MediaKeyboardStickerUiModel>()
        stickerPacksList.forEach { stickerPack ->
            stickerPack.stickers.forEach { sticker ->
                if (isStickerSuitsForMessage(text, sticker)) {
                    stickers.add(sticker)
                }
            }
        }
        return stickers
    }

    private fun isStickerSuitsForMessage(message: String, sticker: MediaKeyboardStickerUiModel): Boolean {
        if (message.isEmpty()) return false
        val emojiInMessage = sticker.emoji.firstOrNull { it == message }
        if (emojiInMessage != null) currentEmoji = emojiInMessage
        val isEmoji = emojiInMessage != null
        val isText = sticker.keywords.any { it.equals(message.trimEnd(), true) }
        return isEmoji || isText
    }

}
