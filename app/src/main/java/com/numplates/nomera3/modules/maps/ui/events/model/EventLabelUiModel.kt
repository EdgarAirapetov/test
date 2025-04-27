package com.numplates.nomera3.modules.maps.ui.events.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentPostModel

data class EventLabelUiModel(
    @ColorRes
    val textColorResId: Int,
    val textSizeSp: Int,
    @DrawableRes
    val imgResId: Int,
    @StringRes
    val titleResId: Int,
    val date: String,
    val day: String,
    val time: String,
    val distanceAddress: DistanceAddressUiModel?,
    val attachment: UIAttachmentPostModel? = null,
)
