package com.numplates.nomera3.presentation.view.utils.apphints

import androidx.annotation.StringRes
import com.numplates.nomera3.R

data class ListHints(
        val hints: MutableList<Hint>
)



data class Hint(

        val id: Int,

        val type: HintTypes,

        @StringRes
        val text: Int = R.string.app_hint_empty,

        /**
         * Layout require should be ConstraintLayout
         */
        val layout: Int = R.layout.app_hint_group_chat,

        val visibleTimeSec: Long = 4,

        var isShown: Boolean = false,

        var isShowOneTime: Boolean = true
)