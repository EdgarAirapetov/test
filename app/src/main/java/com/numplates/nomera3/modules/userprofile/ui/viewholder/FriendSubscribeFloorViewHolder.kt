package com.numplates.nomera3.modules.userprofile.ui.viewholder

import android.content.res.ColorStateList
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import com.meera.core.extensions.click
import com.meera.core.extensions.font
import com.meera.core.extensions.gone
import com.meera.core.extensions.setTint
import com.meera.core.extensions.textColor
import com.meera.core.extensions.visible
import com.numplates.nomera3.FRIEND_STATUS_CONFIRMED
import com.numplates.nomera3.FRIEND_STATUS_INCOMING
import com.numplates.nomera3.FRIEND_STATUS_NONE
import com.numplates.nomera3.FRIEND_STATUS_OUTGOING
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityFriendSubscribeFloor
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction

class FriendSubscribeFloorViewHolder(
    parent: ViewGroup,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit
) : BaseUserViewHolder<UserEntityFriendSubscribeFloor>(parent, R.layout.item_friend_subscribe_floor) {

    private val ivAddFriend = itemView.findViewById<ImageView>(R.id.iv_add_friend)
    private val vgSubscribe = itemView.findViewById<LinearLayout>(R.id.vg_subscribe)
    private val tvSubscribe = itemView.findViewById<TextView>(R.id.tv_subscribe)
    private val ivSubscribe = itemView.findViewById<ImageView>(R.id.iv_subscribe)

    override fun bind(data: UserEntityFriendSubscribeFloor) {
        setupTextAndImages(data.friendStatus, data.isSubscribed, data.isUserBlacklisted)
        setupTheme(data.userStatus, data.isSubscribed)
        initClickListeners(
            friendStatus = data.friendStatus,
            isSubscribed = data.isSubscribed,
            userId = data.userId,
            approved = data.approved,
            influenecer = data.topContentMaker
        )
    }

    private fun setupTextAndImages(friendStatus: Int, isSubscribed: Boolean, isUserBlacklisted: Boolean) {
        if (isSubscribed) {
            tvSubscribe.setText(R.string.reading)
            ivSubscribe.visible()
        } else {
            tvSubscribe.setText(R.string.user_info_subscribe)
            ivSubscribe.gone()
        }

        val context = itemView.context
        val drawable = when {
            isUserBlacklisted -> {
                getDrawable(context, R.drawable.blocked_user_gray)
            }
            friendStatus == FRIEND_STATUS_NONE -> {
                getDrawable(context, R.drawable.ic_add_friend)
            }
            friendStatus == FRIEND_STATUS_OUTGOING -> {
                getDrawable(context, R.drawable.ic_friend_outgoing_request_btn_purple)
            }
            friendStatus == FRIEND_STATUS_INCOMING -> {
                getDrawable(context, R.drawable.ic_friend_incoming_request)
            }
            friendStatus == FRIEND_STATUS_CONFIRMED -> {
                getDrawable(context, R.drawable.ic_delete_friend)
            }
            else -> {
                getDrawable(context, R.drawable.ic_add_friend)
            }
        }
        ivAddFriend.setImageDrawable(drawable)
    }

    private fun initClickListeners(
        friendStatus: Int,
        isSubscribed: Boolean,
        userId: Long,
        approved: Boolean,
        influenecer: Boolean
    ) {
        ivAddFriend.click {
            profileUIActionHandler.invoke(UserProfileUIAction.OnFriendClicked())
        }
        vgSubscribe.click {
            profileUIActionHandler.invoke(UserProfileUIAction.OnSubscribeClicked(
                isSubscribed = isSubscribed,
                userId = userId,
                friendStatus = friendStatus,
                approved = approved,
                topContent = influenecer,
                message = if (friendStatus == FRIEND_STATUS_CONFIRMED) {
                    vgSubscribe.context.getString(R.string.subscribed_on_user)
                } else {
                    vgSubscribe.context.getString(R.string.subscribed_on_user_notif_on)
                }
            ))
        }
    }

    private fun setupTheme(userStatus: AccountTypeEnum, isSubscribed: Boolean) {
        when (userStatus) {
            AccountTypeEnum.ACCOUNT_TYPE_PREMIUM,
            AccountTypeEnum.ACCOUNT_TYPE_REGULAR,
            AccountTypeEnum.ACCOUNT_TYPE_UNKNOWN -> {
                ivAddFriend.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(itemView.context, R.color.tale_white)
                )
                ivAddFriend.setTint(R.color.ui_purple)
                ivSubscribe.setTint(R.color.ui_purple)
                if (isSubscribed) {
                    tvSubscribe.textColor(R.color.ui_purple)
                    tvSubscribe.font(R.font.source_sanspro_regular)
                    vgSubscribe.setBackgroundResource(R.drawable.bg_subscribe_regular_subscribed)
                } else {
                    tvSubscribe.textColor(R.color.ui_white)
                    tvSubscribe.font(R.font.source_sanspro_semibold)
                    vgSubscribe.setBackgroundResource(R.drawable.bg_subscribe_regular_not_subscribed)
                }
            }
            AccountTypeEnum.ACCOUNT_TYPE_VIP -> {
                ivAddFriend.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(itemView.context, R.color.ui_dark_gray_background)
                )
                ivAddFriend.setTint(R.color.ui_yellow)
                ivSubscribe.setTint(R.color.ui_yellow)
                if (isSubscribed) {
                    tvSubscribe.textColor(R.color.ui_yellow)
                    tvSubscribe.font(R.font.source_sanspro_regular)
                    vgSubscribe.setBackgroundResource(R.drawable.bg_subscribe_vip_subcribed)
                } else {
                    tvSubscribe.textColor(R.color.ui_black)
                    tvSubscribe.font(R.font.source_sanspro_semibold)
                    vgSubscribe.setBackgroundResource(R.drawable.bg_subscribe_vip_not_subscribed)
                }
            }
        }
    }

}

