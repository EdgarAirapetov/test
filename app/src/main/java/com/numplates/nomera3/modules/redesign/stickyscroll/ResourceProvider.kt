package com.numplates.nomera3.modules.redesign.stickyscroll

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StyleableRes
import androidx.core.content.res.use

internal class ResourceProvider(private val context: Context, private val attrs: AttributeSet?, @StyleableRes private val styleRes: IntArray) :
    IResourceProvider {
    override fun getResourcesByIds(@StyleableRes vararg styleResIds: Int): Array<Int> {
        context.obtainStyledAttributes(attrs, styleRes).use { typedArray ->
            return mutableListOf<Int>().apply {
                styleResIds.forEach { styleResId -> add(typedArray.getResourceId(styleResId, 0)) }
            }.toTypedArray()
        }
    }
}
