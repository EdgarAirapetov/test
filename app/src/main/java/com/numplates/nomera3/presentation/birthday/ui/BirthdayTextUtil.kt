package com.numplates.nomera3.presentation.birthday.ui

import android.content.Context
import com.numplates.nomera3.R
import timber.log.Timber
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject

interface BirthdayTextUtil {
    fun getBirthdayTextListRanges(
        dateOfBirth: Long? = null,
        birthdayText: String,
    ): List<IntRange>
}

/**
 * Данный Класс распознаёт введенный текст.
 * Основная задача: Распознать введенный текст слов поздравлений, которая возможно имеет слова
 * поздравления с Днем Рождения.
 */
class BirthdayTextUtilImpl @Inject constructor(
    appContext: Context
) : BirthdayTextUtil {

    private val birthdayKeyWords: Array<String>
    private val symbolRegexPattern = Pattern.compile("[^\\p{L}\\d\\s]")

    init {
        birthdayKeyWords = appContext.resources.getStringArray(R.array.birthday_key_words)
    }

    override fun getBirthdayTextListRanges(
        dateOfBirth: Long?,
        birthdayText: String,
    ): List<IntRange> {
        val listResult = mutableListOf<IntRange>()
        if (birthdayText.isNotEmpty()) {
            birthdayKeyWords.forEach { birthdayWord ->
                listResult.addAll(
                    getBirthdayRanges(
                        inputText = birthdayText,
                        birthdayWord = birthdayWord
                    )
                )
            }
        }
        return listResult
    }

    /**
     * Проверяем, что есть ли в string ключевые слова
     * Так же проверяем валидацию эмоджи, знаков.
     */
    private fun getBirthdayRanges(
        inputText: String,
        birthdayWord: String
    ): List<IntRange> {
        val result = mutableListOf<IntRange>()
        var index = 0
        while (
            inputText.indexOf(
                string = birthdayWord,
                startIndex = index,
                ignoreCase = true,
            ).also { index = it } != -1
        ) {
            val endIndex = index + birthdayWord.length
            val substringText = inputText.substring(index, endIndex)
            val substringWithDelta = try {
                inputText.substring(index, endIndex + 1)
            } catch (e: StringIndexOutOfBoundsException) {
                Timber.e(e)
                substringText
            }
            if (isContainsSpecialSymbol(substringText) ||
                (isContainsPhrase(source = substringText, subItem = birthdayWord) &&
                    isContainsPhrase(source = substringWithDelta, subItem = birthdayWord))
            ) {
                result.add(IntRange(start = index, endInclusive = endIndex))
            }
            index = endIndex
        }
        return result
    }

    private fun isContainsPhrase(source: String, subItem: String): Boolean {
        val pattern = "\\b$subItem\\b"
        val p: Pattern = Pattern.compile(pattern)
        val m: Matcher = p.matcher(source)
        return m.find()
    }

    private fun isContainsSpecialSymbol(newInputText: String): Boolean {
        return symbolRegexPattern.matcher(newInputText).find()
    }
}
