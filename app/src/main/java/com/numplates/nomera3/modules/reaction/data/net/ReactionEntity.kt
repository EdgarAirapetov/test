package com.numplates.nomera3.modules.reaction.data.net

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReactionEntity(
    @SerializedName("count")
    val count: Int,
    @SerializedName("is_mine")
    val isMine: Int,
    @SerializedName("reaction")
    val reactionType: String
) : Parcelable {
    override fun toString(): String {
        return "type : $reactionType, isMine : $isMine, count : $count"
    }
}

fun ReactionEntity.isMine(): Boolean {
    return isMine == 1
}
