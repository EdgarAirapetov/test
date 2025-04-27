package com.numplates.nomera3.presentation.view.ui.edittextautocompletable

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.core.content.ContextCompat.getColor
import androidx.core.text.toSpannable
import androidx.core.widget.doAfterTextChanged
import com.meera.core.extensions.empty
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.ui.edittextautocompletable.TagType.HASHTAG
import com.numplates.nomera3.presentation.view.ui.edittextautocompletable.TagType.UNIQUE_NAME
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.min
import kotlin.properties.Delegates

class EditTextAutoCompletable : BaseEditTextAutoCompletable {

    companion object {
        const val SELECTION_END_NOT_INITIALIZED_VALUE = -100
        const val HIGHLIGHT_DELAY = 40L
    }

    private var suggestionMenuEnabled = true

    var isHashtagFeatureActive: Boolean = true

    var isGetTextBeforeCursor: Boolean = true

    var shouldReplaceWholeUniqueName: Boolean = false

    var highlightColorRes: Int
        get() = _highlightColor
        set(value) {
            _highlightColor = value
            _highlightForegroundColorSpan = ForegroundColorSpan(_highlightColor)
        }

    var defaultColor: Int
        get() = _defaultColor
        set(value) {
            _defaultColor = value
            _defaultForegroundColorSpan = ForegroundColorSpan(_defaultColor)
        }

    var suggestionMenu: SuggestionMenuView?
        get() = _suggestionMenu
        set(value) {
            _suggestionMenu = value
        }

    var checkUniqueNameStrategy: UniqueNameValidationStrategy
        get() = _checkUniqueNameStrategy
        set(value) {
            _checkUniqueNameStrategy = value
        }

    var keyPreImeHook: ((keyCode: Int, event: KeyEvent?) -> Boolean)?
        get() = _keyPreImeHook
        set(value) {
            _keyPreImeHook = value
        }

    private var selectionEndObservable: Int? by Delegates.observable(SELECTION_END_NOT_INITIALIZED_VALUE) { _, _, _ ->
        launchFindTagJob()
    }

    private val _regexContainer: EditTextAutoCompletableRegex by lazy { EditTextAutoCompletableRegex() }
    private var _suggestionMenu: SuggestionMenuView? = null
    private var _checkUniqueNameStrategy: UniqueNameValidationStrategy = UniqueNameValidationStrategy.AddPost

    private var tagHighlighter: TextWatcher? = null
    private var textChangingListener: TextWatcher? = null

    private var _findTagJob: Job? = null

    private var _highlightAllUniqueNamesJob: Job? = null

    private var _highlightJob: Job? = null

    private var _highlightColor: Int = getColor(context, R.color.uiKitColorForegroundLink)
    private var _highlightForegroundColorSpan = ForegroundColorSpan(_highlightColor)

    private var _defaultColor: Int = getColor(context, R.color.uiKitColorForegroundPrimary)
    private var _defaultForegroundColorSpan = ForegroundColorSpan(_defaultColor)

    private var isTextChangeInProgress = true
    private var isInitBlockInvocationComplete: Boolean = false

    private var _keyPreImeHook: ((keyCode: Int, event: KeyEvent?) -> Boolean)? = null

    // c @JvmOverloads не работает https://medium.com/@mmlodawski/https-medium-com-mmlodawski-do-not-always-trust-jvmoverloads-5251f1ad2cfe
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {

        val newWatcherList = listOfNotNull(tagHighlighter, textChangingListener)
        textWatcherList.addAll(newWatcherList)

        isInitBlockInvocationComplete = true
    }

