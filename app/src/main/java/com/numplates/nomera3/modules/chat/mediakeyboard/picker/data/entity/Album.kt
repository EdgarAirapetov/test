package com.numplates.nomera3.modules.chat.mediakeyboard.picker.data.entity

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Album(
    val id: String?,
    val name: String,
    val lastImageUri: Uri,
    var imagesCount: Int = 0,
    var chosen: Boolean = false
) : Parcelable