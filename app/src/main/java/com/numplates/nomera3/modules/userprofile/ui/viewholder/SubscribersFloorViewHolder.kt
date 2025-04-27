package com.numplates.nomera3.modules.userprofile.ui.viewholder

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat.getColor
import com.meera.core.extensions.asCountString
import com.meera.core.extensions.click
import com.meera.core.extensions.color
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.userprofile.ui.entity.SubscribersFloorUiEntity
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction

class SubscribersFloorViewHolder(
    parent: ViewGroup,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit
) : BaseUserViewHolder<SubscribersFloorUiEntity>(parent, R.layout.item_subscriber_floor) {

    private val llFriendBtn = itemView.findViewById<LinearLayout>(R.id.ll_friend_btn)
    private val llSubscribersBtn = itemView.findViewById<LinearLayout>(R.id.ll_subscribers_btn)
    private val llSubscriptionBtn = itemView.findViewById<LinearLayout>(R.id.ll_subscription_btn)

    private val tvFriendsCount = itemView.findViewById<TextView>(R.id.tv_friends_count)
    private val tvSubscribersCount = itemView.findViewById<TextView>(R.id.tv_subscribers_count)
    private val tvSubscriptionsCount = itemView.findViewById<TextView>(R.id.tv_subscriptions_count)

    private val tvFriends = itemView.findViewById<TextView>(R.id.tv_friends)
    private val tvSubscribers = itemView.findViewById<TextView>(R.id.tv_subscribers)
    private val tvSubscriptions = itemView.findViewById<TextView>(R.id.tv_subscriptions)

    private val tvFriendsIncomingCount =
        itemView.findViewById<TextView>(R.id.tv_friends_incoming_count)
    private var model: SubscribersFloorUiEntity? = null

    override fun bind(data: SubscribersFloorUiEntity) {
        model = data
        setupTheme(data)
        setupText(data)
        initClickListeners(data.isMe)
        profileUIActionHandler.invoke(UserProfileUIAction.OnHolderBind(absoluteAdapterPosition))
    }

    private fun initClickListeners(isMe: Boolean) {
        llFriendBtn.click { handleShowFriendsClick(isMe) }
        llSubscribersBtn.click { handleShowSubscribersClick(isMe) }
        llSubscriptionBtn.click { handleShowSubscriptionsClick(isMe) }
    }

    @SuppressLint("SetTextI18n")
    private fun setupText(data: SubscribersFloorUiEntity) {
        // configure counts with convertValueToString
        tvFriendsCount?.text = data.friendsCount.asCountString()
        tvSubscribersCount?.text = data.subscribersCount.asCountString()
        tvSubscriptionsCount?.text = data.subscriptionCount.asCountString()

        val incomeFriendCount = getAcceptableIncomeFriendCount(data.friendsRequestCount.toInt())
        if (incomeFriendCount.isNotEmpty() && data.isMe) {
            tvFriendsIncomingCount?.text = "(+$incomeFriendCount)" // friends count here
            tvFriendsIncomingCount?.visible()
        } else {
            tvFriendsIncomingCount?.gone()
        }
    }

    private fun setupTheme(entityUiEntity: SubscribersFloorUiEntity) {
        when (entityUiEntity.userStatus) {
            AccountTypeEnum.ACCOUNT_TYPE_PREMIUM,
            AccountTypeEnum.ACCOUNT_TYPE_REGULAR -> {
                changeButtonsBackground(R.drawable.background_friends_subscriptions_regular)
                handleAccountTypeRegular(
                    isMe = entityUiEntity.isMe,
                    isNeedShowFriendsSubscribers = entityUiEntity.showFriendsSubscribers
                )
            }
            AccountTypeEnum.ACCOUNT_TYPE_VIP -> {
                changeButtonsBackground(R.drawable.background_friends_subscriptions_vip)
                handleAccountTypeVip(
                    isMe = entityUiEntity.isMe,
                    isNeedShowFriendsSubscribers = entityUiEntity.showFriendsSubscribers
                )
            }
            else -> {}
        }
    }

    private fun handleAccountTypeRegular(isMe: Boolean, isNeedShowFriendsSubscribers: Boolean) {
        if (isMe) {
            makeAllTextPurple()
        } else {
            setUserThemeByPrivacy(
                isAllowShowFriendsSubscribers = isNeedShowFriendsSubscribers,
                isVip = false
            )
        }
    }

    private fun handleAccountTypeVip(isMe: Boolean, isNeedShowFriendsSubscribers: Boolean) {
        if (isMe) {
            makeAllTextWhite()
        } else {
            setUserThemeByPrivacy(
                isAllowShowFriendsSubscribers = isNeedShowFriendsSubscribers,
                isVip = true
            )
        }
    }

    /**
     * Устанавливаем состояние кнопок "Друзья/Подписки/Подписчики" другого юзера
     */
    private fun setUserThemeByPrivacy(
        isAllowShowFriendsSubscribers: Boolean,
        isVip: Boolean
    ) {
        when {
            isAllowShowFriendsSubscribers -> {
                setUserColorPrivacyAllowed(isVip)
            }
            else -> {
                setUserRegularColorPrivacyNotAllowed(isVip)
            }
        }
    }

    private fun setUserColorPrivacyAllowed(isVip: Boolean) {
        setFriendsTextColorByCountPrivacy(isVip)
        setSubscriberTextColorByCountPrivacy(isVip)
        setSubscriptionTextColorByCountPrivacy(isVip)
    }

    private fun setFriendsTextColorByCountPrivacy(isVip: Boolean) {
        if (isVip) {
            setFriendsTextColor(
                getUserVipFriendPrivacyTextColor(
                    model?.friendsCount ?: 0
                )
            )
        } else {
            setFriendsTextColor(
                getUserRegularFriendPrivacyTextColor(
                    model?.friendsCount ?: 0
                )
            )
        }
    }

    private fun setSubscriberTextColorByCountPrivacy(isVip: Boolean) {
        if (isVip) {
            setSubscribersTextColor(
                getUserVipFriendPrivacyTextColor(model?.subscribersCount ?: 0)
            )
        } else {
            setSubscribersTextColor(
                getUserRegularFriendPrivacyTextColor(model?.subscribersCount ?: 0)
            )
        }
    }

    private fun setSubscriptionTextColorByCountPrivacy(isVip: Boolean) {
        if (isVip) {
            setSubscriptionsTextColor(
                getUserVipFriendPrivacyTextColor(model?.subscriptionCount ?: 0)
            )
        } else {
            setSubscriptionsTextColor(
                getUserRegularFriendPrivacyTextColor(model?.subscriptionCount ?: 0)
            )
        }
    }

    private fun setUserRegularColorPrivacyNotAllowed(isVip: Boolean) {
        if (isVip) makeAllTextWhite() else makeAllTextBlack()
    }

    private fun getUserVipFriendPrivacyTextColor(subscribersItemCount: Long) =
        if (subscribersItemCount > 0) getVipGoldTextColor() else getWhiteTextColor()

    private fun getUserRegularFriendPrivacyTextColor(subscribersItemCount: Long) =
        if (subscribersItemCount > 0) getPurpleTextColor() else getBlackTextColor()

    private fun getVipGoldTextColor() =
        itemView.context.color(R.color.vip_map_distance_color)

    private fun getWhiteTextColor() =
        itemView.context.color(R.color.ui_white)

    private fun getPurpleTextColor() =
        itemView.context.color(R.color.ui_purple)

    private fun getBlackTextColor() =
        itemView.context.color(R.color.ui_black)

    private fun makeAllTextWhite() {
        val color = getColor(itemView.context, R.color.ui_white)
        tvFriendsCount?.setTextColor(color)
        tvSubscribersCount?.setTextColor(color)
        tvSubscriptionsCount?.setTextColor(color)
        tvFriendsIncomingCount?.setTextColor(color)

        tvFriends?.setTextColor(color)
        tvSubscribers?.setTextColor(color)
        tvSubscriptions?.setTextColor(color)
    }

    private fun makeAllTextPurple() {
        val color = getColor(itemView.context, R.color.ui_purple)
        tvFriendsCount?.setTextColor(color)
        tvSubscribersCount?.setTextColor(color)
        tvSubscriptionsCount?.setTextColor(color)
        tvFriendsIncomingCount?.setTextColor(color)

        tvFriends?.setTextColor(color)
        tvSubscribers?.setTextColor(color)
        tvSubscriptions?.setTextColor(color)
    }

    private fun makeAllTextBlack() {
        val color = itemView.context.color(R.color.ui_black)
        tvFriendsCount?.setTextColor(color)
        tvSubscribersCount?.setTextColor(color)
        tvSubscriptionsCount?.setTextColor(color)
        tvFriendsIncomingCount?.setTextColor(color)
        tvFriends?.setTextColor(color)
        tvSubscribers?.setTextColor(color)
        tvSubscriptions?.setTextColor(color)
    }

    private fun setFriendsTextColor(@ColorInt textColor: Int) {
        tvFriends?.setTextColor(textColor)
        tvFriendsCount?.setTextColor(textColor)
    }

    private fun setSubscribersTextColor(@ColorInt textColor: Int) {
        tvSubscribers?.setTextColor(textColor)
        tvSubscribersCount?.setTextColor(textColor)
    }

    private fun setSubscriptionsTextColor(@ColorInt textColor: Int) {
        tvSubscriptions?.setTextColor(textColor)
        tvSubscriptionsCount?.setTextColor(textColor)
    }

    private fun isUserNotAllowShowFriendsSubscribers(
        isMe: Boolean,
        userCounter: Long?
    ): Boolean {
        return !isMe && ((userCounter ?: 0) == 0L || !(model?.showFriendsSubscribers ?: false))
    }

    private fun changeButtonsBackground(@DrawableRes resId: Int) {
        llFriendBtn.setBackgroundResource(resId)
        llSubscribersBtn.setBackgroundResource(resId)
        llSubscriptionBtn.setBackgroundResource(resId)
    }

    private fun handleShowFriendsClick(isMe: Boolean) {
        if (isUserNotAllowShowFriendsSubscribers(isMe, model?.friendsCount)) {
            profileUIActionHandler.invoke(UserProfileUIAction.DisabledSubscriberFloorClicked)
        } else {
            profileUIActionHandler.invoke(UserProfileUIAction.OnFriendsListClicked)
        }
    }

    private fun handleShowSubscribersClick(isMe: Boolean) {
        if (isUserNotAllowShowFriendsSubscribers(isMe, model?.subscribersCount)) {
            profileUIActionHandler.invoke(UserProfileUIAction.DisabledSubscriberFloorClicked)
        } else {
            profileUIActionHandler.invoke(UserProfileUIAction.OnSubscribersListClicked)
        }
    }

    private fun handleShowSubscriptionsClick(isMe: Boolean) {
        if (isUserNotAllowShowFriendsSubscribers(isMe, model?.subscriptionCount)) {
            profileUIActionHandler.invoke(UserProfileUIAction.DisabledSubscriberFloorClicked)
        } else {
            profileUIActionHandler.invoke(UserProfileUIAction.OnSubscriptionsListClicked)
        }
    }

    // map any int to string +1..+99
    // 0   -> ""
    // 1   -> "+1"
    // 99  -> "+99"
    // 999 -> "+99"
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

