package com.numplates.nomera3.presentation.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.util.TypedValue
import android.webkit.URLUtil
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.util.PatternsCompat
import com.meera.core.extensions.addClickWithData
import com.meera.core.extensions.color
import com.meera.core.extensions.getListRangeSpannableColored
import com.meera.core.extensions.isFalse
import com.meera.core.extensions.italic
import com.meera.core.extensions.normal
import com.meera.core.extensions.size
import com.meera.db.models.message.ParsedUniquename
import com.meera.db.models.message.UniquenameEntity
import com.meera.db.models.message.UniquenameSpanData
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.data.ChatBirthdayUiEntity
import com.numplates.nomera3.modules.chat.helpers.ClickableMovementMethod
import com.numplates.nomera3.modules.chat.ui.adapter.ChatMessagesAdapter
import com.numplates.nomera3.modules.tags.data.entity.SpanDataClickType
import com.numplates.nomera3.modules.tags.data.entity.UniquenameType
import com.numplates.nomera3.modules.tags.ui.MovementMethod
import com.numplates.nomera3.presentation.view.ui.MeeraTextViewWithImages
import com.numplates.nomera3.presentation.view.ui.TextViewProfanitySpanner
import com.numplates.nomera3.presentation.view.ui.TextViewWithImages
import com.numplates.nomera3.presentation.view.utils.ROAD_POSTS_TEXT_SIZE


const val TEXT_SPACE_DEFAULT_SEND_24H =
    "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0"
const val TEXT_SPACE_DEFAULT_RECEIVE_24H = "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0"
@Suppress("detekt:MaxLineLength")
const val TEXT_SPACE_DEFAULT_SEND_12H =
    "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0"
const val TEXT_SPACE_DEFAULT_RECEIVE_12H =
    "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0"

private const val MAX_TEXT_LENGTH = 300

private data class SpanDataContainer(
    val id: String?,
    val text: String?,
    val startSpanPos: Int?,
    val endSpanPos: Int?,
    val type: String?,
    val userId: Long?,
    val groupId: Long?,
    val symbol: String?,
    val url: String?
)

fun AppCompatEditText?.getTrueTextLengthWithProfanity(spanData: List<UniquenameSpanData>?): Int {
    return this?.text?.toString()?.getTrueTextLengthWithProfanity(spanData) ?: 0
}

fun AppCompatTextView?.getTrueTextLengthWithProfanity(spanData: List<UniquenameSpanData>?): Int {
    return this?.text?.toString()?.getTrueTextLengthWithProfanity(spanData) ?: 0
}

fun String.getTrueTextLengthWithProfanity(spanData: List<UniquenameSpanData>?): Int {
    if (spanData == null) {
        return length
    }

    var resultLength = length

    spanData.forEach { spanItem ->
        if (spanItem.type == UniquenameType.PROFANITY.value) {
            resultLength += spanItem.tag?.length ?: 0
            resultLength -= 1
        }
    }

    return resultLength
}

fun ParsedUniquename.getTrueTextWithProfanity(): String{
    var text = text.toString()
    for(item in spanData){
        item.tag?.let {
            text = text.replaceFirst(item.symbol.toString(), item.tag.toString())
        }
    }
    return text
}

/**
 * TODO Тех-долг https://nomera.atlassian.net/browse/BR-11316
 */
