package com.numplates.nomera3.modules.peoples.ui.content.entity

import android.os.Parcelable
import androidx.annotation.DrawableRes
import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentType
import kotlinx.parcelize.Parcelize

@Parcelize
data class HeaderUiEntity(
    val text: String,
    val textSize: Int,
    @DrawableRes val textDrawable: Int? = null
) : Parcelable, PeoplesContentUiEntity {
    override fun getUserId(): Long? = null

    override fun getPeoplesActionType(): PeoplesContentType {
        return PeoplesContentType.HEADER_TYPE
    }
}
