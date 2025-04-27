package com.numplates.nomera3.modules.bump.ui.mapper

import androidx.annotation.StringRes
import com.meera.core.extensions.toBoolean
import com.meera.core.utils.IS_APP_REDESIGNED
import com.numplates.nomera3.FRIEND_STATUS_CONFIRMED
import com.numplates.nomera3.FRIEND_STATUS_INCOMING
import com.numplates.nomera3.FRIEND_STATUS_OUTGOING
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.ResourceManager
import com.numplates.nomera3.modules.bump.domain.entity.ShakeMutualUsersModel
import com.numplates.nomera3.modules.bump.domain.entity.UserShakeModel
import com.numplates.nomera3.modules.bump.ui.entity.UserFriendShakeStatus
import com.numplates.nomera3.modules.bump.ui.entity.UserShakeUiModel
import com.numplates.nomera3.modules.bump.ui.entity.UserShakeUiState
import com.numplates.nomera3.presentation.model.MutualUserUiModel
import com.numplates.nomera3.presentation.model.MutualUsersUiModel
import com.numplates.nomera3.presentation.view.utils.MutualUsersTextUtil
import javax.inject.Inject

private const val MIN_DOTS_COUNT = 1
private const val DEFAULT_DOT_SELECTED_POSITION = 0
private const val NEXT_POSITION = 1

class ShakeUiMapper @Inject constructor(
    private val resourceManager: ResourceManager,
    private val mutualUsersTextUtil: MutualUsersTextUtil
) {

    fun createSuccessUiListEntity (
        data: List<UserShakeModel>
    ): List<UserShakeUiModel> = data.map { user ->
        UserShakeUiModel(
            userId = user.userId,
            name = user.name,
            avatarSmall = user.avatarSmall,
            userFriendShakeStatus = user.isFriends.toUserFriendStatus(),
            labelText = if (user.isFriends.toUserFriendStatus() == UserFriendShakeStatus.USER_SHAKE_ALREADY_FRIENDS) {
                resourceManager.getString(R.string.shake_you_already_friends_with, user.name)
            } else {
                resourceManager.getString(R.string.you_bumped_with, user.name)
            },
            mutualUsers = if (user.mutualUserModel != null) {
                MutualUsersUiModel(
                    moreCount = user.mutualUserModel.moreCount,
                    mutualUsers = createMutualUsers(user.mutualUserModel),
                    mutualUsersTextColorRes = if (IS_APP_REDESIGNED) R.color.uiKitColorForegroundPrimary else R.color.white,
                    mutualUsersText = mutualUsersTextUtil.getMutualText(
                        fullUsersName = user.mutualUserModel.mutualUsers.map { it.name.orEmpty() },
                        moreCount = user.mutualUserModel.moreCount
                    )
                )
            } else null,
            approvedUser = user.approved.toBoolean(),
            topContentMaker = user.topContentMaker.toBoolean()
        )
    }

    fun mapNextSelectedUser(
        nextUser: UserShakeUiModel,
        currentState: UserShakeUiState
    ): UserShakeUiState {
        return currentState.copy(
            shakeUser = nextUser,
            selectedPosition = currentState.selectedPosition + NEXT_POSITION,
            isNeedToShowDotsIndicator = nextUser.userFriendShakeStatus != UserFriendShakeStatus.USER_SHAKE_ALREADY_FRIENDS
        )
    }

    fun createShakeUserUiStateModel(
        currentUser: UserShakeUiModel,
        allUsers: List<UserShakeUiModel>
    ): UserShakeUiState {
        val dotPosition = allUsers.indexOf(currentUser)
        return UserShakeUiState(
            shakeUser = currentUser,
            dotsCount = allUsers.size,
            selectedPosition = if (dotPosition == -1) DEFAULT_DOT_SELECTED_POSITION else dotPosition,
            isNeedToShowDotsIndicator = currentUser.userFriendShakeStatus !=
                UserFriendShakeStatus.USER_SHAKE_ALREADY_FRIENDS && allUsers.size > MIN_DOTS_COUNT
        )
    }

    fun changeCurrentUserState(
        currentUserState: UserShakeUiState,
        allUsers: List<UserShakeUiModel>
    ): UserShakeUiState {
        val dotPosition = allUsers.indexOf(currentUserState.shakeUser)
        return currentUserState.copy(
            dotsCount = allUsers.size,
            isNeedToShowDotsIndicator = currentUserState.shakeUser.userFriendShakeStatus !=
                UserFriendShakeStatus.USER_SHAKE_ALREADY_FRIENDS && allUsers.size > MIN_DOTS_COUNT,
            selectedPosition = if (dotPosition != -1) dotPosition else DEFAULT_DOT_SELECTED_POSITION
        )
    }

    private fun createMutualUsers(
        model: ShakeMutualUsersModel
    ): List<MutualUserUiModel> = model.mutualUsers.map { data ->
        MutualUserUiModel(
            userId = data.userId,
            avatar = data.avatarLink.orEmpty(),
            name = data.name.orEmpty()
        )
    }

    fun getStringByResource(
        @StringRes res: Int,
        name: String
    ): String = resourceManager.getString(res, name)

    private fun Int.toUserFriendStatus(): UserFriendShakeStatus {
        return when (this) {
            FRIEND_STATUS_INCOMING -> UserFriendShakeStatus.USER_SHAKE_FRIEND_REQUESTED_BY_USER
            FRIEND_STATUS_OUTGOING -> UserFriendShakeStatus.USER_SHAKE_FRIEND_REQUESTED_BY_ME
            FRIEND_STATUS_CONFIRMED -> UserFriendShakeStatus.USER_SHAKE_ALREADY_FRIENDS
            else -> UserFriendShakeStatus.USER_SHAKE_REQUEST_UNKNOWN
        }
    }
}
