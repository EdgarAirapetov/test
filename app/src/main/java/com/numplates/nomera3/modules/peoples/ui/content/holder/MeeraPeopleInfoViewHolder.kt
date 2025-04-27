package com.numplates.nomera3.modules.peoples.ui.content.holder

import android.view.View
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.string
import com.meera.uikit.widgets.buttons.ButtonType
import com.meera.uikit.widgets.people.ApprovedIconSize
import com.meera.uikit.widgets.people.TopAuthorApprovedUserModel
import com.meera.uikit.widgets.userpic.UserpicStoriesStateEnum
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemPeopleInfoBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeopleInfoUiEntity

class MeeraPeopleInfoViewHolder(
    private val binding: MeeraItemPeopleInfoBinding,
    private val actionListener: (actions: FriendsContentActions) -> Unit
) : BasePeoplesViewHolder<PeopleInfoUiEntity, MeeraItemPeopleInfoBinding>(binding) {

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
        binding.btnSubscribe.visibility = if (isVisible) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    private fun setSubscribeButtonEnabled(isEnabled: Boolean) {
        binding.btnSubscribe.isEnabled = isEnabled
    }

    private fun handleIsUserSubscribed(isUserSubscribed: Boolean) {
        binding.btnSubscribe.apply {
            if (isUserSubscribed) {
                text = binding.root.context.getString(R.string.reading)
                buttonType = ButtonType.OUTLINE
            } else {
                text = binding.root.context.getString(R.string.group_join)
                buttonType = ButtonType.FILLED
            }
        }
    }

    private fun setUserData(item: PeopleInfoUiEntity) {
        binding.tvPeopleName.text = item.userName
        setupUserAvatar(item)
    }

    private fun setupUserAvatar(item: PeopleInfoUiEntity) {
        val storiesState = when {
            item.hasMoments && item.hasNewMoments -> UserpicStoriesStateEnum.NEW
            item.hasMoments -> UserpicStoriesStateEnum.VIEWED
            else -> UserpicStoriesStateEnum.NO_STORIES
        }
        binding.upiPeopleAvatar.setConfig(
            UserpicUiModel(
                storiesState = storiesState,
                userAvatarUrl = item.imageUrl
            )
        )
    }

    private fun setApprovedName(item: PeopleInfoUiEntity) {
        binding.tvPeopleName.enableTopContentAuthorApprovedUser(
            params = TopAuthorApprovedUserModel(
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
        binding.btnSubscribe.setThrottledClickListener {
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
        binding.upiPeopleAvatar.setThrottledClickListener {
            val entity = item ?: return@setThrottledClickListener
            actionListener.invoke(FriendsContentActions.OnUserAvatarClicked(entity, binding.upiPeopleAvatar))
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
