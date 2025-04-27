package com.numplates.nomera3.modules.userprofile.ui.meerafriends.friends

import android.view.ViewGroup
import com.meera.core.adapters.baserecycleradapter.BaseAsyncAdapter
import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.adapters.baserecycleradapter.RecyclerData
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.meera.core.extensions.empty
import com.meera.core.extensions.toBoolean
import com.meera.db.models.userprofile.UserSimple
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.cell.CellRightElement
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.FRIEND_STATUS_NONE
import com.numplates.nomera3.R
import com.numplates.nomera3.REQUEST_NOT_CONFIRMED_BY_ME
import com.numplates.nomera3.databinding.MeeraItemFriendBinding
import com.numplates.nomera3.presentation.model.adaptermodel.SubscriptionType
import com.numplates.nomera3.presentation.view.adapter.newfriends.FriendModel

class MeeraFriendsAdapter(
    private val isMe: Boolean,
    private val userId: Long,
    private val onItemClicked: (FriendModel) -> Unit,
    private val onRemoveClicked: (FriendModel) -> Unit,
    private val onActionClicked: (FriendModel) -> Unit
) : BaseAsyncAdapter<String, FriendsRecyclerData>() {
    override fun getHolderType(viewType: Int, parent: ViewGroup): BaseVH<FriendsRecyclerData, *> {
        return when (viewType) {
            TYPE_DATA -> MeeraFriendVH(
                binding = parent.toBinding(),
                isMe = isMe,
                userId = userId,
                onItemClicked = onItemClicked,
                onRemoveClicked = onRemoveClicked,
                onActionClicked = onActionClicked
            )
            else -> throw RuntimeException("Missing data adapter type")
        }
    }
}

class MeeraFriendVH(
    private val binding: MeeraItemFriendBinding,
    private val isMe: Boolean,
    private val userId: Long,
    private val onItemClicked: (FriendModel) -> Unit,
    private val onRemoveClicked: (FriendModel) -> Unit,
    private val onActionClicked: (FriendModel) -> Unit
) : BaseVH<FriendsRecyclerData, MeeraItemFriendBinding>(binding) {

    override fun bind(data: FriendsRecyclerData) {
        val friendModel = (data as FriendsRecyclerData.RecyclerData).friendModel
        val avatarUrl = friendModel.userModel.avatar
        val userPicConfig = if (avatarUrl.isNullOrBlank().not()) UserpicUiModel(userAvatarUrl = avatarUrl)
        else UserpicUiModel(userAvatarRes = R.drawable.fill_8_round)
        val formattedUniqueName = friendModel.userSimple?.uniqueName?.lowercase()?.let { uName ->
            if (uName.isNotEmpty()) "@$uName" else String.empty()
        } ?: String.empty()

        with(binding.uikitCellUser) {
            setTitleValue(friendModel.userSimple?.name ?: "")
            cellTitleVerified = friendModel.userSimple?.approved.toBoolean()
            setDescriptionValue(formattedUniqueName)
            setLeftUserPicConfig(userPicConfig)
            setCityValue(friendModel.userModel.city ?: "")
            cellPosition = if (data.friendModel.needSeparator) CellPosition.MIDDLE else CellPosition.BOTTOM

            if (isMe) {
                cellRightElement = CellRightElement.ICON
                setRightIconClickListener {
                    onRemoveClicked.invoke(friendModel)
                }
            } else {
                setUserIconBySettings(getSubscriptionType(friendModel.userSimple, userId))
                setRightIconClickListener {
                    onActionClicked.invoke(friendModel)
                }
            }
            rootView.setOnClickListener { onItemClicked.invoke(friendModel) }
        }
    }


    private fun getSubscriptionType(model: UserSimple?, myUserId: Long?): SubscriptionType {
        return when {
            model?.settingsFlags?.friendStatus == REQUEST_NOT_CONFIRMED_BY_ME -> SubscriptionType.TYPE_INCOMING_FRIEND_REQUEST
            model?.settingsFlags?.friendStatus == FRIEND_STATUS_NONE && myUserId != model.userId -> SubscriptionType.TYPE_FRIEND_NONE
            else -> SubscriptionType.DEFAULT
        }
    }

    private fun setUserIconBySettings(subscriptionType: SubscriptionType) {
        binding.uikitCellUser.cellRightElement = CellRightElement.ICON
        when (subscriptionType) {
            SubscriptionType.TYPE_INCOMING_FRIEND_REQUEST -> binding.uikitCellUser.setRightIcon(R.drawable.ic_outlined_user_response_l)
            SubscriptionType.TYPE_FRIEND_NONE -> binding.uikitCellUser.setRightIcon(R.drawable.ic_outlined_user_add_l)
            else -> binding.uikitCellUser.cellRightElement = CellRightElement.NONE
        }
    }
}

private const val TYPE_DATA = 1

sealed interface FriendsRecyclerData : RecyclerData<String, FriendsRecyclerData> {


    data class RecyclerData(
        val id: Long,
        val friendModel: FriendModel,
    ) : FriendsRecyclerData {

        override fun getItemId() = id.toString()
        override fun contentTheSame(newItem: FriendsRecyclerData) = this.equals(newItem)
        override fun itemViewType() = TYPE_DATA
    }

}
