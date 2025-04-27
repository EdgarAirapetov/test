package com.numplates.nomera3.modules.userprofile.ui.meerafriends.mutual

import android.view.ViewGroup
import com.meera.core.adapters.baserecycleradapter.BaseAsyncAdapter
import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.adapters.baserecycleradapter.RecyclerData
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.cell.CellRightElement
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemFriendBinding
import com.numplates.nomera3.presentation.model.adaptermodel.FriendsFollowersUiModel
import com.numplates.nomera3.presentation.model.adaptermodel.SubscriptionType

class MeeraMutualFriendsAdapter(
    private val onItemCLicked: (FriendsFollowersUiModel) -> Unit,
    private val onActionClicked: (FriendsFollowersUiModel) -> Unit
) : BaseAsyncAdapter<String, MutualFriendsRecyclerData>() {
    override fun getHolderType(viewType: Int, parent: ViewGroup): BaseVH<MutualFriendsRecyclerData, *> {
        return when (viewType) {
            TYPE_DATA -> MeeraMutualFriendVH(
                binding = parent.toBinding(), onItemCLicked = onItemCLicked, onActionClicked = onActionClicked
            )

            else -> throw RuntimeException("Missing data adapter type")
        }
    }
}


class MeeraMutualFriendVH(
    private val binding: MeeraItemFriendBinding,
    private val onItemCLicked: (FriendsFollowersUiModel) -> Unit,
    private val onActionClicked: (FriendsFollowersUiModel) -> Unit
) : BaseVH<MutualFriendsRecyclerData, MeeraItemFriendBinding>(binding) {
    override fun bind(data: MutualFriendsRecyclerData) {
        val friendModel = data as MutualFriendsRecyclerData.RecyclerData

        with(binding.uikitCellUser) {

            setTitleValue(friendModel.friendModel.userSimple?.name ?: "")
            cellTitleVerified = friendModel.friendModel.userSimple?.approved.toBoolean()


            setUserIconBySettings(friendModel.friendModel.subscriptionType)


            val avatarUrl = friendModel.friendModel.userSimple?.avatarSmall
            if (avatarUrl.isNullOrBlank().not()) {
                setLeftUserPicConfig(UserpicUiModel(userAvatarUrl = avatarUrl))
            } else {
                setLeftUserPicConfig(UserpicUiModel(userAvatarRes = R.drawable.fill_8_round))
            }
            friendModel.friendModel.userSimple?.uniqueName?.lowercase()?.let { uName ->
                if (uName.isNotEmpty()) {
                    val formattedUniqueName = "@$uName"
                    binding.uikitCellUser.setDescriptionValue(formattedUniqueName)
                }
            }
            setCityValue(friendModel.friendModel.userSimple?.city?.name ?: "")
            cellPosition = CellPosition.MIDDLE

            setRightIconClickListener {
                onActionClicked.invoke(friendModel.friendModel)
            }

            rootView.setThrottledClickListener {
                onItemCLicked.invoke(friendModel.friendModel)
            }
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


const val TYPE_DATA = 1


sealed interface MutualFriendsRecyclerData : RecyclerData<String, MutualFriendsRecyclerData> {


    data class RecyclerData(
        val id: Long,
        val friendModel: FriendsFollowersUiModel,
    ) : MutualFriendsRecyclerData {
        override fun getItemId() = id.toString()
        override fun contentTheSame(newItem: MutualFriendsRecyclerData) = this.equals(newItem)
        override fun itemViewType() = TYPE_DATA
    }

}
