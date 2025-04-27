package com.numplates.nomera3.modules.notifications.ui.mapper

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import androidx.core.text.color
import androidx.core.text.italic
import com.meera.db.models.message.UniquenameEntity
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.tags.data.entity.UniquenameType
import javax.inject.Inject

class TagsMapper @Inject constructor(
    private val context: Context,
) {

    fun mapData(source: CharSequence, tags: List<UniquenameEntity>): Spannable {
        val regex = "<tag:(.*?)>".toRegex()
        val output = SpannableStringBuilder()
        var match = regex.find(source)
        var lastIndex = 0
        while (match != null) {
            output.append(source.subSequence(lastIndex, match.range.first))
            appendTagData(output, tags, match)
            lastIndex = match.range.last + 1
            match = match.next()
        }
        output.append(source.subSequence(lastIndex, source.length))
        return output
    }

    private fun appendTagData(
        builder: SpannableStringBuilder,
        tags: List<UniquenameEntity>,
        match: MatchResult
    ) {
        val tag = tags.find { match.groupValues[1] == it.id }
        when (tag?.type) {
            UniquenameType.PROFANITY_NO_LINK.value,
            UniquenameType.PROFANITY.value -> {
                builder.append(tag.options?.symbol ?: tag.text ?: match.value)
            }

            UniquenameType.UNIQNAME.value -> {
                builder.color(context.getColor(R.color.uiKitColorForegroundLink)) { append(tag.text) }
            }

            UniquenameType.NO_CLICK_CHAT.value -> {
                builder.color(context.getColor(R.color.uiKitColorForegroundLink)) { append(tag.text) }
            }

            UniquenameType.HASHTAG.value -> {
                builder.color(context.getColor(R.color.uiKitColorForegroundLink)) { append(tag.text) }
            }

            UniquenameType.LINK.value -> {
                builder.color(context.getColor(R.color.uiKitColorForegroundLink)) { append(tag.text) }
            }

            UniquenameType.FONT_STYLE_ITALIC.value -> {
                builder.italic { append(tag.text) }
            }

            else -> {
                builder.append(tag?.text ?: match.value)
            }
        }
    }
}
