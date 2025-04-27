package com.numplates.nomera3.modules.common

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.numplates.nomera3.R
import javax.inject.Inject

class ObsceneWordImageProvider @Inject constructor(
    private val context: Context,
) : SpanImageProvider {

    override fun provideByChar(char: CharSequence): Drawable? {
        return ResourcesCompat.getDrawable(context.resources, getResourceByChar(char), null)
    }

    private fun getResourceByChar(character: CharSequence): Int {
        return when (character) {
            "Ǟ" -> R.drawable.text_icon_1
            "Ǡ" -> R.drawable.text_icon_2
            "Ǣ" -> R.drawable.text_icon_3
            "ſ" -> R.drawable.text_icon_4
            "Ǥ" -> R.drawable.text_icon_5
            "Ǩ" -> R.drawable.text_icon_6
            "Ǹ" -> R.drawable.text_icon_7
            "Ǭ" -> R.drawable.text_icon_8
            "Ǯ" -> R.drawable.text_icon_9
            "Ǳ" -> R.drawable.text_icon_10
            "Ǻ" -> R.drawable.text_icon_11
            "Ǽ" -> R.drawable.text_icon_12
            "Ǿ" -> R.drawable.text_icon_13
            "Ȁ" -> R.drawable.text_icon_14
            "Ȃ" -> R.drawable.text_icon_15
            "Ȅ" -> R.drawable.text_icon_16
            "Ȇ" -> R.drawable.text_icon_17
            "Ȉ" -> R.drawable.text_icon_18
            "Ȋ" -> R.drawable.text_icon_19
            "Ȍ" -> R.drawable.text_icon_20
            "Ȏ" -> R.drawable.text_icon_21
            "Ȑ" -> R.drawable.text_icon_22
            "Ȓ" -> R.drawable.text_icon_23
            "Ȕ" -> R.drawable.text_icon_24
            "Ȗ" -> R.drawable.text_icon_25
            "Ȝ" -> R.drawable.text_icon_26
            "Ȟ" -> R.drawable.text_icon_27
            "Ȥ" -> R.drawable.text_icon_28
            "Ȧ" -> R.drawable.text_icon_29
            "Ȩ" -> R.drawable.text_icon_30
            else -> 0
        }
    }
}
