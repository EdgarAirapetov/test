package com.meera.core.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.meera.core.extensions.empty
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule
import kotlin.math.min

/**
 * EditText с подсветкой уникальных имен и выдачей уникального имени по коллбеку
 * */
open class EditTextExtended : AppCompatEditText {

    private var _hashtagListener: HashtagListener? = null

    /**
     * Regex для проверки хэштега
     * https://regex101.com/r/wP6yX5/1
     * */
    private var regexValidateHashtag: Regex = "(\\#[а-яА-Яa-zA-Z_0-9\\ud83c\\udf00-\\ud83d\\ude4f\\ud83d\\ude80-\\ud83d\\udeff]*)(?!\\S)".toRegex()

    /**
     * Regex для поиска слов разделенных пробелами
     * */
    private var regexGetAllWords: Regex = "\\S*[a-zA-Z@]+\\S*".toRegex()

    /**
     *
     * */
    private var defaultTextColor: DefaultTextColor = DefaultTextColor.Standard

    /**
     * Коллбэк для выдачи уникального имени после изменения текста
     * */
    private var onNewUniqueNameAfterTextChangedListener: OnNewUniqueNameListener? = null

    /**
     * Коллбэк для выдачи уникального имени после изменения позиции курсора
     * */
    private var onNewUniqueNameAfterSelectionChangedListener: OnNewUniqueNameListener? = null

    /**
     * Коллбэк для скрытия клавиатуры
     * */
    private var onUniqueNameNotFoundListener: OnUniqueNameNotFoundListener? = null

    /**
     * Правило проверки уникального имени
     * */
    private var checkUniqueNameStrategy: CheckUniqueNameStrategy = CheckUniqueNameStrategyAddPost()

    /**
     * Флаг для запрещения выдачи уникального имени после изменения позиции курсора, чтобы не
     * было двойной выдачи одного и того же уникального имени. Потому что после изменения текста
     * двигается курсор, и срабатывает ненужный вызов onNewUniqueNameAfterSelectionChangedListener
     * */
    private var isSelectionListenerLocked = true

    /**
     * Флаг для запрещения выдачи уникального имени после изменения позиции курсора, чтобы не
     * было двойной выдачи одного и того же уникального имени. Потому что после изменения текста
     * двигается курсор, и срабатывает ненужный вызов onNewUniqueNameAfterSelectionChangedListener
     * */
    private var isSelectionListenerFeatureLocked = true

    /**
     * Регулярка для проверки уникального имени
     * https://nomeraworkspace.slack.com/files/UTWAAJW1E/F01PK105J9M/image.png
     * */
    private val uniqueNameRegex by lazy { Regex("^[a-zA-Z0-9._@]+$", RegexOption.IGNORE_CASE) }

    /**
     *
     * */
    private var getUniqueNameTask: TimerTask? = null

