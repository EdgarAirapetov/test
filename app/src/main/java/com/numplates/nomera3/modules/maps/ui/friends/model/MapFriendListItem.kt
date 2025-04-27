package com.numplates.nomera3.modules.maps.ui.friends.model

import com.google.android.gms.maps.model.LatLng
import com.numplates.nomera3.modules.moments.user.domain.model.UserMomentsModel

sealed interface MapFriendListItem {

    fun isTheSame(other: MapFriendListItem): Boolean
    fun isContentTheSame(other: MapFriendListItem): Boolean

    data class MapFriendUiModel(
        val userId: Long,
        val name: String,
        val uniqueName: String,
        val ageLocation: String,
        val avatarUrl: String,
        val accountType: Int,
        val approved: Int,
        val profileBlocked: Boolean,
        val profileDeleted: Boolean,
        val blacklistedByMe: Boolean,
        val blacklistedMe: Boolean,
        val topContentMaker: Boolean,
        val moments: UserMomentsModel?,
        val location: LatLng,
        val accountColor: Int,
        val birthday: Long,
        val city: String,
        val countryDto: String,
        val friendStatus: Int,
        val subscriptionOn: Int,
        val subscribersCount: Int,
        val gender: Int,
        val iCanChat: Boolean
        ) : MapFriendListItem {
        override fun isTheSame(other: MapFriendListItem): Boolean = other is MapFriendUiModel && other.userId == userId
        override fun isContentTheSame(other: MapFriendListItem): Boolean = other == this
    }

    data class StubItemUiModel(val isInitial: Boolean, val position: Int) : MapFriendListItem {
        override fun isTheSame(other: MapFriendListItem): Boolean = other == this && isInitial
        override fun isContentTheSame(other: MapFriendListItem): Boolean = other == this
    }

    data object FindFriendItemUiModel : MapFriendListItem {
        override fun isTheSame(other: MapFriendListItem): Boolean = other == this
        override fun isContentTheSame(other: MapFriendListItem): Boolean = other == this
    }

    data object EmptyItemUiModel : MapFriendListItem {
        override fun isTheSame(other: MapFriendListItem): Boolean = other == this
        override fun isContentTheSame(other: MapFriendListItem): Boolean = other == this
    }

    data object EmptySearchItemUiModel : MapFriendListItem {
        override fun isTheSame(other: MapFriendListItem): Boolean = other == this
        override fun isContentTheSame(other: MapFriendListItem): Boolean = other == this
    }
}
