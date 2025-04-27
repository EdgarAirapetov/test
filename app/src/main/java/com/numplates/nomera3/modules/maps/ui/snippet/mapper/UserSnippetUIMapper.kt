package com.numplates.nomera3.modules.maps.ui.snippet.mapper

import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.maps.domain.model.UserSnippetModel
import com.numplates.nomera3.modules.maps.domain.model.UserUpdateModel
import com.numplates.nomera3.modules.maps.ui.model.MapUserUiModel
import com.numplates.nomera3.modules.maps.ui.snippet.UserSnippetBottomSheetWidget.Companion.PAGE_SIZE
import com.numplates.nomera3.modules.maps.ui.snippet.model.ContentState
import com.numplates.nomera3.modules.maps.ui.snippet.model.DataFetchingStateModel
import com.numplates.nomera3.modules.maps.ui.snippet.model.LoaderItem
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.maps.ui.snippet.model.UserPreviewItem
import com.numplates.nomera3.modules.maps.ui.snippet.model.UserSnippetUiModel
import javax.inject.Inject

class UserSnippetUIMapper @Inject constructor() {

    fun mapUiModel(
        selectedUser: MapUserUiModel?,
        pages: Map<Int, List<UserSnippetModel>>,
        currentItemIndex: Int,
        snippetState: SnippetState,
        dataFetchingState: DataFetchingStateModel
    ): UserSnippetUiModel {
        return if (selectedUser != null) {
            val items = if (pages.isEmpty()) {
                val item = UserPreviewItem(
                    uid = selectedUser.id,
                    payload = selectedUser
                )
                listOf(item)
            } else {
                val sortedPages = pages.entries
                    .sortedBy { it.key }
                    .map { it.value }
                val users = sortedPages
                    .map { mapItems(selectedUser, it) }
                    .flatten()
                if (sortedPages.lastOrNull()?.size == PAGE_SIZE && dataFetchingState.loading) {
                    users.plus(LoaderItem)
                } else {
                    users
                }
            }
            val pageIsPinPreview = (items.getOrNull(0) as? UserPreviewItem)?.payload is MapUserUiModel
            val contentState = if (dataFetchingState.error != null && pageIsPinPreview) ContentState.ERROR
            else ContentState.ITEMS
            val showFadeIn = contentState == ContentState.ITEMS && snippetState == SnippetState.Preview
            val pageIsLoaderItem = items.getOrNull(currentItemIndex) is LoaderItem
            val expandedStateRestricted = pageIsPinPreview || pageIsLoaderItem
            UserSnippetUiModel(
                contentState = contentState,
                items = items,
                isFull = selectedUser.isFull,
                showFadeIn = showFadeIn,
                selectedUserIsVip = selectedUser.accountType == AccountTypeEnum.ACCOUNT_TYPE_VIP,
                expandedStateRestricted = expandedStateRestricted
            )
        } else {
            UserSnippetUiModel(
                contentState = ContentState.ITEMS,
                items = listOf(),
                showFadeIn = false,
                selectedUserIsVip = null,
                expandedStateRestricted = true
            )
        }
    }

    fun mapAuxUiModel(
        auxUser: MapUserUiModel?,
        auxUserFullModel: UserSnippetModel?,
        snippetState: SnippetState,
        dataFetchingState: DataFetchingStateModel
    ): UserSnippetUiModel {
        return if (auxUser != null) {
            val item = UserPreviewItem(
                    uid = auxUser.id,
                    payload = auxUserFullModel ?: auxUser
                )
            val pageIsPinPreview = item.payload is MapUserUiModel
            val contentState = if (dataFetchingState.error != null && pageIsPinPreview) ContentState.ERROR
            else ContentState.ITEMS
            val showFadeIn = contentState == ContentState.ITEMS && snippetState == SnippetState.Preview
            UserSnippetUiModel(
                contentState = contentState,
                items = listOf(item),
                showFadeIn = showFadeIn,
                selectedUserIsVip = auxUser.accountType == AccountTypeEnum.ACCOUNT_TYPE_VIP,
                expandedStateRestricted = pageIsPinPreview
            )
        } else {
            UserSnippetUiModel(
                contentState = ContentState.ITEMS,
                items = listOf(),
                showFadeIn = false,
                selectedUserIsVip = null,
                expandedStateRestricted = true
            )
        }
    }

    fun mapUpdateSnippetModel(snippetModel: UserSnippetModel, updateModel: UserUpdateModel): UserSnippetModel {
        return snippetModel.copy(
            uid = updateModel.uid,
            name = updateModel.name,
            uniqueName = updateModel.uniqueName,
            birthday = updateModel.birthday,
            avatar = updateModel.avatar,
            avatarBig = updateModel.avatarBig,
            gender = updateModel.gender,
            accountType = updateModel.accountType,
            accountColor = updateModel.accountColor,
            city = updateModel.city,
            country = updateModel.country,
            approved = updateModel.approved,
            friendStatus = updateModel.friendStatus,
            subscriptionOn = updateModel.subscriptionOn,
            subscribersCount = updateModel.subscribersCount,
            profileBlocked = updateModel.profileBlocked,
            profileDeleted = updateModel.profileDeleted,
            blacklistedByMe = updateModel.blacklistedByMe,
            blacklistedMe = updateModel.blacklistedMe
        )
    }

    private fun mapItems(
        selectedUserModel: MapUserUiModel,
        userSnippets: List<UserSnippetModel>
    ): List<UserPreviewItem> {
        return userSnippets
            .sortedWith( UserSnippetComparator(selectedUserModel.id))
            .map { model ->
                UserPreviewItem(
                    uid = model.uid,
                    payload = model,
                )
            }
    }

    private class UserSnippetComparator(val prioritizedUid: Long? = null) : Comparator<UserSnippetModel> {

        override fun compare(o1: UserSnippetModel, o2: UserSnippetModel): Int {
            return when {
                (o2.uid == prioritizedUid && o1.uid != prioritizedUid) || o1.distance > o2.distance -> 1
                (o1.uid == prioritizedUid && o2.uid != prioritizedUid) || o1.distance < o2.distance -> -1
                else -> o1.uid.compareTo(o2.uid)
            }
        }
    }
}
