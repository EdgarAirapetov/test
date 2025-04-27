package com.numplates.nomera3.modules.viewvideo.presentation.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.meera.core.extensions.gone
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.core.utils.TopAuthorApprovedUserModel
import com.meera.core.utils.enableTopContentAuthorApprovedUser
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewVideoHeaderBinding
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.feed.ui.entity.UserPost
import com.numplates.nomera3.modules.viewvideo.presentation.data.ViewVideoHeaderEvent
import com.numplates.nomera3.modules.viewvideo.presentation.data.ViewVideoHeaderUiModel

class ViewVideoHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private var eventListener: ((ViewVideoHeaderEvent) -> Unit)? = null

    private val binding = ViewVideoHeaderBinding.inflate(LayoutInflater.from(context), this)
    private var displayedHeader: ViewVideoHeaderUiModel? = null

    init {
        setClickListeners()
    }

    fun setEventListener(listener: ((ViewVideoHeaderEvent) -> Unit)?) {
        eventListener = listener
    }

    fun setHeaderInfo(headerModel: ViewVideoHeaderUiModel?) {
        if (headerModel != null) {
            setAvatar(headerModel.user)
            setName(headerModel.user)
            setSubscriptionStatus(headerModel)
        } else {
            gone()
        }
        displayedHeader = headerModel
    }

    private fun setClickListeners() {
        binding.vvViewVideoHeaderAvatar.setThrottledClickListener {
            eventListener?.invoke(ViewVideoHeaderEvent.UserClicked)
        }
        binding.tvViewVideoUserName.setThrottledClickListener {
            eventListener?.invoke(ViewVideoHeaderEvent.UserClicked)
        }
        binding.tvSubscribeButton.setThrottledClickListener {
            if (displayedHeader?.user?.blackListedByMe.isTrue()) return@setThrottledClickListener
            val isSubscribedToUser = displayedHeader?.isSubscribedToUser ?: return@setThrottledClickListener
            eventListener?.invoke(if (isSubscribedToUser) ViewVideoHeaderEvent.UnfollowClicked else ViewVideoHeaderEvent.FollowClicked)
        }
    }

    private fun setAvatar(user: UserPost) {
        if (checkIfAvatarAlreadySet(user)) return
        binding.vvViewVideoHeaderAvatar.setUp(
            context = context,
            avatarLink = user.avatarSmall,
            accountType = user.accountType.value,
            frameColor = user.accountColor
        )
    }

    private fun setName(user: UserPost) {
        initApprovedUser(user)
        binding.tvViewVideoUserName.text = user.name
    }

    private fun setSubscriptionStatus(headerModel: ViewVideoHeaderUiModel) {
        val subscriptionText = if (headerModel.isSubscribedToUser) R.string.post_read else R.string.post_start_follow
        binding.tvSubscribeButton.apply {
            setText(subscriptionText)
            if (headerModel.isShowFollowButton) visible() else gone()
        }
    }

    private fun initApprovedUser(user: UserPost) {
        binding.tvViewVideoUserName.enableTopContentAuthorApprovedUser(
            params = TopAuthorApprovedUserModel(
                    approved = user.approved.toBoolean(),
                    interestingAuthor = user.topContentMaker.toBoolean(),
                    isVip = user.accountType == AccountTypeEnum.ACCOUNT_TYPE_VIP
                        || user.accountType == AccountTypeEnum.ACCOUNT_TYPE_PREMIUM
            )
        )
    }

    private fun checkIfAvatarAlreadySet(user: UserPost): Boolean {
        return displayedHeader?.user?.avatarSmall == user.avatarSmall
            && displayedHeader?.user?.accountType == user.accountType
            && displayedHeader?.user?.accountColor == user.accountColor
    }
}