    /**
     * (Альтернативый) Основной TextWatcher для отслеживания изменения текста и подсветки. Если
     * пройдет приемку, заменить mainTextWatcher на mainTextWatcherExperimental
     * */
    private val mainTextWatcherExperimental: TextWatcher = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        /**
         * После изменения текста подсвечиваем все уникальные имена
         * */
        override fun afterTextChanged(s: Editable?) {
            getUniqueNameTask?.cancel()

            getUniqueNameTask = Timer("GetUniqueNameTask", false).schedule(300) {
                val uniqueNameBeforePointer = getUniqueNameBeforePointer()
                if (uniqueNameBeforePointer != null) {
                    onNewUniqueNameAfterTextChangedListener?.onNewUniqueName(uniqueNameBeforePointer)
                } else {
                    onUniqueNameNotFoundListener?.onNotFound()
                }

                val hashtagBeforePointer = getHashtagBeforePointer()
                if (hashtagBeforePointer != null) {
                    _hashtagListener?.onFound(hashtagBeforePointer)
                } else {
                    _hashtagListener?.onNotFound()
                }
            }
        }
    }

    private val highlighterTextWatcher: TextWatcher = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(s: Editable?) {
            highlightUniqueNames()
        }
    }


    /**
     * Вспомогательный TextWatcher для активации выдачи уникального имени по изменению позиции курсора
     * */
    private val updateEndTextWatcher: TextWatcher = object : TextWatcher {
        private var unlockSelectionListenerTask: TimerTask? = null

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            isSelectionListenerLocked = true
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        /**
         * Если изменили текст и в нем есть уникальное имя, то будет выдача уникального имени через
         * onNewUniqueNameAfterTextChangedListener, но при изменении текста еще срабатывает метод
         * onSelectionChanged(..) у EditText, в котором есть свой коллбек на выдачу уникального имени.
         * То есть при изменении текста может быть выдача одного и того уникального имени два раза
         * подряд через разные колбеки. Чтобы избежать этого блочим выдачу уникального имени по
         * курсору через флаг isSelectionListenerLocked. Если сразу менять флаг, а не через таймер
         * то происходит двойная выдача
         *
         * Вообще лучше бы как то подругому сделать этот момент - сейчас слишком костыльно, особенно
         * с таймером
         * */
        override fun afterTextChanged(s: Editable?) {
            unlockSelectionListenerTask?.cancel()
            unlockSelectionListenerTask = Timer("UnlockSelectionListener", false).schedule(400) {
                isSelectionListenerLocked = false
            }
        }
    }


    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize()
    }

    /**
     *
     * */
    public fun setHashtagListener(listener: HashtagListener) {
        _hashtagListener = listener
    }

    /**
     * Коллбек для скрытия клавиатуры
     * */
    public fun setOnUniqueNameNotFoundListener(newListener: OnUniqueNameNotFoundListener) {
        onUniqueNameNotFoundListener = newListener
    }

    /**
     * Коллбек для выдачи уникального имени после изменения текста
     * */
    public fun setOnNewUniqueNameAfterTextChangedListener(newListener: OnNewUniqueNameListener) {
        onNewUniqueNameAfterTextChangedListener = newListener
    }

    /**
     * Коллбек для выдачи уникального имени после изменения позиция курсора
     * */
    public fun setOnNewUniqueNameAfterSelectionChangedListener(newListener: OnNewUniqueNameListener) {
        onNewUniqueNameAfterSelectionChangedListener = newListener
    }

    /**
     *
     * */
    public fun setDefaultTextColor(newDefaultTextColor: DefaultTextColor) {
        defaultTextColor = newDefaultTextColor
    }

    /**
     *
     * */
    public fun replaceUniqueNameBySuggestion(oldUniqueName: String, suggestion: String) {
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

    /**
     * Установка правила проверки по которому будет выдача уник имени
     * */
    public fun setCheckUniqueNameStrategy(newStrategy: CheckUniqueNameStrategy) {
        checkUniqueNameStrategy = newStrategy
    }

    /**
     * Используется для выдачи уникального имени после изменения позиция курсора
     * */
    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)

        if (!isSelectionListenerLocked && !isSelectionListenerFeatureLocked) {
            val uniqueName = getUniqueNameUnderPointer(text?.toString())
            onNewUniqueNameAfterSelectionChangedListener?.onNewUniqueName(uniqueName)
        }
    }

    /**
     * Инициализация вызывается в конструкторах
     * */
    @SuppressLint("ClickableViewAccessibility")
    private fun initialize() {
        addTextChangedListener(mainTextWatcherExperimental)
        addTextChangedListener(highlighterTextWatcher)
        addTextChangedListener(updateEndTextWatcher)
    }

    /**
     * Подсветить уникальные имена после изменения текста
     * */
    private fun highlightUsingRegex() {
        text?.let { regexGetAllWords.findAll(it) }?.filter { it.value.isNotEmpty() }?.forEach { matchResult ->
            val charSequence = matchResult.value
            if (isHighlightableUniqueName(charSequence)) {
                text?.setSpan(
                    ForegroundColorSpan(Color.parseColor("#6a48d9")),
                    matchResult.range.first,
                    matchResult.range.last + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                text?.setSpan(
                    ForegroundColorSpan(Color.parseColor(defaultTextColor.color)),
                    matchResult.range.first,
                    matchResult.range.last + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    private fun highlightUniqueNames() {
        highlightUsingRegex()
    }

    @Synchronized
    private fun getTextBeforeCursorOrNull(): CharSequence? {
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
            e.printStackTrace()
            null
        }
    }

    /**
     * Взять уникальное имя слева от курсора
     * */
    private fun getUniqueNameBeforePointer(): String? {
        val textBeforeCursor = getTextBeforeCursorOrNull() ?: return null
        val specialCharLastIndex = textBeforeCursor.indexOfLast { it == '@' }
        if (specialCharLastIndex >= 0 && specialCharLastIndex < textBeforeCursor.length) {
            return textBeforeCursor
                .substring(specialCharLastIndex, textBeforeCursor.length)
                .trimStart()
                .takeIf { checkUniqueNameStrategy.checkForSearch(it) }

        } else {
            return null
        }
    }

    /**
     * Взять хэштег от курсора
     * */
    private fun getHashtagBeforePointer(): String? {
        val textBeforeCursor = getTextBeforeCursorOrNull() ?: return null
        val specialCharLastIndex = textBeforeCursor.indexOfLast { it == '#' }
        if (specialCharLastIndex >= 0 && specialCharLastIndex < textBeforeCursor.length) {
            val hashtag = textBeforeCursor
                .substring(specialCharLastIndex, textBeforeCursor.length)
                .takeIf { it.length <= 100 }
                ?.trimStart()
                ?.takeIf { validateHashtag(it) }

            return hashtag

        } else {
            return null
        }
    }

    private fun validateHashtag(hashtag: String): Boolean {
        return hashtag.matches(regexValidateHashtag)
    }

    /**
     * Взять уникальное имя на котором стоит курсор
     * */
    private fun getUniqueNameUnderPointer(newText: String?): String {
        if (newText != null) {
            val currentSelectionEnd = selectionEnd.takeIf { it >= 0 } ?: 0

            val startIndex = newText
                .subSequence(0, currentSelectionEnd)
                .indexOfLast { it == ' ' }
                .takeIf { it >= 0 }
                ?: 0

            val endIndex = newText
                .subSequence(currentSelectionEnd, newText.length)
                .indexOfFirst { it == ' ' }
                .takeIf { it >= 0 }
                ?: newText.length

            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                return newText
                    .subSequence(startIndex, endIndex)
                    .toString()
                    .trimStart()
                    .takeIf { it.startsWith("@", true) }
                    ?.takeIf { isHighlightableUniqueName(it) }
                    ?: String.empty()
            } else {
                return String.empty()
            }
        } else {
            return String.empty()
        }
    }

    /**
     *
     * */
    private fun isHighlightableUniqueName(charSequence: CharSequence?): Boolean {
        return charSequence
            ?.toString()
            ?.let { text: String ->
                text.matches(uniqueNameRegex)
                    && text.length >= 4
                    && text.length <= 26
                    && !text.contains("..")
                    && !text.endsWith(".")
                    && text.filter { it == '@' }.length == 1
                    && text.trim().startsWith('@')
            }
            ?: false
    }

    /**
     * Коллбек для выдачи уникального имени
     * */
    public interface OnNewUniqueNameListener {

        public fun onNewUniqueName(uniqueName: String)

    }

    /**
     * Коллбек срабатывающий если не осталось уникальных имен в тексте
     * */
    public interface OnUniqueNameNotFoundListener {

        public fun onNotFound()

    }

    /**
     * Виды проверки уникального имени для выдачи уник имени по коллбеку. Добавлено т.к. при создании
     * поста и при добавлении комментария, условия выдачи уник имени отличаются. Например, при создании
     * поста выдача уник имени должна быть после ввода 3х символов, а при комментировании уже после
     * ввода хотя бы одного символа
     * */
    interface CheckUniqueNameStrategy {

        fun checkForSearch(charSequence: CharSequence?): Boolean

    }

    /**
     * Проверка для уник имени при добавлении комментария
     * */
    public class CheckUniqueNameStrategyAddComment : BaseCheckUniqueNameStrategy() {
        override fun checkForSearch(charSequence: CharSequence?): Boolean {
            return charSequence
                ?.toString()
                ?.let { text: String ->
                    text.matches(uniqueNameRegex)
                        && text.length >= 2 // @ + 1 символ
                        && text.length <= 26
                        && !text.contains("..")
                        && !text.endsWith(".")
                        && text.filter { it == '@' }.length == 1
                        && text.trim().startsWith('@')
                }
                ?: false
        }
    }

    /**
     * Проверка для уник имени при добавлении поста
     * */
    public class CheckUniqueNameStrategyAddPost : BaseCheckUniqueNameStrategy() {
        override fun checkForSearch(charSequence: CharSequence?): Boolean {
            return charSequence
                ?.toString()
                ?.let { text: String ->
                    text.matches(uniqueNameRegex)
                        && text.length >= 2 // @ + 1 символ
                        && text.length <= 26
                        && !text.contains("..")
                        && !text.endsWith(".")
                        && text.filter { it == '@' }.length == 1
                        && text.trim().startsWith('@')
                }
                ?: false
        }
    }

    /**
     * Проверка для уник имени для группового чата
     * */
    public class CheckUniqueNameStrategyGroupChat : BaseCheckUniqueNameStrategy() {
        override fun checkForSearch(charSequence: CharSequence?): Boolean {
            return charSequence
                ?.toString()
                ?.let { text: String ->
                    text.matches(uniqueNameRegex)
                        && text.length >= 1 // @ + 1 символ
                        && text.length <= 26
                        && !text.contains("..")
                        && !text.endsWith(".")
                        && text.filter { it == '@' }.length == 1
                        && text.trim().startsWith('@')
                }
                ?: false
        }
    }

    /**
     * Базовый класс для проверок уник имени
     * */
    abstract class BaseCheckUniqueNameStrategy : CheckUniqueNameStrategy {

        protected val uniqueNameRegex by lazy { Regex("^[a-zA-Z0-9._@]+$", RegexOption.IGNORE_CASE) }

    }

    /**
     *
     * */
    enum class DefaultTextColor(val color: String) {
        Standard("#000000"),
        MediaViewerPreviewChat("#B3FFFFFF")
    }
}

interface HashtagListener {

    fun onFound(hashtag: String)

    fun onNotFound()

}

