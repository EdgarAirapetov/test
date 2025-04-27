package com.numplates.nomera3.modules.userprofile.ui.viewholder

import android.annotation.SuppressLint
import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.extensions.CountStringPostfixData
import com.meera.core.extensions.asCountString
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.textColor
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraSubscriberFloorItemBinding
import com.numplates.nomera3.modules.userprofile.ui.fragment.UserInfoRecyclerData
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction

class MeeraSubscribersFloorViewHolder(
    private val binding: MeeraSubscriberFloorItemBinding,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit
) : BaseVH<UserInfoRecyclerData, MeeraSubscriberFloorItemBinding>(binding) {

    private var model: UserInfoRecyclerData.SubscribersFloorUiEntity? = null

    override fun bind(data: UserInfoRecyclerData) {
        model = data as UserInfoRecyclerData.SubscribersFloorUiEntity
        setupText(data)
        initClickListeners(data.isMe)
        profileUIActionHandler.invoke(UserProfileUIAction.OnHolderBind(absoluteAdapterPosition))
    }

    private fun initClickListeners(isMe: Boolean) {
        if (!isUserNotAllowShowFriendsSubscribers(isMe)){
            binding.llFriendBtn.setThrottledClickListener { handleShowFriendsClick(isMe) }
            binding.llSubscribersBtn.setThrottledClickListener { handleShowSubscribersClick(isMe) }
            binding.llSubscriptionBtn.setThrottledClickListener { handleShowSubscriptionsClick(isMe) }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupText(data: UserInfoRecyclerData.SubscribersFloorUiEntity) {
        val postfixTextData = CountStringPostfixData(
            thousand = itemView.context.getString(R.string.general_postfix_count_thousand),
            million = itemView.context.getString(R.string.general_postfix_count_million)
        )
        binding.tvCityCountry.text = String.format("%s, %s", data.city, data.country)
        binding.tvFriendsCount.text = data.friendsCount.asCountString(postfixTextData)
        binding.tvSubscribersCount.text = data.subscribersCount.asCountString(postfixTextData)
        binding.tvSubscriptionsCount.text = data.subscriptionCount.asCountString(postfixTextData)

        val incomeFriendCount = getAcceptableIncomeFriendCount(data.friendsRequestCount.toInt())
        if (incomeFriendCount.isNotEmpty() && data.isMe) {
            binding.tvFriendsIncomingCount.text = "(+$incomeFriendCount)" // friends count here
            binding.tvFriendsIncomingCount.visible()
        } else {
            binding.tvFriendsIncomingCount.gone()
        }
        isEnableShowFriendsSubscribers(data.isMe)
    }

    private fun isEnableShowFriendsSubscribers(isMe: Boolean){
        if (isUserNotAllowShowFriendsSubscribers(isMe)){
            binding.tvFriendsCount.textColor(R.color.uiKitColorDisabledPrimary)
            binding.tvFriendsIncomingCount.invisible()
            binding.tvFriends.textColor(R.color.uiKitColorDisabledPrimary)

            binding.tvSubscribersCount.textColor(R.color.uiKitColorDisabledPrimary)
            binding.tvSubscribers.textColor(R.color.uiKitColorDisabledPrimary)
            binding.tvSubscriptions.textColor(R.color.uiKitColorDisabledPrimary)
            binding.tvSubscriptionsCount.textColor(R.color.uiKitColorDisabledPrimary)
        }
    }

    private fun isUserNotAllowShowFriendsSubscribers(
        isMe: Boolean,
    ): Boolean {
        return !isMe && !(model?.showFriendsSubscribers ?: false)
    }

    private fun handleShowFriendsClick(isMe: Boolean) {
        if (isUserNotAllowShowFriendsSubscribers(isMe)) {
            profileUIActionHandler.invoke(UserProfileUIAction.DisabledSubscriberFloorClicked)
        }
        profileUIActionHandler.invoke(UserProfileUIAction.OnFriendsListClicked)
    }

    private fun handleShowSubscribersClick(isMe: Boolean) {
        if (isUserNotAllowShowFriendsSubscribers(isMe)) {
            profileUIActionHandler.invoke(UserProfileUIAction.DisabledSubscriberFloorClicked)
        }
        profileUIActionHandler.invoke(UserProfileUIAction.OnSubscribersListClicked)
    }

    private fun handleShowSubscriptionsClick(isMe: Boolean) {
        if (isUserNotAllowShowFriendsSubscribers(isMe)) {
            profileUIActionHandler.invoke(UserProfileUIAction.DisabledSubscriberFloorClicked)
        }
        profileUIActionHandler.invoke(UserProfileUIAction.OnSubscriptionsListClicked)
    }

    private fun getAcceptableIncomeFriendCount(friendsRequestCount: Int?): String {
        return if (friendsRequestCount != null) {
            when (friendsRequestCount) {
                0 -> ""
                in 1..99 -> "$friendsRequestCount"
                else -> "99"
            }
        } else {
            ""
        }
    }
}

