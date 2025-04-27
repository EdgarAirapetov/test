package com.numplates.nomera3.modules.chat.ui.listeners

import android.content.Context
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.data.ChatBirthdayUiEntity
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import com.numplates.nomera3.modules.tags.data.entity.SpanDataClickType
import com.numplates.nomera3.presentation.utils.spanCharSequence
import timber.log.Timber

class MeeraSpanListenersMapper(
    private val context: Context,
    private val messagesListener: MeeraMessagesListener,
) {

    fun mapSpannableListeners(item: MessageUiModel): CharSequence {
        return spanCharSequence(
            context = context,
            post = item.content.tagSpan,
            linkColor = R.color.uiKitColorForegroundLink,
            chatBirthdayData = ChatBirthdayUiEntity(
                birthdayTextColor = R.color.uiKitBackAddOrange,
                birthdayTextRanges = item.birthdayRangesList
            ),
            click = { clickType ->
                when (clickType) {
                    is SpanDataClickType.ClickUserId -> {
                        messagesListener.onUniquenameClicked(clickType.userId ?: 0)
                    }

                    is SpanDataClickType.ClickUnknownUser -> {
                        messagesListener.onUniquenameUnknownProfileError()
                    }

                    is SpanDataClickType.ClickHashtag -> {
                        messagesListener.onHashtagClicked(clickType.hashtag)
                    }

                    is SpanDataClickType.ClickBirthdayText -> {
                        messagesListener.onBirthdayTextClicked()
                    }

                    is SpanDataClickType.ClickLink -> {
                        messagesListener.onLinkClicked(clickType.link)
                    }

                    else -> {
                        Timber.d("This data click type can not be handled.")
                    }
                }
            }
        )
    }
}
