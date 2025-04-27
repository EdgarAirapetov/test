package com.meera.db.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MutualUserDbModel(
    val userId: Long,
    val name: String,
    val avatar: String,
    val accountType: Int,
    val mutualTotalCount: Int
) : Parcelable
