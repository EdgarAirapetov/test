package com.numplates.nomera3.modules.userprofile.ui.meerafriends.subscribers

import android.view.ViewGroup
import com.meera.core.adapters.baserecycleradapter.BaseAsyncAdapter
import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.adapters.baserecycleradapter.RecyclerData
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.meera.core.extensions.toBoolean
import com.meera.db.models.userprofile.UserSimple
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.cell.CellRightElement
import com.meera.uikit.widgets.people.ApprovedIconSize
import com.meera.uikit.widgets.people.TopAuthorApprovedUserModel
import com.meera.uikit.widgets.people.UiKitUsernameView
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.FRIEND_STATUS_NONE
import com.numplates.nomera3.R
import com.numplates.nomera3.REQUEST_NOT_CONFIRMED_BY_ME
import com.numplates.nomera3.databinding.MeeraItemSubscriberBinding
import com.numplates.nomera3.presentation.model.adaptermodel.SubscriptionType

class MeeraSubscribersAdapter(
    private val isMe: Boolean,
    private val userId: Long,
    private val onItemClicked: (UserSimple) -> Unit,
    private val onRemoveClicked: (UserSimple) -> Unit,
    private val onActionClicked: (UserSimple) -> Unit
) : BaseAsyncAdapter<String, SubscribersRecyclerData>() {
    override fun getHolderType(viewType: Int, parent: ViewGroup): BaseVH<SubscribersRecyclerData, *> {
        return when (viewType) {
            TYPE_DATA -> MeeraSubscribersVH(
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


class MeeraSubscribersVH(
    private val binding: MeeraItemSubscriberBinding,
    private val isMe: Boolean,
    private val userId: Long,
    private val onItemClicked: (UserSimple) -> Unit,
    private val onRemoveClicked: (UserSimple) -> Unit,
    private val onActionClicked: (UserSimple) -> Unit
) : BaseVH<SubscribersRecyclerData, MeeraItemSubscriberBinding>(binding) {
    override fun bind(data: SubscribersRecyclerData) {
        val friendModel = data as SubscribersRecyclerData.RecyclerData

        with(binding.uikitCellUser) {
            val avatarUrl = friendModel.user.avatarSmall
            if (avatarUrl.isNullOrBlank().not()) {
                setLeftUserPicConfig(UserpicUiModel(userAvatarUrl = avatarUrl))
            } else {
                setLeftUserPicConfig(UserpicUiModel(userAvatarRes = R.drawable.fill_8_round))
            }

            setTitleValue(friendModel.user.name ?: "")
            findViewById<UiKitUsernameView>(R.id.tv_title)?.apply {
                enableTopContentAuthorApprovedUser(
                    params = TopAuthorApprovedUserModel(
                        customIconTopContent = R.drawable.ic_approved_author_gold_10,
                        approvedIconSize = ApprovedIconSize.SMALL,
                        approved = friendModel.user.approved.toBoolean(),
                        interestingAuthor = friendModel.user.topContentMaker.toBoolean()
                    )
                )
            }
            friendModel.user.uniqueName?.lowercase()?.let { uName ->
                if (uName.isNotEmpty()) {
                    val formattedUniqueName = "@$uName"
                    binding.uikitCellUser.setDescriptionValue(formattedUniqueName)
                }
            }
            setCityValue(friendModel.user.city?.name ?: "")

            if (isMe) {
                cellRightElement = CellRightElement.ICON
                setRightIconClickListener {
                    onRemoveClicked.invoke(friendModel.user)
                }
            } else {
                setUserIconBySettings(getSubscriptionType(friendModel.user, userId))
                setRightIconClickListener {
                    onActionClicked.invoke(friendModel.user)
                }
            }

            cellPosition = CellPosition.MIDDLE

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


sealed interface SubscribersRecyclerData : RecyclerData<String, SubscribersRecyclerData> {


    data class RecyclerData(
        val id: Long,
        val user: UserSimple,
    ) : SubscribersRecyclerData {
        override fun getItemId() = id.toString()
        override fun contentTheSame(newItem: SubscribersRecyclerData) = this.equals(newItem)
        override fun itemViewType() = TYPE_DATA
    }

}
