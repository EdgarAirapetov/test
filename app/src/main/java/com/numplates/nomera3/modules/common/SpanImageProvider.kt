package com.numplates.nomera3.modules.common

import android.graphics.drawable.Drawable

interface SpanImageProvider {

    fun provideByChar(char: CharSequence): Drawable?
}
