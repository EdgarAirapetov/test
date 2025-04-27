package com.numplates.nomera3.modules.services.ui.viewholder

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.MutualFriendsUiModel
import com.meera.uikit.widgets.buttons.ButtonType
import com.meera.uikit.widgets.groupusersrow.GroupUsersRowItemUiModel
import com.meera.uikit.widgets.people.TopAuthorApprovedUserModel
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemRecommendedPeopleBinding
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiAction
import com.numplates.nomera3.presentation.model.MutualFriendsUiEntity

class MeeraServicesRecommendedUserViewHolder(
    private val binding: MeeraItemRecommendedPeopleBinding,
    private val actionListener: (MeeraServicesUiAction) -> Unit
) : ViewHolder(binding.root) {

    fun bind(item: RecommendedPeopleUiEntity) {
        handleButtonState(item)
        setUserData(item)
        initListeners(item)
    }

    private fun setUserData(item: RecommendedPeopleUiEntity) {
        initMutualUsers(item)
        setPeopleName(item.userName)
        setTopContentApproved(item)
        handleCityUserCity(item)
        loadUserAvatar(item)
    }

    private fun setPeopleName(userName: String) {
        binding.tvRecommendedPeopleName.text = userName
    }

    private fun setTopContentApproved(item: RecommendedPeopleUiEntity) {
        binding.tvRecommendedPeopleName.enableTopContentAuthorApprovedUser(
            TopAuthorApprovedUserModel(
                item.isAccountApproved,
                item.topContentMaker
            )
        )
    }

    private fun handleCityUserCity(item: RecommendedPeopleUiEntity) {
        binding.tvRecommendedPeopleAgeCity.text = if (item.isAllowToShowAge) {
            item.fullUserAgeCity
        } else {
            item.userCity
        }
    }

    private fun loadUserAvatar(item: RecommendedPeopleUiEntity) {
        binding.upiRecommendedPeople.setConfig(
            UserpicUiModel(
            userAvatarUrl = item.userAvatarUrl
        )
        )
    }

    private fun handleButtonState(item: RecommendedPeopleUiEntity) {
        if (!item.hasFriendRequest) {
            setupItemCanSendFriendRequest()
        } else {
            setupItemFriendRequestSent()
        }
    }

    private fun setupItemCanSendFriendRequest() {
        binding.btnRecommendedPeopleAdd.apply {
            text = context.getString(R.string.user_personal_info_add_photo_text)
            buttonType = ButtonType.FILLED
            src = 0
        }
    }

    private fun setupItemFriendRequestSent() {
        binding.btnRecommendedPeopleAdd.apply {
            text = context.getString(R.string.group_join_request_sent)
            buttonType = ButtonType.TRANSPARENT
            src = R.drawable.ic_outlined_check_s
            updateContentColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.uiKitColorForegroundSecondary)))
        }
    }

    private fun initListeners(item: RecommendedPeopleUiEntity) {
        binding.btnRecommendedPeopleAdd.setThrottledClickListener {
            handleClickAddToFriend(item)
        }
        binding.root.setThrottledClickListener {
            actionListener.invoke(
                MeeraServicesUiAction.RecommendedUserClick(item)
            )
        }
        binding.btnCloseRelated.setThrottledClickListener {
            actionListener.invoke(MeeraServicesUiAction.RemoveRecommendedUserClick(item))
        }
    }

    private fun initMutualUsers(item: RecommendedPeopleUiEntity) {
        binding.vgMutualRecommendation.isVisible = item.mutualUsersEntity.mutualFriends.isNotEmpty()
        binding.vgMutualRecommendation.setState(
            model = mapMutualFriendsModel(item.mutualUsersEntity, item.totalMutualUsersCount)
        )
    }

    private fun mapMutualFriendsModel(src: MutualFriendsUiEntity, totalMutual: Int): MutualFriendsUiModel {
        return MutualFriendsUiModel(
            mutualUsers = src.mutualFriends.map { GroupUsersRowItemUiModel(it.avatarSmall) },
            mutualText = getMutualFriendsPlurals(totalMutual)
        )
    }

    private fun handleClickAddToFriend(item: RecommendedPeopleUiEntity) {
        val isUserSubscribed = item.hasFriendRequest
        actionListener.invoke(if (isUserSubscribed) {
            MeeraServicesUiAction.RemoveRecommendedUserFromFriendsClick(item)
        } else {
            MeeraServicesUiAction.AddRecommendedUserToFriendsClick(item)
        })
    }

    private fun getMutualFriendsPlurals(
        totalMutual: Int
    ) = binding.root.context.pluralString(R.plurals.user_mutual_subscription, totalMutual)

}
