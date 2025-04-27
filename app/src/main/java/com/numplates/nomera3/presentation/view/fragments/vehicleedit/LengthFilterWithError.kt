package com.numplates.nomera3.presentation.view.fragments.vehicleedit

import android.text.InputFilter
import android.text.Spanned


class LengthFilterWithError(
    private val maxLength: Int,
    private val onLimitExceeded: () -> Unit,
) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val currentLength = dest?.length ?: 0
        val newLength = currentLength + (end - start)

        return if (newLength > maxLength) {
            onLimitExceeded()
            ""
        } else {
            null
        }
    }
}
