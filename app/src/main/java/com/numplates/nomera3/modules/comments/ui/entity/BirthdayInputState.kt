package com.numplates.nomera3.modules.comments.ui.entity

import com.meera.db.models.userprofile.UserSimple

/**
 * Хранит состояние для фрагмента, когда юзер печатает текст
 * [com.numplates.nomera3.modules.comments.ui.fragment.PostFragmentV2]
 * Нужно для того, чтобы понять ввел ли юзер праздничные слова
 */
data class BirthdayInputState(
    val wordsRanges: List<IntRange> = listOf(),
    val userSimple: UserSimple? = null
)
