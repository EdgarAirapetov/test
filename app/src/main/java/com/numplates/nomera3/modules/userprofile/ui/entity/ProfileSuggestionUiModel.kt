package com.numplates.nomera3.modules.userprofile.ui.entity

import android.os.Parcelable
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import kotlinx.parcelize.Parcelize

@Parcelize
sealed interface ProfileSuggestionUiModels : Parcelable {

    fun getUserId(): Long?

    @Parcelize
    data class ProfileSuggestionUiModel(
        val userId: Long,
        val avatarLink: String,
        val name: String,
        val uniqueName: String,
        val cityName: String,
        val isApproved: Boolean,
        val isTopContentMaker: Boolean,
        val accountType: AccountTypeEnum,
        val mutualFriendsCount: Int,
        var isSubscribed: Boolean = false,
        var isVip: Boolean = false,
        val gender: Int?
    ) : ProfileSuggestionUiModels {
        val hasMutualFriends: Boolean
            get() = mutualFriendsCount > 0

        override fun getUserId(): Long {
            return userId
        }
    }

    @Parcelize
    data class SuggestionSyncContactUiModel(
        var isUserVip: Boolean = false,
    ) : ProfileSuggestionUiModels {

        override fun getUserId(): Long? {
            return null
        }
    }
}
