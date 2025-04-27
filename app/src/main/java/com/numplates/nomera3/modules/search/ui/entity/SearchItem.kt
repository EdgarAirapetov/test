package com.numplates.nomera3.modules.search.ui.entity

import androidx.annotation.StringRes
import com.meera.db.models.userprofile.VehicleEntity
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum

sealed class SearchItem {
    data class User(
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
        val isMyProfile:Boolean = false,
        val hasMoments: Boolean,
        val hasNewMoments: Boolean
    ) : SearchItem() {
        enum class ButtonState {
            ShowAdd,
            ShowIncome,
            Hide
        }
    }

    data class Group(
        val groupId: Int,
        val name: String,
        val image: String?,
        val participantCount: Int,
        val isClosedGroup: Boolean = true,
        val buttonState: ButtonState
    ) : SearchItem() {
        enum class ButtonState {
            Show,
            Hide
        }
    }

    data class HashTag(
        val name: String,
        val count: Int?
    ) : SearchItem()

    data object GroupShimmer : SearchItem()

    data object HashtagShimmer : SearchItem()

    data class Title(
        @StringRes val titleResource: Int,
        @StringRes val buttonLabelResource: Int? = null
    ) : SearchItem()

    data class RecentBlock(
        val items: List<RecentBaseItem>
    ) : SearchItem() {
        sealed class RecentBaseItem {
            data class RecentUser(
                val uid: Long,
                val image: String?,
                val name: String?,
                val gender: Int,
                val accountType: AccountTypeEnum,
                val accountColor: Int,
                val approved: Boolean,
                val topContentMaker: Boolean,
                val hasMoments: Boolean,
                val hasNewMoments: Boolean
            ) : RecentBaseItem()

            data class RecentGroup(
                val groupId: Int,
                val image: String?,
                val name: String?
            ) : RecentBaseItem()
        }
    }
}
