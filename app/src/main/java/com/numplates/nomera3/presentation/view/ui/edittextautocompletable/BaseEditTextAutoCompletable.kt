package com.numplates.nomera3.presentation.view.ui.edittextautocompletable

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import timber.log.Timber
import kotlin.math.min

open class BaseEditTextAutoCompletable : AppCompatEditText {

    protected val textWatcherList: MutableList<TextWatcher> = mutableListOf()

    protected val customViewScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    protected val wordDividedBySpaceRegex = "\\S*[a-zA-Z@]+\\S*".toRegex()

    // c @JvmOverloads не работает https://medium.com/@mmlodawski/https-medium-com-mmlodawski-do-not-always-trust-jvmoverloads-5251f1ad2cfe
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun replaceUniqueNameBySuggestion(oldUniqueName: String, suggestion: String) {
        val suggestionWithSpaceEnd = suggestion.let { "@$it " }

        text?.subSequence(0, selectionEnd)
            ?.lastIndexOf("@")
            ?.also { startIndex: Int ->
                text?.replace(
                    startIndex,
                    min((startIndex + oldUniqueName.length), text?.length ?: 0),
                    suggestionWithSpaceEnd
                )
            }

        text?.subSequence(0, selectionEnd)
            ?.lastIndexOf(suggestionWithSpaceEnd)
            ?.let { it + suggestionWithSpaceEnd.length }
            ?.also { setSelection(it) }
    }

    open fun replaceUniqueNameBySuggestion(suggestion: String) {
        val suggestionWithSpaceEnd = suggestion.let { "@$it " }

        textWatcherList.forEach(this::removeTextChangedListener)

        text?.subSequence(0, selectionEnd)
            ?.lastIndexOf("@")
            ?.also { startIndex: Int ->
                text?.replace(
                    startIndex,
                    min(selectionEnd, text?.length ?: 0),
                    suggestionWithSpaceEnd
                )
            }

        text?.subSequence(0, selectionEnd)
            ?.lastIndexOf(suggestionWithSpaceEnd)
            ?.let { it + suggestionWithSpaceEnd.length }
            ?.also { setSelection(it) }

        textWatcherList.forEach(this::addTextChangedListener)
    }

    @Synchronized
    protected fun getTextBeforeCursorOrNull(): CharSequence? {
        return try {
            val newSelectionEnd = selectionEnd
                .takeIf { it >= 0 }
                ?: 0

            if (newSelectionEnd > 0) {
                text?.subSequence(0, newSelectionEnd)
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    protected fun getTagBeforePointer(tagSymbol: Char): String? {
        val textBeforeCursor = getTextBeforeCursorOrNull() ?: return null
        val specialCharLastIndex = textBeforeCursor.indexOfLast { it == tagSymbol }
        if (specialCharLastIndex >= 0 && specialCharLastIndex < textBeforeCursor.length) {
            return textBeforeCursor.substring(specialCharLastIndex, textBeforeCursor.length)
        } else {
            return null
        }
    }

    protected fun getUniqueNameUnderPointer(): String? {
        return getUniqueNameUnderCursorRegex()
    }

    private fun getUniqueNameUnderCursorRegex(): String? {
        try {
            if (selectionEnd > 0) {
                wordDividedBySpaceRegex
                    .findAll(text ?: "")
                    .forEach { matchResult: MatchResult ->
                        if (matchResult.range.first < selectionEnd && matchResult.range.last + 1 >= selectionEnd) {
                            return matchResult.value
                        }
                    }
            }

            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    protected fun getUniqueNameUnderCursorRegexWithPosition(): Pair<Int, Int>? {
        try {
            if (selectionEnd > 0) {
                wordDividedBySpaceRegex
                    .findAll(text ?: "")
                    .forEach { matchResult: MatchResult ->
                        if (matchResult.range.first < selectionEnd && matchResult.range.last + 1 >= selectionEnd) {
                            return Pair(matchResult.range.first, matchResult.range.last)
                        }
                    }
            }

            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }


    protected fun isUniqueNameHighlightable(charSequence: CharSequence?): Boolean {
        return charSequence
            ?.toString()
            ?.let { UniqueNameValidationStrategy.isHighlightable.validate(it) }
            ?: false
    }

    protected fun isHashtagHighlightable(charSequence: CharSequence?): Boolean {
        return charSequence
            ?.toString()
            ?.let { HashtagValidationStrategy.IsHighlightable.validate(it) }
            ?: false
    }

    protected fun getTextBeforeCursorUntilLastSpaceOrNewLine(): Triple<Int, Int, String>? {
        try {
            val textBeforeCursor = getTextBeforeCursorOrNull() ?: return null

            val firstSpaceBeforeCursor = textBeforeCursor
                .indexOfLast { it == ' ' || it == '\n' }
                .takeIf { it >= 0 }
                ?.plus(1)
                ?: 0

            return text
                ?.substring(firstSpaceBeforeCursor, selectionEnd)
                ?.let { Triple(firstSpaceBeforeCursor, selectionEnd, it) }
        } catch (e: Exception) {
            Timber.e(e)
            return null
        }
    }

    override fun onDetachedFromWindow() {
        customViewScope.cancel()
        super.onDetachedFromWindow()
    }
}