fun parseUniquename(input: String?, tags: List<UniquenameEntity?>?): ParsedUniquename {
    val tagStringLength = 42    // Длина строки тэга (<tag:324545-7326....>>)
    val spanData = mutableListOf<SpanDataContainer?>()
    var offset = 0

    val regex = "<tag:(.*?)>".toRegex()
    val parsedText = input?.replace(regex) { matchResult ->
        tags?.forEach { tag ->
            if (matchResult.groupValues[1] == tag?.id) {
                val range = matchResult.range
                val nameLength = if (tag.type == UniquenameType.PROFANITY.value) {
                    tag.options?.symbol?.length ?: 0
                } else {
                    tag.text?.length ?: 0
                }

                val start = range.first - offset
                val end = range.last.plus(1) - tagStringLength + nameLength - offset

                spanData.add(
                    SpanDataContainer(
                        id = tag.id,
                        text = tag.text,
                        startSpanPos = start,
                        endSpanPos = end,
                        type = tag.type,
                        userId = tag.options?.userId,
                        groupId = tag.options?.groupId,
                        symbol = tag.options?.symbol,
                        url = tag.options?.url
                    )
                )
                offset += tagStringLength - nameLength
                if (tag.type == UniquenameType.PROFANITY.value) {
                    return@replace tag.options?.symbol ?: ""
                }
                return@replace tag.text ?: ""
            }
        }
        ""      // stub
    }

    val listSpanData = spanData.map {
        UniquenameSpanData(
            id = it?.id,
            tag = it?.text,
            type = it?.type,
            startSpanPos = it?.startSpanPos,
            endSpanPos = it?.endSpanPos,
            userId = it?.userId,
            groupId = it?.groupId,
            symbol = it?.symbol,
            url = it?.url
        )
    }

    return ParsedUniquename(parsedText, listSpanData)
}


/**
 * Parse unique name without tag data from server
 */
fun parseUniquename(input: String?): ParsedUniquename {
    val spanData = mutableListOf<UniquenameSpanData>()
    val regex = "(@[A-Za-z0-9-_]+)(?:@[A-Za-z0-9-_]+)*".toRegex()
    regex.findAll(input ?: "").forEach { match ->
        spanData.add(
            UniquenameSpanData(
                null,
                match.groupValues[1],
                UniquenameType.NO_CLICK_CHAT.value,
                match.range.first,
                match.range.last + 1,
                0,
                0,
                null
            )
        )
    }
    return ParsedUniquename(input, spanData)
}

/**
 * For TextView with images
 */
fun spanTagsText(
    context: Context,
    tvText: TextViewWithImages,
    post: ParsedUniquename?,
    linkColor: Int = R.color.uiKitColorAccentPrimary,
    click: (SpanDataClickType) -> Unit = { SpanDataClickType.ClickUnknownUser }
) {
    val text = handleSpanTags(context, post, linkColor, click)
    tvText.strBuilder = text
    tvText.text = text
    tvText.movementMethod = MovementMethod
    tvText.isFocusable = false
}

fun spanTagsText(
    context: Context,
    tvText: MeeraTextViewWithImages,
    post: ParsedUniquename?,
    linkColor: Int = R.color.uiKitColorForegroundLink,
    click: (SpanDataClickType) -> Unit = { SpanDataClickType.ClickUnknownUser }
) {
    val text = handleSpanTags(context, post, linkColor, click)
    tvText.strBuilder = text
    tvText.text = text
    tvText.movementMethod = MovementMethod
    tvText.isFocusable = false
}

fun spanTagsTextInPosts(
    context: Context,
    tvText: TextViewWithImages,
    post: ParsedUniquename?,
    linkColor: Int = R.color.uiKitColorAccentPrimary,
    click: (SpanDataClickType) -> Unit = { SpanDataClickType.ClickUnknownUser },
    font: Typeface? = null,
    movementMethod: android.text.method.MovementMethod? = MovementMethod
) {
    val text = handleSpanTagsInPosts(context, post, linkColor, click, font)
    tvText.strBuilder = text
    tvText.text = text
    tvText.movementMethod = movementMethod
    tvText.isFocusable = false
}

fun spanTagsTextInPosts(
    context: Context,
    tvText: MeeraTextViewWithImages,
    post: ParsedUniquename?,
    linkColor: Int = R.color.uiKitColorForegroundLink,
    click: (SpanDataClickType) -> Unit = { SpanDataClickType.ClickUnknownUser },
    font: Typeface? = null,
    movementMethod: android.text.method.MovementMethod? = MovementMethod
) {
    val text = handleSpanTagsInPosts(context, post, linkColor, click, font)
    tvText.strBuilder = text
    tvText.text = text
    tvText.movementMethod = movementMethod
    tvText.isFocusable = false
}

