package com.numplates.nomera3.modules.peoples.ui.content.holder

import android.view.View
import com.meera.core.extensions.getDrawableCompat
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.string
import com.meera.core.extensions.textColor
import com.meera.core.utils.ApprovedIconSize
import com.meera.core.utils.TopAuthorApprovedUserModel
import com.meera.core.utils.enableTopContentAuthorApprovedUser
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemPeopleInfoBinding
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeopleInfoUiEntity

class PeopleInfoViewHolder(
    private val binding: ItemPeopleInfoBinding,
    private val actionListener: (actions: FriendsContentActions) -> Unit
) : BasePeoplesViewHolder<PeopleInfoUiEntity, ItemPeopleInfoBinding>(binding) {

    init {
        initListeners()
    }

    override fun bind(item: PeopleInfoUiEntity) {
        super.bind(item)
        setApprovedName(item)
        handleButtonState(item)
        setUserData(item)
        handleSubscribersState(item)
    }

    private fun handleButtonState(item: PeopleInfoUiEntity) {
        setSubscribeButtonVisibility(!item.isMe)
        setSubscribeButtonEnabled(!item.isMe)
        if (!item.isMe) handleIsUserSubscribed(item.isUserSubscribed)
    }

    private fun setSubscribeButtonVisibility(isVisible: Boolean) {
        binding.tvPeopleSubscribe.visibility = if (isVisible) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    private fun setSubscribeButtonEnabled(isEnabled: Boolean) {
        binding.tvPeopleSubscribe.isEnabled = isEnabled
    }

    private fun handleIsUserSubscribed(isUserSubscribed: Boolean) {
        if (isUserSubscribed) {
            binding.tvPeopleSubscribe.text = binding.root.context.getString(R.string.reading)
            binding.tvPeopleSubscribe.background = binding.root.context.getDrawableCompat(R.drawable.background_grey_4r)
            binding.tvPeopleSubscribe.textColor(R.color.ui_purple)
        } else {
            binding.tvPeopleSubscribe.text = binding.root.context.getString(R.string.group_join)
            binding.tvPeopleSubscribe.background =
                binding.root.context.getDrawableCompat(R.drawable.background_rect_purple)
            binding.tvPeopleSubscribe.textColor(R.color.ui_white)
        }
    }

    private fun setUserData(item: PeopleInfoUiEntity) {
        binding.tvPeopleName.text = item.userName

        binding.ivPeopleAvatar.setUp(
            binding.root.context,
            item.imageUrl,
            item.accountType.value,
            0,
            hasShadow = false,
            hasMoments = item.hasMoments,
            hasNewMoments = item.hasNewMoments,
        )
    }

    private fun setApprovedName(item: PeopleInfoUiEntity) {
        binding.tvPeopleName.enableTopContentAuthorApprovedUser(
            params = TopAuthorApprovedUserModel(
                isVip = item.accountType == AccountTypeEnum.ACCOUNT_TYPE_VIP,
                customIconTopContent = R.drawable.ic_approved_author_gold_10,
                approvedIconSize = ApprovedIconSize.SMALL,
                approved = item.isApprovedAccount,
                interestingAuthor = item.isInterestingAuthor
            )
        )
    }

    private fun initListeners() {
        binding.root.setThrottledClickListener {
            val entity = item ?: return@setThrottledClickListener
            actionListener.invoke(FriendsContentActions.OnUserClicked(entity))
        }
        binding.tvPeopleSubscribe.setThrottledClickListener {
            val currentUser = item ?: return@setThrottledClickListener
            val isSubscribed = item?.isUserSubscribed ?: false
            if (isSubscribed) {
                actionListener.invoke(
                    FriendsContentActions.OnBloggerUnSubscribeClicked(currentUser)
                )
            } else {
                actionListener.invoke(
                    FriendsContentActions.OnBloggerSubscribeClicked(currentUser)
                )
            }
        }
        binding.ivPeopleAvatar.setThrottledClickListener {
            val entity = item ?: return@setThrottledClickListener
            actionListener.invoke(FriendsContentActions.OnUserAvatarClicked(entity, binding.ivPeopleAvatar))
        }
    }

    private fun handleSubscribersState(item: PeopleInfoUiEntity) {
        when {
            !item.isMe -> setUserUniqueName(item.uniqueName)
            else -> binding.tvPeopleUniqname.text = binding.root.context.string(R.string.people_its_you)
        }
    }

    private fun setUserUniqueName(uniqueName: String) {
        binding.tvPeopleUniqname.text =
            "${binding.root.context.getString(R.string.uniquename_prefix)}$uniqueName"
    }
}
