package com.numplates.nomera3.modules.userprofile.ui.viewholder

import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.buttons.ButtonType
import com.numplates.nomera3.FRIEND_STATUS_CONFIRMED
import com.numplates.nomera3.FRIEND_STATUS_INCOMING
import com.numplates.nomera3.FRIEND_STATUS_NONE
import com.numplates.nomera3.FRIEND_STATUS_OUTGOING
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFriendSubscribeFloorItemBinding
import com.numplates.nomera3.modules.userprofile.ui.fragment.UserInfoRecyclerData
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction

class MeeraFriendSubscribeFloorViewHolder(
    private val binding: MeeraFriendSubscribeFloorItemBinding,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit
) : BaseVH<UserInfoRecyclerData, MeeraFriendSubscribeFloorItemBinding>(binding) {
    override fun bind(data: UserInfoRecyclerData) {
        data as UserInfoRecyclerData.UserEntityFriendSubscribeFloor
        setupTextAndImages(data.friendStatus, data.isSubscribed)
        binding.btnSubscribe.isClickable = true
        initClickListeners(
            friendStatus = data.friendStatus,
            isSubscribed = data.isSubscribed,
            userId = data.userId,
            approved = data.approved,
            influenecer = data.topContentMaker,
            isSuggestionShowed = data.isSuggestionShowed
        )

        if (data.isSuggestionShowed) {
            binding.btnRecommendations.src = R.drawable.ic_outlined_chevron_up_m
        } else {
            binding.btnRecommendations.src = R.drawable.ic_outlined_chevron_down_m
        }
    }

    private fun setupTextAndImages(friendStatus: Int, isSubscribed: Boolean) {
        if (isSubscribed) {
            binding.btnSubscribe.buttonType = ButtonType.OUTLINE
            binding.btnSubscribe.text = binding.root.resources.getString(R.string.reading)
        } else {
            binding.btnSubscribe.buttonType = ButtonType.FILLED
            binding.btnSubscribe.text = binding.root.resources.getString(R.string.general_subscribe)
        }

        val drawable = when {
            friendStatus == FRIEND_STATUS_NONE -> {
                R.drawable.ic_outlined_user_add_m
            }

            friendStatus == FRIEND_STATUS_OUTGOING -> {
                R.drawable.ic_outlined_user_respond_m
            }

            friendStatus == FRIEND_STATUS_INCOMING -> {
                R.drawable.ic_outlined_user_request_m
            }

            friendStatus == FRIEND_STATUS_CONFIRMED -> {
                R.drawable.ic_outlined_following_m
            }

            else -> {
                R.drawable.ic_outlined_user_add_m
            }
        }
        binding.btnAddFriend.src = drawable
    }

    private fun initClickListeners(
        friendStatus: Int,
        isSubscribed: Boolean,
        userId: Long,
        approved: Boolean,
        influenecer: Boolean,
        isSuggestionShowed: Boolean
    ) {
        binding.btnAddFriend.apply {
            setThrottledClickListener {
                profileUIActionHandler.invoke(
                    UserProfileUIAction.OnSuggestionFriendClicked(
                        friendStatus = friendStatus, approved = approved, influenecer = influenecer
                    )
                )
            }
        }
        binding.btnSubscribe.apply {
            setThrottledClickListener {
                profileUIActionHandler.invoke(
                    UserProfileUIAction.OnSubscribeRequestClicked(
                        isSubscribed = isSubscribed,
                        userId = userId,
                        friendStatus = friendStatus,
                        approved = approved,
                        topContent = influenecer,
                        message = binding.root.resources.getString(R.string.meera_subscribed_on_user_notif_on)
                    )
                )
            }
        }

        binding.btnRecommendations.setThrottledClickListener {
            profileUIActionHandler.invoke(UserProfileUIAction.OnShowSuggestion(isSuggestionShowed.not()))
        }
    }
}

