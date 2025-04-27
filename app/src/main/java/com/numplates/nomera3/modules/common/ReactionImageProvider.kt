package com.numplates.nomera3.modules.common

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.numplates.nomera3.modules.reaction.data.ReactionType.Companion.getDrawableFromCharacter
import com.numplates.nomera3.modules.reaction.data.ReactionType.Companion.getDrawableFromUnsupportedCharacter
import javax.inject.Inject

class ReactionImageProvider @Inject constructor(
    private val context: Context
) : SpanImageProvider {

    override fun provideByChar(char: CharSequence): Drawable? {
        val resId = getDrawableFromCharacter(char.toString()).takeIf { it != 0 }
            ?: getDrawableFromUnsupportedCharacter(char.toString())
        return ResourcesCompat.getDrawable(context.resources, resId, null)
    }
}
