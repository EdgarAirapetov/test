package com.numplates.nomera3.modules.holidays.data.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meera.db.models.userprofile.ProductEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class HolidayInfoEntity(
    @SerializedName("id")
    val id: Long?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("main_button_links")
    val mainButtonLinkEntity: MainButtonLinkEntity,
    @SerializedName("start_time")
    val startTime: Long,
    @SerializedName("finish_time")
    val finishTime: Long,
    @SerializedName("onboarding")
    val onBoardingEntity: OnBoardingEntity,
    @SerializedName("cap_links")
    val hatsLink: HatsEntity,
    @SerializedName("room_style")
    val chatRoomEntity: ChatRoomEntity,
    @SerializedName("product")
    val productEntity: ProductEntity?,
    @SerializedName("holiday_code")
    val holidayCode: String? = null
): Parcelable