    /**
     * This code snippet allows you to dynamically attach a custom key event handler (keyPreImeHook) to your EditText.
     * When a key event occurs, it first checks if a hook function is present.  If so, it lets that hook function
     * decide whether to consume the event or not.  If no hook is present or the hook doesn't handle the event,
     * it defaults to the standard IME behavior.
     */
    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        return keyPreImeHook?.invoke(keyCode, event) ?: super.onKeyPreIme(keyCode, event)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initListeners()
    }

    private fun initListeners() {
        tagHighlighter = createTagHighlighter()
        textChangingListener = createTextChangingListener()

        addTextChangedListener(tagHighlighter)
        addTextChangedListener(textChangingListener)
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)

        // onSelectionChanged вызывается раньше инициализации selectionEndObservable
        // поэтому использован флаг isInitBlockInvocationComplete
        if (isInitBlockInvocationComplete && !isTextChangeInProgress && selStart == selEnd) {
            if (selectionEndObservable != null) {
                selectionEndObservable = selEnd
            }
        }
    }

    fun disableSuggestionMenu() {
        _suggestionMenu?.dismiss()
        suggestionMenuEnabled = false
    }

    fun enableSuggestionMenu() {
        suggestionMenuEnabled = true
    }

    private fun createTextChangingListener(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                isTextChangeInProgress = true
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // do nothing
            }

            override fun afterTextChanged(s: Editable?) {
                isTextChangeInProgress = false
            }
        }
    }

    private fun createTagHighlighter(): TextWatcher {

        return this.doAfterTextChanged { s: Editable? ->
            _highlightJob?.cancel()
            _highlightJob = customViewScope.launch {
                delay(100)
                highlightUniqueName()
            }
        }
    }

    private fun launchFindTagJob() {
        _findTagJob?.cancel()
        if (suggestionMenuEnabled) {
            _findTagJob = customViewScope.launch {
                delay(300)

                val uniqueNameBeforePointer = if (isGetTextBeforeCursor) {
                    getUniqueNameBeforePointer()
                } else {
                    getUniqueNameUnderPointer()
                        ?.takeIf {
                            _checkUniqueNameStrategy.validate(it)
                        }
                }

                if (uniqueNameBeforePointer != null) {
                    _suggestionMenu?.getSuggestedTagList(uniqueNameBeforePointer, UNIQUE_NAME)
                } else {
                    _suggestionMenu?.dismiss(UNIQUE_NAME)
                }

                if (isHashtagFeatureActive) {
                    val hashtagBeforePointer = getHashtagBeforePointer()
                    if (hashtagBeforePointer != null) {
                        if (hashtagBeforePointer.length == 1 && hashtagBeforePointer == "#") {
                            _suggestionMenu?.getSuggestedTagList(String.empty(), HASHTAG)
                        } else {
                            _suggestionMenu?.getSuggestedTagList(hashtagBeforePointer, HASHTAG)
                        }
                    } else {
                        _suggestionMenu?.dismiss(HASHTAG)
                    }
                }
            }
        } else {
            _suggestionMenu?.dismiss()
        }
    }

    override fun replaceUniqueNameBySuggestion(suggestion: String) {
        if (shouldReplaceWholeUniqueName) {
            replaceWholeUniqueNameBySuggestion(suggestion)
            return
        }

        val spannableSuggestion = suggestion.let { "@$it " }.toSpannable().apply {
            this.setSpan(
                ForegroundColorSpan(_highlightColor),
                0,
                suggestion.length + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        textWatcherList.forEach(this::removeTextChangedListener)

        text?.subSequence(0, selectionEnd)
            ?.lastIndexOf("@")
            ?.also { startIndex: Int ->
                text?.replace(
                    startIndex,
                    min(selectionEnd, text?.length ?: 0),
                    spannableSuggestion
                )
            }

        text?.subSequence(0, selectionEnd)
            ?.lastIndexOf(spannableSuggestion.toString())
            ?.let { it + spannableSuggestion.length }
            ?.also { setSelection(it) }

        textWatcherList.forEach(this::addTextChangedListener)
    }

    private fun replaceWholeUniqueNameBySuggestion(suggestion: String) {
        getUniqueNameUnderCursorRegexWithPosition()?.also { (st, en) ->
            text?.replace(st, en, suggestion)
        }
    }

    fun highlightAllUniqueNamesAndHashTags() {
        _highlightAllUniqueNamesJob?.cancel()
        _highlightAllUniqueNamesJob = customViewScope.launch {
            delay(HIGHLIGHT_DELAY)
            text?.let {
                _regexContainer.allWordsDividedBySpace.findAll(it)
            }?.filter { it.value.isNotEmpty() }
                ?.forEach { matchResult ->
                    val word = matchResult.value
                    val wordSpans = text?.getSpans(
                        matchResult.range.first,
                        matchResult.range.last + 1,
                        ForegroundColorSpan::class.java
                    ) ?: emptyArray()
                    if (isUniqueNameHighlightable(word) || isHashtagHighlightable(word)) {
                        if (wordSpans.isNotEmpty()) {
                            wordSpans
                                .filter { it.foregroundColor == _highlightColor }
                                .forEach { text?.removeSpan(it) }
                        }

                        text?.apply {
                            setSpan(
                                ForegroundColorSpan(_highlightColor),
                                matchResult.range.first,
                                min(matchResult.range.last + 1, length),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    } else {
                        wordSpans
                            .filter { it.foregroundColor == _highlightColor }
                            .forEach { text?.removeSpan(it) }
                    }
                }
        }
    }

    fun replaceHashtagBySuggestion(suggestion: String) {
        val spannableSuggestion = suggestion.let { "#$it " }.toSpannable().apply {
            this.setSpan(
                    ForegroundColorSpan(highlightColorRes),
                    0,
                    suggestion.length + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        textWatcherList.forEach(this::removeTextChangedListener)

        text?.subSequence(0, selectionEnd)
            ?.lastIndexOf("#")
            ?.also { startIndex: Int ->
                text?.replace(
                    startIndex,
                    min(selectionEnd, text?.length ?: 0),
                    spannableSuggestion
                )
            }

        text?.subSequence(0, selectionEnd)
            ?.lastIndexOf(spannableSuggestion.toString())
            ?.let { it + spannableSuggestion.length }
            ?.also { setSelection(it) }

        textWatcherList.forEach(this::addTextChangedListener)
    }

    private fun highlightUniqueName() {
        getTextBeforeCursorUntilLastSpaceOrNewLine()?.let { (st, en, word) ->
            val wordSpans = text?.getSpans(
                st,
                en,
                ForegroundColorSpan::class.java
            ) ?: emptyArray()

            val uniqueNameHighlightable = isUniqueNameHighlightable(word)
            // раскомментировать когда нужна подсветка хэштегов
             val hashtagHighlightable = isHashtagHighlightable(word)
             if (uniqueNameHighlightable || hashtagHighlightable) {
//            if (uniqueNameHighlightable) {
                if (wordSpans.isNotEmpty()) {
                    wordSpans
                        .filter { it.foregroundColor == _highlightColor }
                        .forEach { text?.removeSpan(it) }
                }

                text?.setSpan(
                    ForegroundColorSpan(_highlightColor),
                    st,
                    en,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                wordSpans
                    .filter { it.foregroundColor == _highlightColor }
                    .forEach { text?.removeSpan(it) }
            }

            Timber.e("$word has ${wordSpans.size} spans: ${wordSpans.toList().joinToString()}")
        }
    }

    private fun getUniqueNameBeforePointer(): String? {
        return getTagBeforePointer('@')
            ?.trimStart()
            ?.takeIf { _checkUniqueNameStrategy.validate(it) }
    }

    private fun getHashtagBeforePointer(): String? {
        // если символ # не первый символ в строке,
        // то валидный хэштег начинается с пробела
        // либо с новой строки
        val textBeforeCursor = getTextBeforeCursorOrNull() ?: return null
        val lastTagSymbolIndex = textBeforeCursor.indexOfLast { it == '#' }
        if (lastTagSymbolIndex > 0) {
            val charBeforeTagSymbol = textBeforeCursor.get(lastTagSymbolIndex - 1)
            val isNotSpaceOrNewLine = charBeforeTagSymbol == ' ' || charBeforeTagSymbol == '\n'
            if (!isNotSpaceOrNewLine) {
                return null
            }
        }

        return getTagBeforePointer('#')
            ?.trimStart()
            ?.takeIf { it.length in 1..100 }
            ?.takeIf { it.matches(_regexContainer.hashtag) }
    }

    fun clearResources() {
        clearFocus()
        setText("")

        tagHighlighter?.let {
            removeTextChangedListener(it)
            tagHighlighter = null
        }

        textChangingListener?.let {
            removeTextChangedListener(it)
            textChangingListener = null
        }

        _findTagJob?.cancel()
        _highlightAllUniqueNamesJob?.cancel()
        _highlightJob?.cancel()
        suggestionMenu?.clearResources()
        suggestionMenu?.dismiss()
        _highlightAllUniqueNamesJob = null
        _findTagJob = null
        _highlightJob = null
        suggestionMenu = null
    }
}
