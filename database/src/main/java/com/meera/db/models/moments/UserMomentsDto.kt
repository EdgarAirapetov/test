package com.meera.db.models.moments

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserMomentsDto(
    @SerializedName("has_moments") val hasMoments: Int?,
    @SerializedName("has_new_moments") val hasNewMoments: Int?,
    @SerializedName("count_new") val countNew: Int?,
    @SerializedName("count_total") val countTotal: Int?,
    @SerializedName("previews") val previews: List<UserMomentsPreviewDto>?
) : Parcelable {
    companion object {
        fun emptyMoments(): UserMomentsDto {
            return UserMomentsDto(
                hasMoments = 0,
                hasNewMoments = 0,
                countNew = 0,
                countTotal = 0,
                previews = emptyList()
            )
        }
    }
}
