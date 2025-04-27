package com.numplates.nomera3.presentation.view.adapter.holders

import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.db.models.userprofile.City
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.cell.CellRightElement
import com.meera.uikit.widgets.people.ApprovedIconSize
import com.meera.uikit.widgets.people.TopAuthorApprovedUserModel
import com.meera.uikit.widgets.people.UiKitUsernameView
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraUserItemBinding
import com.numplates.nomera3.presentation.model.adaptermodel.FriendsFollowersUiModel
import com.numplates.nomera3.presentation.model.adaptermodel.SubscriptionType

private const val MARGIN_START_DIVIDER = 8

class MeeraFriendsFollowerHolder(
    val binding: MeeraUserItemBinding,
    private val actionListener: (action: MeeraFriendsFollowerAction) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(model: FriendsFollowersUiModel, lastItem: Boolean) {
        binding.vFriends.setMarginStartDivider(MARGIN_START_DIVIDER.dp)
        setUserData(model, lastItem)
        initListeners(model)
    }

    private fun setUserData(model: FriendsFollowersUiModel, lastItem: Boolean) {
        if (lastItem) binding.vFriends.cellPosition = CellPosition.BOTTOM
        binding.vFriends.setCityTextColor(R.color.uiKitColorForegroundPrimary)
        binding.vFriends.setTitleValue(model.userSimple?.name ?: "")
        initUserAvatar(model.userSimple?.avatarSmall, model.userSimple?.gender)

        binding.vFriends.findViewById<UiKitUsernameView>(R.id.tv_title)?.apply {
            enableTopContentAuthorApprovedUser(
                params = TopAuthorApprovedUserModel(
                    customIconTopContent = R.drawable.ic_approved_author_gold_10,
                    approvedIconSize = ApprovedIconSize.SMALL,
                    approved = model.isAccountApproved,
                    interestingAuthor = model.userSimple?.topContentMaker?.toBoolean() ?: false
                )
            )
        }

        initCity(model.userSimple?.city)
        initUniqueName(model.userSimple?.uniqueName)
        setUserIconBySettings(model.subscriptionType)
    }

    private fun initUniqueName(uniqueName: String?) {
        uniqueName?.let { uName: String ->
            if (uName.isNotEmpty()) {
                val formattedUniqueName = "@$uName"
                binding.vFriends.setDescriptionValue(formattedUniqueName)
            }
        }
    }

    private fun initCity(city: City?) {
        binding.vFriends.cellCityText = true

        city?.let { cityNotNull ->
            binding.vFriends.setCityValue(cityNotNull.name ?: "")
        }
    }

    private fun initListeners(model: FriendsFollowersUiModel) {
        binding.vFriends.cellRightIconClickListener = {
            when (model.subscriptionType) {
                SubscriptionType.TYPE_INCOMING_FRIEND_REQUEST -> actionListener.invoke(
                    MeeraFriendsFollowerAction.AcceptRequestFriendClick(
                        model
                    )
                )

                SubscriptionType.TYPE_FRIEND_NONE -> actionListener.invoke(
                    MeeraFriendsFollowerAction.AddFriendsClick(
                        model
                    )
                )

                else -> Unit
            }
        }
        binding.vFriends.setThrottledClickListener {
            model.userSimple?.userId?.let {
                actionListener.invoke(MeeraFriendsFollowerAction.UserClick(it))
            }
        }
    }

    private fun setUserIconBySettings(subscriptionType: SubscriptionType) {
        when (subscriptionType) {
            SubscriptionType.TYPE_INCOMING_FRIEND_REQUEST ->
                binding.vFriends.setRightIcon(R.drawable.ic_outlined_user_response_l)

            SubscriptionType.TYPE_FRIEND_NONE -> binding.vFriends.setRightIcon(R.drawable.ic_outlined_user_add_l)
            else -> binding.vFriends.cellRightElement = CellRightElement.NONE
        }
    }

    private fun initUserAvatar(avatarSmall: String?, gender: Int?){
        avatarSmall?.let {
            binding.vFriends.setLeftUserPicConfig(
                UserpicUiModel(
                    userAvatarUrl = avatarSmall,
                    userAvatarErrorPlaceholder = identifyAvatarByGender(gender)
                )
            )
        } ?: {
            binding.vFriends.setLeftUserPicConfig(
                UserpicUiModel(
                    userAvatarRes = identifyAvatarByGender(gender)
                )
            )
        }
    }

    private fun identifyAvatarByGender(gender: Int?): Int {
        return if (gender.toBoolean()) {
            R.drawable.ic_man_avatar_placeholder
        } else {
            R.drawable.ic_woman_avatar_placeholder
        }
    }
}
