package com.numplates.nomera3.presentation.view.utils

import android.content.Context
import android.text.SpannableStringBuilder
import com.meera.core.extensions.addClickWithDataBold
import com.meera.core.extensions.empty
import com.meera.core.extensions.pluralString
import com.numplates.nomera3.R
import timber.log.Timber

private const val MAX_TEXT_LENGTH = 30
private const val TWO_FRIENDS = 2

/**
 * Утилита, которая будет хэндлить текст кол-во общих подписок
 */
@Deprecated("Используйте MutualUsersTextUtil")
class MutualFriendsTextUtils constructor(
    private val context: Context,
) {
    private val nameList = mutableListOf<String>()
    var moreCount = 0

    fun addUserName(text: String) {
        nameList.add(text)
    }

    fun getTextResult(): SpannableStringBuilder {
        val text = when {
            !isUserNameLong() -> getAllUsersNameTextResult()
            else -> "${getFirstUserName()} ${getTypePluralString(getTotalCountSkipOne())}"
        }
        return SpannableStringBuilder(text).addClickWithDataBold(
            data = Unit,
            range = getTextRange(),
            onClickListener = {}
        )
    }

    private fun getPluralString(count: Int) =
        context.pluralString(R.plurals.user_subscribe_plural, count)

    /**
     * Получаем первое имя из списка.
     * Необходимо в том случае, если длина текста более 30 символов.
     * И результат должен выглядеть так: Константин Коннстантинопольски и ещё 473 подписаны
     */
    private fun getFirstUserName(): String {
        return try {
            nameList[0]
        } catch (e: Exception) {
            Timber.e(e)
            String.empty()
        }
    }

    private fun getTextRange(): IntRange {
        return if (!isUserNameLong()) {
            IntRange(0, getCurrentMutualFriendsStr().length)
        } else {
            IntRange(0, getFirstUserName().length)
        }
    }

    private fun getTypePluralString(count: Int): String {
        return if (count > 0) {
            context.getString(R.string.they_subscribed, count)
        } else {
            getPluralString(getFriendsCount())
        }
    }

    /**
     * Получаем имена юзеров, когда длина строки менее 30 символов.
     */
    private fun getFullUserNamesStr() = nameList.joinToString()

    /**
     * Получаем имена юзеров, когда длина строки менее 30 символов.
     * Но когда юзер имеет 2 общие подписки
     */
    private fun getFullUserNameWithAndSeparator() =
        nameList.joinToString(
            separator = context.getString(R.string.and_with_space),
        )

    /**
     * Получаем результат текста, когда длина имен пользователей менее 30.
     * Если у юзера 2 общих друзей, то нужно показать текст таким образом "Alex и James"
     * Иначе метод вернет имена через запятую.
     */
    private fun getAllUsersNameTextResult(): String {
        return if (getFriendsCount() == TWO_FRIENDS) {
            "${getFullUserNameWithAndSeparator()} ${getPluralString(1)}"
        } else {
            "${getFullUserNamesStr()} ${getTypePluralString(moreCount)}"
        }
    }

    /**
     * Через данный метод мы будем проверять длину строки, а так же
     * устанавливать конец строки Spannable
     */
    private fun getCurrentMutualFriendsStr(): String {
        return if (getFriendsCount() == TWO_FRIENDS) {
            getFullUserNameWithAndSeparator()
        } else {
            getFullUserNamesStr()
        }
    }

    /**
     * Получаем общее кол-во подписок, когда имя юзера длинное.
     * Минусуем одну подписку т.к первое имя юзера должно отображаться
     */
    private fun getTotalCountSkipOne() =
        (getFriendsCount() + moreCount) - 1

    private fun isUserNameLong() = getCurrentMutualFriendsStr().length >= MAX_TEXT_LENGTH

    private fun getFriendsCount() = nameList.size
}