fun spanTagsTextInVideoPosts(
    context: Context,
    tvText: TextView,
    post: ParsedUniquename?,
    linkColor: Int = R.color.uiKitColorAccentPrimary,
    click: (SpanDataClickType) -> Unit = { SpanDataClickType.ClickUnknownUser }
) {
    val text = handleSpanTags(context, post, linkColor, click)
    val spannedText = TextViewProfanitySpanner.getTextWithImages(context, text, tvText.lineHeight.toFloat())
    tvText.setText(spannedText, TextView.BufferType.SPANNABLE)
    tvText.movementMethod = LinkMovementMethod.getInstance()
    tvText.isFocusable = false
}

fun spanTagsText(
    context: Context,
    post: ParsedUniquename?,
    linkColor: Int = R.color.uiKitColorForegroundLink,
    click: (SpanDataClickType) -> Unit = { SpanDataClickType.ClickUnknownUser }
): SpannableStringBuilder = handleSpanTags(context, post?.copy(text = post.text?.trim()), linkColor, click)

/**
 * todo смотри комментарий в [ChatFragmentNew::onUniquenameClicked]
 * */
fun spanTagsChatText(
    context: Context,
    tvText: TextView,
    post: ParsedUniquename?,
    linkColor: Int = R.color.uiKitColorAccentPrimary,
    spaceMask: String = "",
    isTrimText: Boolean = false,
    chatBirthdayData: ChatBirthdayUiEntity? = null,
    click: (SpanDataClickType) -> Unit = { SpanDataClickType.ClickUnknownUser }
) {
    val text = handleSpanTags(context, post, linkColor, click)
    text.append(spaceMask)
    text.setSpan(
        AbsoluteSizeSpan(ChatMessagesAdapter.MESSAGE_TEXT_SIZE_SP.toInt(), true),
        text.length, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    if (!chatBirthdayData?.birthdayTextRanges.isNullOrEmpty()
        && chatBirthdayData?.isSomeOneHasBirthday == true
    ) {
        text.getListRangeSpannableColored(
            color = context.color(chatBirthdayData.birthdayTextColor),
            rangeList = chatBirthdayData.birthdayTextRanges ?: listOf()
        ) { click.invoke(SpanDataClickType.ClickBirthdayText) }
    }
    val txt = if (isTrimText && text.length > MAX_TEXT_LENGTH) text.substring(0, MAX_TEXT_LENGTH) else text
    tvText.text = txt
    // bellow code is required !!!
    tvText.movementMethod = ClickableMovementMethod.getInstance()
    tvText.isClickable = false
    tvText.isLongClickable = false
}

fun spanCharSequence(
    context: Context,
    post: ParsedUniquename?,
    linkColor: Int = R.color.uiKitColorAccentPrimary,
    spaceMask: String = "",
    isTrimText: Boolean = false,
    chatBirthdayData: ChatBirthdayUiEntity? = null,
    click: (SpanDataClickType) -> Unit = { SpanDataClickType.ClickUnknownUser }
): CharSequence {
    val text = handleSpanTags(context, post, linkColor, click)
    text.append(spaceMask)
    text.setSpan(
        AbsoluteSizeSpan(ChatMessagesAdapter.MESSAGE_TEXT_SIZE_SP.toInt(), true),
        text.length,
        text.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    if (chatBirthdayData != null && !chatBirthdayData.birthdayTextRanges.isNullOrEmpty()) {
        text.getListRangeSpannableColored(
            color = context.color(chatBirthdayData.birthdayTextColor),
            rangeList = chatBirthdayData.birthdayTextRanges
        ) { click.invoke(SpanDataClickType.ClickBirthdayText) }
    }
    return if (isTrimText && text.length > MAX_TEXT_LENGTH) text.substring(0, MAX_TEXT_LENGTH) else text
}

@Suppress("MoveLambdaOutsideParentheses")
private fun handleSpanTags(
    context: Context,
    post: ParsedUniquename?,
    linkColor: Int = R.color.uiKitColorAccentPrimary,
    click: (SpanDataClickType) -> Unit = { _ -> SpanDataClickType.ClickUnknownUser }
): SpannableStringBuilder {
    val postText = post?.text.orEmpty()
    val text = SpannableStringBuilder(postText)
    var lastSpanIndex = 0
    var lastSpanIndexForBadWord = 0
    post?.spanData?.sortedBy { it.startSpanPos ?: 0 }?.forEach { spanData ->
        // we need recalculation of other text(hashtag,uniquename) after bad word spoiler
        var range = IntRange.EMPTY
        if (spanData.type != UniquenameType.PROFANITY.value) {
            val start = text.indexOf(spanData.tag ?: "", lastSpanIndex)
            val end = start + (spanData.tag?.length ?: 0)
            lastSpanIndex = end
            range = IntRange(start, end)
        }
        // TYPE Unique name
        if (spanData.type == UniquenameType.UNIQNAME.value) {
            text.addClickWithData("", range, ContextCompat.getColor(context, linkColor), {
                click.invoke(SpanDataClickType.ClickUserId(spanData.userId))
            })
        }
        if (spanData.type == UniquenameType.FONT_STYLE_ITALIC.value) {
            val italicString = spanData.symbol ?: ""
            val startPos = text.indexOf(italicString, spanData.startSpanPos ?: 0)
            val endPos = startPos + (italicString.length)
            val rangeForItalic = IntRange(startPos, endPos)
            text.italic(rangeForItalic.first, rangeForItalic.last)
        }
        // TYPE No click chat send
        if (spanData.type == UniquenameType.NO_CLICK_CHAT.value) {
            text.color(ContextCompat.getColor(context, linkColor), range)
        }
        // TYPE Hashtag
        if (spanData.type == UniquenameType.HASHTAG.value) {
            text.addClickWithData("", range, ContextCompat.getColor(context, linkColor), {
                click.invoke(SpanDataClickType.ClickHashtag(spanData.tag))
            })
        }
        // TYPE Bad Words
        if (spanData.type == UniquenameType.PROFANITY.value) {
            // we need recalculation of bad word position after spoiler
            val startPos = text.indexOf(spanData.symbol ?: "", lastSpanIndexForBadWord)
            val endPos = startPos + (spanData.symbol?.length ?: 0)
            lastSpanIndexForBadWord = endPos
            val rangeForBadWords = IntRange(startPos, endPos)
            text.addClickWithData("", rangeForBadWords, ContextCompat.getColor(context, linkColor), {
                click.invoke(SpanDataClickType.ClickBadWord(spanData.tag, startPos, endPos, spanData.id))
            })
        }
        if (spanData.type == UniquenameType.LINK.value) {
            text.addClickWithData(spanData.url, range, ContextCompat.getColor(context, linkColor)) {
                click.invoke(SpanDataClickType.ClickLink(spanData.url))
            }
        }
    }

    handleUrlLinks(context, text, linkColor, click)

    return text
}

fun handleSpanTagsInPosts(
    context: Context,
    post: ParsedUniquename?,
    linkColor: Int = R.color.uiKitColorForegroundLink,
    click: (SpanDataClickType) -> Unit = { clickType ->
        SpanDataClickType.ClickUnknownUser
    },
    font: Typeface? = null
): SpannableStringBuilder {
    val postText = if (post?.showFullText == true) post.text else (post?.shortText ?: post?.text.orEmpty())
    val text = SpannableStringBuilder(postText.orEmpty())
    var lastSpanIndex = 0
    var lastSpanIndexForBadWord = 0
    post?.spanData?.sortedBy { it.startSpanPos ?: 0 }?.forEach { spanData ->
        // we need recalculation of other text(hashtag,uniquename) after bad word spoiler
        var range = IntRange.EMPTY
        if (spanData.type != UniquenameType.PROFANITY.value) {
            val start = text.indexOf(spanData.tag ?: "", lastSpanIndex)
            val end = start + (spanData.tag?.length ?: 0)
            lastSpanIndex = end
            range = IntRange(start, end)
        }
        // TYPE Unique name
        if (spanData.type == UniquenameType.UNIQNAME.value) {
            text.addClickWithData("", range, ContextCompat.getColor(context, linkColor), {
                click.invoke(SpanDataClickType.ClickUserId(spanData.userId))
            })
        }
        if (spanData.type == UniquenameType.FONT_STYLE_ITALIC.value) {
            val italicString = spanData.symbol ?: ""
            val startPos = text.indexOf(italicString, spanData.startSpanPos ?: 0)
            val endPos = startPos + (italicString.length)
            val rangeForItalic = IntRange(startPos, endPos)
            text.italic(rangeForItalic.first, rangeForItalic.last, font)
        }
        // TYPE No click chat send
        if (spanData.type == UniquenameType.NO_CLICK_CHAT.value) {
            text.color(ContextCompat.getColor(context, linkColor), range)
        }
        // TYPE Hashtag
        if (spanData.type == UniquenameType.HASHTAG.value) {
            text.addClickWithData("", range, ContextCompat.getColor(context, linkColor), {
                click.invoke(SpanDataClickType.ClickHashtag(spanData.tag))
            })
        }
        // TYPE Bad Words
        if (spanData.type == UniquenameType.PROFANITY.value && spanData.symbol != null) {
            // we need recalculation of bad word position after spoiler
            val spanSymbol = spanData.symbol ?: ""
            val startPos = text.indexOf(spanSymbol, lastSpanIndexForBadWord)
            val endPos = startPos + (spanSymbol.length)
            lastSpanIndexForBadWord = endPos
            val rangeForBadWords = IntRange(startPos, endPos)
            text.addClickWithData("", rangeForBadWords, ContextCompat.getColor(context, linkColor), {
                click.invoke(SpanDataClickType.ClickBadWord(spanData.tag, startPos, endPos, spanData.id))
            })
        }
        if (spanData.type == UniquenameType.PROFANITY_NO_LINK.value && spanData.symbol != null) {
            val string = spanData.symbol ?: ""
            val startPos = text.indexOf(string, spanData.startSpanPos ?: 0)
            val endPos = startPos + (string.length)
            val rangeForText = IntRange(startPos, endPos)
            text.normal(rangeForText.first..rangeForText.last)
        }
    }
    if (post?.showFullText.isFalse() && post?.shortText.isNullOrEmpty().not()) {
        val moreText = context.getString(R.string.more_with_tab)
        val range = IntRange(text.length - moreText.length, text.length)
        text.addClickWithData(data = "", range = range, color = ContextCompat.getColor(context, linkColor)) {
            click.invoke(SpanDataClickType.ClickMore)
        }
        val textSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            ROAD_POSTS_TEXT_SIZE,
            context.resources.displayMetrics
        ).toInt()
        text.size(textSizePx = textSizePx, start = range.first, end = range.last)
    }
    handleUrlLinks(context, text, linkColor, click)
    return text
}

/* PatternsCompat.AUTOLINK_WEB_URL доступен только из своей библиотеки, но так как это необходимый нам паттерн,
используем напрямую с аннотацией */
@SuppressLint("RestrictedApi")
private fun handleUrlLinks(context: Context, text: SpannableStringBuilder, linkColor: Int,
                        click: (SpanDataClickType) -> Unit = { _ -> SpanDataClickType.ClickUnknownUser }) {
    PatternsCompat.AUTOLINK_WEB_URL.toRegex().findAll(text.toString()).filter { it.value.isNotEmpty() }
        .forEach { matchResult ->
            val first = matchResult.range.first
            val last = matchResult.range.last + if (text.toString().length > matchResult.range.last + 1) {
                if (text.toString().toCharArray()[matchResult.range.last + 1].toString() == "/") 2 else 1 } else 1
            val range = IntRange(first, last)
            text.addClickWithData("", range, ContextCompat.getColor(context, linkColor)) {
                val url = URLUtil.guessUrl(matchResult.value)
                click.invoke(SpanDataClickType.ClickLink(url))
            }
        }
}

fun TextViewWithImages.setTextNoSpans(text: String?) {
    val sb = SpannableStringBuilder(text)
    this.strBuilder = sb
    this.text = text
}

fun MeeraTextViewWithImages.setTextNoSpans(text: String?) {
    val sb = SpannableStringBuilder(text)
    this.strBuilder = sb
    this.text = text
}
