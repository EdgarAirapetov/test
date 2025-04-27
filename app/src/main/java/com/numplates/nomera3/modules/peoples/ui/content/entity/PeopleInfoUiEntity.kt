package com.numplates.nomera3.modules.peoples.ui.content.entity

import android.os.Parcelable
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentType
import kotlinx.parcelize.Parcelize

@Parcelize
data class PeopleInfoUiEntity(
    val userId: Long,
    val userName: String,
    val userSubscribers: Int,
    val accountType: AccountTypeEnum,
    val isApprovedAccount: Boolean,
    val isInterestingAuthor: Boolean,
    val imageUrl: String,
    val isUserSubscribed: Boolean,
    val isUserSubscribedToMe: Boolean,
    val hasMoreThanThousandSubscribers: Boolean,
    val isMe: Boolean,
    val uniqueName: String,
    val hasMoments: Boolean,
    val hasNewMoments: Boolean
) : Parcelable, PeoplesContentUiEntity {
    override fun getUserId(): Long = userId

    override fun getPeoplesActionType(): PeoplesContentType {
        return PeoplesContentType.PEOPLE_INFO_TYPE
    }

}
