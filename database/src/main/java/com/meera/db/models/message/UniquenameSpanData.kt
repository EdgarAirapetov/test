package com.meera.db.models.message

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UniquenameSpanData(

    val id: String?,

    val tag: String?,

    val type: String?,

    val startSpanPos: Int?,

    val endSpanPos: Int?,

    val userId: Long?,

    val groupId: Long? = 0,

    val symbol: String?,

    val url: String? = null

) : Parcelable
