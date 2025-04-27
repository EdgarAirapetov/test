package com.numplates.nomera3.presentation.view.utils.apphints

import com.numplates.nomera3.R

enum class Tooltip : TooltipSettings {
    ACCOUNT_BUTTON {
        override val textResId: Int
            get() = R.string.tooltip_account_new_feature_text

        override val duration: Long
            get() = TooltipDuration.COMMON_END_DELAY

        override val pointerAlignment: TooltipPointerAlignment
            get() = TooltipPointerAlignment.Bottom

        override val imageDrawableStart: Int
            get() = 0
    },

    RATING_PROFILE {
        override val textResId: Int
        get() = R.string.tooltip_profile_rating_text

        override val duration: Long
        get() = TooltipDuration.COMMON_END_DELAY

        override val pointerAlignment: TooltipPointerAlignment
        get() = TooltipPointerAlignment.None

        override val imageDrawableStart: Int
        get() = R.drawable.badge_rating
    }


}