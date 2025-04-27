package com.numplates.nomera3.presentation.view.utils

import android.content.Context
import android.text.SpannableStringBuilder
import com.meera.core.extensions.addClickWithDataBold
import com.meera.core.extensions.empty
import com.meera.core.extensions.pluralString
import com.numplates.nomera3.R
import javax.inject.Inject

private const val MAX_TEXT_LENGTH = 30
private const val TWO_FRIENDS = 2

interface MutualUsersTextUtil {
    fun getMutualTextSpan(
        fullUsersName: List<String>,
        moreCount: Int
    ) : SpannableStringBuilder

    fun getMutualText(
        fullUsersName: List<String>,
        moreCount: Int
    ) : String
}

class MutualUsersTextUtilImpl @Inject constructor(
    private val context: Context
) : MutualUsersTextUtil {

    private val nameList = mutableListOf<String>()
    private var moreCount = 0

    override fun getMutualTextSpan(
        fullUsersName: List<String>,
        moreCount: Int
    ): SpannableStringBuilder {
        clear()
        addMutualUsersName(
            fullUsersName = fullUsersName,
            moreCount = moreCount
        )
        return SpannableStringBuilder(getTextResult()).addClickWithDataBold(
            data = Unit,
            range = getTextRange(),
            onClickListener = {}
        )
    }

    override fun getMutualText(
        fullUsersName: List<String>,
        moreCount: Int
    ): String {
        clear()
        addMutualUsersName(
            fullUsersName = fullUsersName,
            moreCount = moreCount
        )
        return getTextResult()
    }

    private fun clear() {
        nameList.clear()
        moreCount = 0
    }

    private fun addMutualUsersName(
        fullUsersName: List<String>,
        moreCount: Int
    ) {
        this.moreCount = moreCount
        this.nameList.addAll(fullUsersName)
    }

    private fun getTextResult(): String {
        return when {
            !isUserNameLong() -> getAllUsersNameTextResult()
            else -> "${getFirstUserName()} ${getTypePluralString(getTotalCountSkipOne())}"
        }
    }

    private fun getPluralString(count: Int) =
        context.pluralString(R.plurals.user_subscribe_plural, count)

    /**
     * Получаем первое имя из списка.
     * Необходимо в том случае, если длина текста более 30 символов.
     * И результат должен выглядеть так: Константин Коннстантинопольски и ещё 473 подписаны
     */
    private fun getFirstUserName(): String = nameList.firstOrNull() ?: String.empty()

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
