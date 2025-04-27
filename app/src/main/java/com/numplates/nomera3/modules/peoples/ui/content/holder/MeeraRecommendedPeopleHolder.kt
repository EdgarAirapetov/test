package com.numplates.nomera3.modules.peoples.ui.content.holder

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.meera.core.extensions.isEllipsized
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.MutualFriendsUiModel
import com.meera.uikit.widgets.buttons.ButtonType
import com.meera.uikit.widgets.dp
import com.meera.uikit.widgets.groupusersrow.GroupUsersRowItemUiModel
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemRecommendedPeopleBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity
import com.numplates.nomera3.presentation.model.MutualFriendsUiEntity
import com.numplates.nomera3.presentation.view.holder.BaseItemViewHolder


private const val PADDING_ICON = 4

class MeeraRecommendedPeopleHolder(
    private val binding: MeeraItemRecommendedPeopleBinding,
    private val actionListener: (FriendsContentActions) -> Unit
) : BaseItemViewHolder<RecommendedPeopleUiEntity, MeeraItemRecommendedPeopleBinding>(binding) {

    init {
        initListeners()
    }

    override fun bind(item: RecommendedPeopleUiEntity) {
        super.bind(item)
        handleButtonState(item)
        setUserData(item)
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
        binding.tvRecommendedPeopleName.apply {
            post {
                val padding = if (isEllipsized()) 0.dp else PADDING_ICON.dp
                enableApprovedIcon(
                    enabled = item.isAccountApproved || item.topContentMaker,
                    topContentMaker = item.topContentMaker,
                    padding = padding
                )
            }
        }
    }

    private fun handleCityUserCity(item: RecommendedPeopleUiEntity) {
        binding.tvRecommendedPeopleAgeCity.text = if (item.isAllowToShowAge) {
            item.fullUserAgeCity
        } else {
            item.userCity
        }
    }

    private fun loadUserAvatar(item: RecommendedPeopleUiEntity) {
        binding.upiRecommendedPeople.setConfig(UserpicUiModel(
            userAvatarUrl = item.userAvatarUrl
        ))
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

    private fun initListeners() {
        binding.btnRecommendedPeopleAdd.setThrottledClickListener {
            handleClickAddToFriend()
        }
        binding.root.setThrottledClickListener {
            val entity = item ?: return@setThrottledClickListener
            actionListener.invoke(
                FriendsContentActions.OnRelatedUserClicked(
                    entity = entity
                )
            )
        }
        binding.btnCloseRelated.setThrottledClickListener {
            val userId = item?.userId ?: 0
            actionListener.invoke(FriendsContentActions.OnHideRelatedUserUiAction(userId))
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

    private fun handleClickAddToFriend() {
        val isUserSubscribed = item?.hasFriendRequest ?: false
        val userId = item?.userId ?: 0
        actionListener.invoke(if (isUserSubscribed) {
            FriendsContentActions.OnRecommendedUserRemoveFromFriendsClicked(userId)
        } else {
            FriendsContentActions.OnRecommendedUserAddToFriendClicked(
                entity = item ?: return
            )
        })
    }

    private fun getMutualFriendsPlurals(
        totalMutual: Int
    ) = binding.root.context.pluralString(R.plurals.user_mutual_subscription, totalMutual)
}
