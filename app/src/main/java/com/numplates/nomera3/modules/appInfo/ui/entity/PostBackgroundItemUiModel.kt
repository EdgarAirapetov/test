package com.numplates.nomera3.modules.appInfo.ui.entity

import android.os.Parcelable
import com.meera.core.extensions.empty
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostBackgroundItemUiModel (
    val id: Int = DEFAULT_ID,
    val url: String = String.empty(),
    var previewUrl: String = String.empty(),
    var fontColor: String = String.empty()
): Parcelable {
    var isSelected: Boolean = false

    fun setSelected(isSelected: Boolean): PostBackgroundItemUiModel {
        this.isSelected = isSelected
        return this
    }

    fun isWhiteFont(): Boolean = fontColor == "FFFFFF"

    companion object {
        const val DEFAULT_ID = -1
    }
}
