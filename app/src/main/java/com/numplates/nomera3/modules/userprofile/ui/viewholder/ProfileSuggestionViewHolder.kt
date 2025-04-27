package com.numplates.nomera3.modules.userprofile.ui.viewholder

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.meera.core.extensions.getDrawableCompat
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.textColor
import com.meera.core.utils.ApprovedIconSize
import com.meera.core.utils.TopAuthorApprovedUserModel
import com.meera.core.utils.enableTopContentAuthorApprovedUser
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemProfileSuggestionBinding
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.userprofile.ui.entity.ProfileSuggestionUiModels
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction

class ProfileSuggestionViewHolder(
    private val binding: ItemProfileSuggestionBinding,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit = { _ -> }
) : ViewHolder(binding.root) {

    fun bind(model: ProfileSuggestionUiModels.ProfileSuggestionUiModel) {
        with(binding) {
            Glide.with(binding.root.context)
                .load(model.avatarLink)
                .placeholder(R.drawable.fill_8_round)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.ivAvatar)
            tvName.text = model.name
            tvName.enableTopContentAuthorApprovedUser(
                params = TopAuthorApprovedUserModel(
                    isVip = model.accountType == AccountTypeEnum.ACCOUNT_TYPE_VIP,
                    customIconTopContent = R.drawable.ic_approved_author_gold_10,
                    approvedIconSize = ApprovedIconSize.SMALL,
                    approved = model.isApproved,
                    interestingAuthor = model.isTopContentMaker
                )
            )
            val descriptionText = if (model.hasMutualFriends) {
                root.resources.getQuantityString(
                    R.plurals.plurals_mutual_friends,
                    model.mutualFriendsCount,
                    model.mutualFriendsCount
                )
            } else {
                "@${model.uniqueName}"
            }
            tvDescription.text = descriptionText
            setupButton(model)
            val backgroundDrawable =
                if (model.isVip) R.drawable.bg_profile_suggestion_vip else R.drawable.bg_profile_suggestion
            val nameTextColor = if (model.isVip) R.color.ui_white else R.color.ui_black
            root.background = root.context.getDrawableCompat(backgroundDrawable)
            val icClose = if (model.isVip) R.drawable.ic_close_gold_16 else R.drawable.ic_close_purple_16
            tvName.textColor(nameTextColor)
            root.setThrottledClickListener {
                profileUIActionHandler(
                    UserProfileUIAction.OnSuggestionUserClicked(
                        isTopContentMaker = model.isTopContentMaker,
                        isApproved = model.isApproved,
                        hasMutualFriends = model.hasMutualFriends,
                        isSubscribed = model.isSubscribed,
                        toUserId = model.userId
                    )
                )
            }
            ibSuggestionRemove.setImageDrawable(root.context.getDrawableCompat(icClose))
        }
        initListeners(model)
    }

    private fun setupButton(model: ProfileSuggestionUiModels.ProfileSuggestionUiModel) {
        setupButtonTheme(model)
        setupButtonText(model)
    }

    private fun setupButtonTheme(model: ProfileSuggestionUiModels.ProfileSuggestionUiModel) {
        with(binding.tvPeopleSubscribe) {
            when {
                !model.isVip && model.isSubscribed -> {
                    background = context.getDrawableCompat(R.drawable.background_grey_4r)
                    textColor(R.color.ui_purple)
                    setThrottledClickListener {
                        if (model.hasMutualFriends) {
                            profileUIActionHandler(UserProfileUIAction.RemoveFriendSuggestion(model.userId))
                        } else {
                            profileUIActionHandler(
                                UserProfileUIAction.UnsubscribeSuggestion(
                                    userId = model.userId,
                                    isApprovedUser = model.isApproved,
                                    topContentMaker = model.isTopContentMaker
                                )
                            )
                        }
                    }
                }
                !model.isVip && !model.isSubscribed -> {
                    background = context.getDrawableCompat(R.drawable.background_rect_purple)
                    textColor(R.color.ui_white)
                    setThrottledClickListener {
                        if (model.hasMutualFriends) {
                            profileUIActionHandler(
                                UserProfileUIAction.AddFriendSuggestion(
                                    userId = model.userId,
                                    isApprovedUser = model.isApproved,
                                    topContentMaker = model.isTopContentMaker
                                )
                            )
                        } else {
                            profileUIActionHandler(
                                UserProfileUIAction.SubscribeSuggestion(
                                    userId = model.userId,
                                    isApprovedUser = model.isApproved,
                                    topContentMaker = model.isTopContentMaker
                                )
                            )
                        }
                    }
                }
                model.isVip && model.isSubscribed -> {
                    background = context.getDrawableCompat(R.drawable.background_post_action_bar_vip_button)
                    textColor(R.color.vip_gold)
                    setThrottledClickListener {
                        if (model.hasMutualFriends) {
                            profileUIActionHandler(UserProfileUIAction.RemoveFriendSuggestion(model.userId))
                        } else {
                            profileUIActionHandler(
                                UserProfileUIAction.UnsubscribeSuggestion(
                                    userId = model.userId,
                                    isApprovedUser = model.isApproved,
                                    topContentMaker = model.isTopContentMaker
                                )
                            )
                        }
                    }
                }
                model.isVip && !model.isSubscribed -> {
                    background = context.getDrawableCompat(R.drawable.bg_profile_suggestion_action_vip)
                    textColor(R.color.ui_black)
                    setThrottledClickListener {
                        if (model.hasMutualFriends) {
                            profileUIActionHandler(
                                UserProfileUIAction.AddFriendSuggestion(
                                    userId = model.userId,
                                    isApprovedUser = model.isApproved,
                                    topContentMaker = model.isTopContentMaker
                                )
                            )
                        } else {
                            profileUIActionHandler(
                                UserProfileUIAction.SubscribeSuggestion(
                                    userId = model.userId,
                                    isApprovedUser = model.isApproved,
                                    topContentMaker = model.isTopContentMaker
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setupButtonText(model: ProfileSuggestionUiModels.ProfileSuggestionUiModel) {
        with(binding.tvPeopleSubscribe) {
            when {
                model.hasMutualFriends && model.isSubscribed -> {
                    text = context.getString(R.string.request_send)
                }
                !model.hasMutualFriends && model.isSubscribed -> {
                    text = context.getString(R.string.reading)
                }
                model.hasMutualFriends && !model.isSubscribed -> {
                    text = context.getString(R.string.general_add)
                }
                !model.hasMutualFriends && !model.isSubscribed -> {
                    text = context.getString(R.string.general_subscribe)
                }
            }
        }
    }

    private fun initListeners(model: ProfileSuggestionUiModels.ProfileSuggestionUiModel) {
        binding.ibSuggestionRemove.setThrottledClickListener {
            profileUIActionHandler(UserProfileUIAction.BlockSuggestionById(model.userId))
        }
    }

}
