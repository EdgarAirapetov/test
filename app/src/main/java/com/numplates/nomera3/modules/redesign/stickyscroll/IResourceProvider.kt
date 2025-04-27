package com.numplates.nomera3.modules.redesign.stickyscroll

import androidx.annotation.StyleableRes

internal interface IResourceProvider {
    fun getResourcesByIds(@StyleableRes vararg styleResId: Int): Array<Int>
}
