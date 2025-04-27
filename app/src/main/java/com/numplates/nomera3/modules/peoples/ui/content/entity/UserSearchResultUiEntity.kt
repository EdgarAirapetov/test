package com.numplates.nomera3.modules.peoples.ui.content.entity

import com.meera.db.models.userprofile.VehicleEntity
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentType

data class UserSearchResultUiEntity(
    val uid: Long,
    val name: String?,
    val avatarImage: String?,
    val tagName: String?,
    val additionalInfo: String?,
    val vehicle: VehicleEntity?,
    val gender: Int?,
    val accountType: AccountTypeEnum,
    val accountColor: Int?,
    val friendStatus: Int?,
    val isSubscribed: Boolean,
    val isBlackListedByMe: Boolean,
    val buttonState: ButtonState,
    val approved: Int = 0,
    val topContentMaker: Int = 0,
    val isMyProfile: Boolean = false,
    val hasMoments: Boolean = false,
    val hasNewMoments: Boolean = false
) : PeoplesContentUiEntity {
    enum class ButtonState {
        ShowAdd,
        ShowIncome,
        Hide
    }

    override fun getUserId(): Long = uid

    override fun getPeoplesActionType(): PeoplesContentType = PeoplesContentType.USER_SEARCH_RESULT
}
