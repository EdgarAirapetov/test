package com.numplates.nomera3.presentation.view.fragments.meerasettings

import android.content.Context
import com.meera.core.extensions.pluralString
import com.numplates.nomera3.R

import javax.inject.Inject

private const val TEXT_INPUT_LENGTH_RESTRICTION = 240

class RateUsUiMapper @Inject constructor(
    val context: Context
) {
    fun mapRateUsDialogState(
        text: String = "",
        isRatingSent: Boolean = false,
        isCancel: Boolean = false
    ): RateUsDialogState {
        return when {
            isRatingSent -> RateUsDialogState(
                description = text,
                isRatingSent = isRatingSent
            )
            isCancel -> RateUsDialogState(
                description = text,
                isCancel = true
            )
            else -> RateUsDialogState(
                description = context.pluralString(
                    R.plurals.meera_rate_us_char_reminder,
                    TEXT_INPUT_LENGTH_RESTRICTION - text.length
                )
            )
        }
    }
}
