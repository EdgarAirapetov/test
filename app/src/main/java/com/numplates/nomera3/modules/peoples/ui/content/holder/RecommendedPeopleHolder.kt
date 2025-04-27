package com.numplates.nomera3.modules.peoples.ui.content.holder

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.meera.core.extensions.getDrawableCompat
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.string
import com.meera.core.extensions.textColor
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemRecommendedPeopleBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity
import com.numplates.nomera3.presentation.view.holder.BaseItemViewHolder

class RecommendedPeopleHolder(
    private val binding: ItemRecommendedPeopleBinding,
    private val actionListener: (FriendsContentActions) -> Unit
) : BaseItemViewHolder<RecommendedPeopleUiEntity, ItemRecommendedPeopleBinding>(binding) {

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
        handleCityUserCity(item)
        loadUserAvatar(item)
    }

    private fun setPeopleName(userName: String) {
        binding.tvRecommendedPeopleName.text = userName
    }

    private fun handleCityUserCity(item: RecommendedPeopleUiEntity) {
        if (item.isAllowToShowAge) {
            binding.tvRecommendedPeopleAgeCity.text = item.fullUserAgeCity
        } else {
            binding.tvRecommendedPeopleAgeCity.text = item.userCity
        }
    }

    private fun loadUserAvatar(item: RecommendedPeopleUiEntity) {
        Glide.with(binding.root.context)
            .load(item.userAvatarUrl)
            .centerCrop()
            .placeholder(R.drawable.fill_8)
            .into(binding.vvRecommendedPeople)
    }

    private fun handleButtonState(item: RecommendedPeopleUiEntity) {
        if (!item.hasFriendRequest) {
            setAddToFriendText(R.string.user_personal_info_add_photo_text)
            setAddToFriendBackground(R.drawable.background_rect_purple)
            setAddToFriendButtonTextColor(R.color.ui_white)
        } else {
            setAddToFriendText(R.string.group_join_request_sent)
            setAddToFriendButtonTextColor(R.color.ui_purple)
            setAddToFriendBackground(R.drawable.white_background_4_radius)
        }
    }

    private fun setAddToFriendButtonTextColor(@ColorRes color: Int) {
        binding.tvRecommendedPeopleAdd.textColor(color)
    }

    private fun setAddToFriendBackground(@DrawableRes drawableRes: Int) {
        binding.tvRecommendedPeopleAdd.background = binding.root.context.getDrawableCompat(drawableRes)
    }

    private fun setAddToFriendText(@StringRes textRes: Int) {
        val textResult = binding.root.context.string(textRes)
        binding.tvRecommendedPeopleAdd.text = textResult
    }

    private fun initListeners() {
        binding.tvRecommendedPeopleAdd.setThrottledClickListener {
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
        binding.ibCloseRelated.setThrottledClickListener {
            val userId = item?.userId ?: 0
            actionListener.invoke(FriendsContentActions.OnHideRelatedUserUiAction(userId))
        }
    }

    private fun initMutualUsers(item: RecommendedPeopleUiEntity) {
        binding.vgMutualRecommendation.isVisible = item.mutualUsersEntity.mutualFriends.isNotEmpty()
        binding.vgMutualRecommendation.setMutualFriends(
            friends = item.mutualUsersEntity,
            isShowDefaultMutualText = false
        )
        binding.vgMutualRecommendation.setText(getMutualFriendsPlurals(item.totalMutualUsersCount))
    }

    private fun handleClickAddToFriend() {
        val isUserSubscribed = item?.hasFriendRequest ?: false
        val userId = item?.userId ?: 0
        if (isUserSubscribed) {
            actionListener.invoke(
                FriendsContentActions.OnRecommendedUserRemoveFromFriendsClicked(userId)
            )
        } else {
            actionListener.invoke(
                FriendsContentActions.OnRecommendedUserAddToFriendClicked(
                    entity = item ?: return
                )
            )
        }
    }

    private fun getMutualFriendsPlurals(
        totalMutual: Int
    ) = binding.root.context.pluralString(R.plurals.user_mutual_subscription, totalMutual)
}
