package com.numplates.nomera3.modules.maps.ui.pin.model

import android.graphics.Bitmap
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum

data class UserPinUiModel(
    val id: Long,
    val name: String?,
    val accountType: AccountTypeEnum,
    val accountColor: Int?,
    val isFriend: Boolean,
    val avatarBitmap: Bitmap?,
    val moments: PinMomentsUiModel
)
