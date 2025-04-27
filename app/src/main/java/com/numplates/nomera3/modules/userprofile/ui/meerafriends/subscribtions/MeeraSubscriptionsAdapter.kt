package com.numplates.nomera3.modules.userprofile.ui.meerafriends.subscribtions

import android.view.ViewGroup
import com.meera.core.adapters.baserecycleradapter.BaseAsyncAdapter
import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.adapters.baserecycleradapter.RecyclerData
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.meera.core.extensions.toBoolean
import com.meera.db.models.userprofile.UserSimple
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.cell.CellRightElement
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.FRIEND_STATUS_NONE
import com.numplates.nomera3.R
import com.numplates.nomera3.REQUEST_NOT_CONFIRMED_BY_ME
import com.numplates.nomera3.databinding.MeeraItemSubscriptionBinding
import com.numplates.nomera3.presentation.model.adaptermodel.SubscriptionType

class MeeraSubscriptionsAdapter(
    private val isMe: Boolean,
    private val userId: Long,
    private val onItemClicked: (UserSimple) -> Unit,
    private val onActionClicked: (UserSimple) -> Unit
) : BaseAsyncAdapter<String, SubscriptionsRecyclerData>() {
    override fun getHolderType(viewType: Int, parent: ViewGroup): BaseVH<SubscriptionsRecyclerData, *> {
        return when (viewType) {
            TYPE_DATA -> MeeraSubscriptionsVH(
                binding = parent.toBinding(),
                isMe = isMe,
                userId = userId,
                onItemClicked = onItemClicked,
                onActionClicked = onActionClicked
            )

            else -> throw RuntimeException("Missing data adapter type")
        }
    }

}


class MeeraSubscriptionsVH(
    private val binding: MeeraItemSubscriptionBinding,
    private val isMe: Boolean,
    private val userId: Long,
    private val onItemClicked: (UserSimple) -> Unit,
    private val onActionClicked: (UserSimple) -> Unit
) : BaseVH<SubscriptionsRecyclerData, MeeraItemSubscriptionBinding>(binding) {
    override fun bind(data: SubscriptionsRecyclerData) {
        val friendModel = data as SubscriptionsRecyclerData.RecyclerData

        with(binding.uikitCellUser) {
            val avatarUrl = friendModel.user.avatarSmall
            if (avatarUrl.isNullOrBlank().not()) {
                setLeftUserPicConfig(UserpicUiModel(userAvatarUrl = avatarUrl))
            } else {
                setLeftUserPicConfig(UserpicUiModel(userAvatarRes = R.drawable.fill_8_round))
            }

            cellTitleVerified = friendModel.user.approved.toBoolean()
            setTitleValue(friendModel.user.name ?: "")
            friendModel.user.uniqueName?.lowercase()?.let { uName ->
                if (uName.isNotEmpty()) {
                    val formattedUniqueName = "@$uName"
                    binding.uikitCellUser.setDescriptionValue(formattedUniqueName)
                }
            }
            setCityValue(friendModel.user.city?.name ?: "")
            cellPosition = CellPosition.MIDDLE

            if (isMe) {
                cellRightElement = CellRightElement.ICON
            } else {
                setUserIconBySettings(getSubscriptionType(friendModel.user, userId))
                setRightIconClickListener {
                    onActionClicked.invoke(friendModel.user)
                }
            }

            rootView.setOnClickListener { onItemClicked.invoke(friendModel.user) }
        }
    }


    private fun getSubscriptionType(
        model: UserSimple?, myUserId: Long?
    ): SubscriptionType {
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


const val TYPE_DATA = 1


sealed interface SubscriptionsRecyclerData : RecyclerData<String, SubscriptionsRecyclerData> {
    data class RecyclerData(
        val id: Long,
        val user: UserSimple,
    ) : SubscriptionsRecyclerData {
        override fun getItemId() = id.toString()
        override fun contentTheSame(newItem: SubscriptionsRecyclerData) = this.equals(newItem)
        override fun itemViewType() = TYPE_DATA
    }

}
