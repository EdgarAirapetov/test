package com.meera.db.models

import android.os.Parcelable
import com.meera.db.models.message.MessageEntity
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class DraftUiModel(
    val roomId: Long?,
    val userId: Long?,
    val lastUpdatedTimestamp: Long,
    val text: String?,
    val reply: @RawValue MessageEntity?,
    val draftId: Int?
) : Parcelable
