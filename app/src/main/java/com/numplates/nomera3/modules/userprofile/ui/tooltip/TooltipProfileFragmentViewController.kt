package com.numplates.nomera3.modules.userprofile.ui.tooltip

import android.graphics.Rect
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.lifecycle.whenResumed
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.getScreenWidth
import com.meera.core.extensions.isOnTheScreen
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentUserInfoBinding
import com.numplates.nomera3.modules.user.ui.event.UserProfileTooltipEffect
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import com.numplates.nomera3.presentation.view.utils.apphints.createTooltip
import com.numplates.nomera3.presentation.view.utils.apphints.createTooltipMatchParent
import com.numplates.nomera3.presentation.view.utils.apphints.showAboveView
import com.numplates.nomera3.presentation.view.utils.apphints.showBelowView
import com.numplates.nomera3.presentation.view.utils.apphints.showCreateAvatarAtUserInfo
import com.numplates.nomera3.presentation.view.utils.apphints.showForUserInfoSubscribers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class TooltipProfileFragmentViewController(
    private val fragment: UserInfoFragment,
    private val binding: FragmentUserInfoBinding?
) {
    private val tooltipScope: CoroutineScope = MainScope()

    var canShow: Boolean = true

    // unique name tooltip
    private val uniqueNameTooltip: PopupWindow? by lazy {
        val tooltip = createTooltip(fragment.context, R.layout.tooltip_about_unique_name)
        setOfTooltip.add(tooltip)
        tooltip
    }

    // unique name copy tooltip
    private val uniqueNameCopiedTooltip: PopupWindow? by lazy {
        val tooltip = createTooltip(fragment.context, R.layout.tooltip_copy_unique_name)
        setOfTooltip.add(tooltip)
        tooltip
    }

    private val createAvatarTooltip: PopupWindow? by lazy {
        val tooltip = createTooltip(fragment.context, R.layout.tooltip_create_avatar_userinfo)
        setOfTooltip.add(tooltip)
        tooltip
    }

    private val subscribersCountTooltip: PopupWindow? by lazy {
        val tooltip = createTooltipMatchParent(fragment.context, R.layout.subscribers_count_tooltip)
        setOfTooltip.add(tooltip)
        tooltip
    }

    private val createReferralTooltip: PopupWindow? by lazy {
        val tooltip = createTooltip(fragment.context, R.layout.tooltip_referral_friends_user_info)
        setOfTooltip.add(tooltip)
        tooltip
    }

    private val peopleTopContentMakerTooltip: PopupWindow? by lazy {
        val tooltip = createTooltip(fragment.context, R.layout.tooltip_profile_top_content_maker)
        setOfTooltip.add(tooltip)
        tooltip
    }

    private val setOfTooltip = mutableSetOf<PopupWindow?>()

    fun handleTooltipEvent(event: UserProfileTooltipEffect) {
        Timber.d("handleTooltipEvent event = ${event}")
        when (event) {
            UserProfileTooltipEffect.ShowAvatarCreateTooltip -> showCreateAvatarTooltip()
            is UserProfileTooltipEffect.ShowReferralTooltip -> showReferralTooltip(event.position)
            UserProfileTooltipEffect.ShowUniqueNameTooltip -> showUniqueNameTooltip()
            UserProfileTooltipEffect.ShowUserSubscribersTooltip -> showUserSubscribersTooltip()
            UserProfileTooltipEffect.ShowUserTopMarkerTooltip -> showUserTopMakerTooltip()
            UserProfileTooltipEffect.ShowUniqueNameTooltipCopied -> showUniqueNameCopiedTooltip()
        }
    }

    fun hideHints() {
        tooltipScope.coroutineContext.cancelChildren()
    }

    private fun showCreateAvatarTooltip() {
        tooltipScope.launch {
            fragment.whenResumed {
                binding?.ivPhoto?.let { view ->
                    createAvatarTooltip?.showCreateAvatarAtUserInfo(
                        fragment = fragment,
                        view = view,
                        offsetY = CREATE_AVATAR_TOOLTIP_OFFSET_Y.dp,
                        offsetX = CREATE_AVATAR_TOOLTIP_OFFSET_X.dp
                    )
                }
                delay(TooltipDuration.COMMON_END_DELAY)
            }
        }.invokeOnCompletion {
            createAvatarTooltip?.dismiss()
        }
    }

    private fun showUserTopMakerTooltip() {
        tooltipScope.launch {
            fragment.whenResumed {
                peopleTopContentMakerTooltip?.dismiss()
                delay(TooltipDuration.COMMON_START_DELAY)
                binding?.nickname?.let { nameView ->
                    val tooltipWidth = (peopleTopContentMakerTooltip?.contentView?.measuredWidth ?: 0)
                    val offsetX = getTextPaintWidth() - USER_NAME_DRAWABLE_PADDING_DP.dp
                    var additionalOffset = 0
                    if (tooltipWidth / 2 > offsetX) {
                        peopleTopContentMakerTooltip?.contentView?.rootView?.setBackgroundResource(R.drawable.ic_tooltip_bottom_left)
                        additionalOffset = (tooltipWidth.toFloat() * TOOLTIP_ARROW_OFFSET_X).toInt()
                    } else if ((tooltipWidth / 2 + nameView.x + nameView.width) > getScreenWidth()) {

                        peopleTopContentMakerTooltip?.contentView?.rootView?.setBackgroundResource(R.drawable.ic_tooltip_bottom_right)
                        additionalOffset = -(tooltipWidth.toFloat() * TOOLTIP_ARROW_OFFSET_X).toInt()
                    } else {
                        peopleTopContentMakerTooltip?.contentView?.rootView?.setBackgroundResource(R.drawable.ic_tooltip_bottom_center)
                        additionalOffset = 0
                    }

                    peopleTopContentMakerTooltip?.showAboveView(
                        fragment = fragment,
                        view = nameView,
                        offsetX = offsetX + additionalOffset,
                        offsetY = (-USER_NAME_DRAWABLE_PADDING_Y).dp
                    )
                    delay(TooltipDuration.SELECT_COMMUNITY_TOOLTIP_DURATION)
                }
            }
        }.invokeOnCompletion {
            peopleTopContentMakerTooltip?.dismiss()
        }
    }

    private fun showUserSubscribersTooltip() {
        tooltipScope.launch {
            fragment.whenResumed {
                binding?.mmpMomentsPreview?.also {
                    subscribersCountTooltip?.showForUserInfoSubscribers(
                        fragment = fragment,
                        view = it,
                        offsetY = -(SUBSCRIBER_COUNT_TOOLTIP_OFFSET_Y.dp)
                    )
                }
                delay(TooltipDuration.COMMON_END_DELAY)
            }
        }.invokeOnCompletion {
            subscribersCountTooltip?.dismiss()
        }
    }

    private fun showUniqueNameCopiedTooltip() {
        tooltipScope.launch {
            fragment.whenResumed {
                binding?.rvDescr?.let { rvDescr ->
                    uniqueNameCopiedTooltip?.showAboveView(
                        fragment = fragment,
                        view = rvDescr
                    )
                }
                delay(TooltipDuration.UNIQUE_NAME_COPIED)
            }
        }.invokeOnCompletion {
            uniqueNameCopiedTooltip?.dismiss()
        }
    }

    private fun showUniqueNameTooltip() {
        tooltipScope.launch {
            fragment.whenResumed {
                binding?.rvDescr?.let {
                    uniqueNameTooltip?.showAboveView(
                        fragment,
                        it,
                        offsetY = SHOW_UNIQUE_NAME_TOOLTIP_OFFSET_Y.dp
                    )
                }
                delay(TooltipDuration.UNIQUE_NAME)
            }
        }.invokeOnCompletion {
            uniqueNameTooltip?.dismiss()
        }
    }

    private fun showReferralTooltip(position: Int) {
        tooltipScope.launch {
            fragment.whenResumed {
                val viewHolder = binding?.rvContent?.findViewHolderForAdapterPosition(position)?.itemView
                viewHolder?.findViewById<LinearLayout>(R.id.ll_friend_btn)?.let { tipView ->
                    if (tipView.isOnTheScreen() && canShow) {
                        createReferralTooltip?.showBelowView(
                            fragment = fragment,
                            view = tipView,
                            offsetX = tipView.height / 2,
                            gravityModifier = Gravity.START
                        )
                        delay(TooltipDuration.CREATE_GROUP_CHAT)
                    }
                }
            }
        }.invokeOnCompletion {
            createReferralTooltip?.dismiss()
        }
    }

    private fun getTextPaintWidth(): Int {
        val rect = Rect()
        binding?.nickname?.apply {
            val ellipsisCount = layout.getEllipsisCount(0)
            val postFix = if (ellipsisCount > 0) ELLIPSIS else String.empty()
            val shownText = text.toString().substring(0, text.length - ellipsisCount) + postFix
            paint.getTextBounds(shownText, 0, shownText.length - 1, rect)
        }
        return rect.width()
    }

    companion object {
        private const val USER_NAME_DRAWABLE_PADDING_DP = 41
        private const val USER_NAME_DRAWABLE_PADDING_Y = 5
        private const val SUBSCRIBER_COUNT_TOOLTIP_OFFSET_Y = 24
        private const val CREATE_AVATAR_TOOLTIP_OFFSET_Y = -10
        private const val CREATE_AVATAR_TOOLTIP_OFFSET_X = -5
        private const val SHOW_UNIQUE_NAME_TOOLTIP_OFFSET_Y = -8
        private const val TOOLTIP_ARROW_OFFSET_X = 45f / 139f
        private const val ELLIPSIS = "..."
    }
}
