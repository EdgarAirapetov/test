package com.numplates.nomera3.modules.userprofile.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.uikit.widgets.buttons.ButtonType
import com.meera.uikit.widgets.people.ApprovedIconSize
import com.meera.uikit.widgets.people.TopAuthorApprovedUserModel
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemProfileSuggestionBinding
import com.numplates.nomera3.modules.userprofile.ui.entity.ProfileSuggestionUiModels
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction

class MeeraProfileSuggestionViewHolder(
    private val binding: MeeraItemProfileSuggestionBinding
) : RecyclerView.ViewHolder(binding.root) {

    private var profileUIActionHandler: ((UserProfileUIAction) -> Unit)? = null

    fun bind(model: ProfileSuggestionUiModels.ProfileSuggestionUiModel) {
        with(binding) {
            upiAvatar.setConfig(
                UserpicUiModel(
                    userAvatarUrl = model.avatarLink,
                    userAvatarErrorPlaceholder = identifyAvatarByGender(model.gender)
                )
            )
            tvSuggestionName.text = model.name
            tvSuggestionName.enableTopContentAuthorApprovedUser(
                params = TopAuthorApprovedUserModel(
                    customIconTopContent = R.drawable.ic_filled_verified_flame_s_colored,
                    approvedIconSize = ApprovedIconSize.SMALL,
                    approved = model.isApproved,
                    interestingAuthor = model.isTopContentMaker
                )
            )
            val descriptionText = when {

                (model.isApproved || model.isTopContentMaker) && model.hasMutualFriends.not() -> {
                    root.context.getString(R.string.uniquename_prefix) + model.uniqueName
                }

                model.hasMutualFriends && model.mutualFriendsCount > MIN_MUTUAL_COUNT -> {
                    root.resources.getQuantityString(
                        R.plurals.plurals_mutual_friends, model.mutualFriendsCount, model.mutualFriendsCount
                    )
                }

                else -> model.cityName
            }
            tvDescription.text = descriptionText
            setupButton(model)
            root.setThrottledClickListener {
                profileUIActionHandler?.invoke(
                    UserProfileUIAction.OnSuggestionUserClicked(
                        isTopContentMaker = model.isTopContentMaker,
                        isApproved = model.isApproved,
                        hasMutualFriends = model.hasMutualFriends,
                        isSubscribed = model.isSubscribed,
                        toUserId = model.userId
                    )
                )
            }
        }
        initListeners(model)
    }

    fun setProfileUIActionHandler(profileUIActionHandler: ((UserProfileUIAction) -> Unit)?) {
        this.profileUIActionHandler = profileUIActionHandler
    }

    fun clearResources() {
        binding.btnSuggestionRemove.setOnClickListener(null)
        binding.btnPeopleSubscribe.setOnClickListener(null)
        binding.root.setOnClickListener(null)
        profileUIActionHandler = null
    }

    private fun identifyAvatarByGender(gender: Int?): Int {
        return if (gender.toBoolean()) {
            R.drawable.ic_man_avatar_placeholder
        } else {
            R.drawable.ic_woman_avatar_placeholder
        }
    }

    private fun setupButton(model: ProfileSuggestionUiModels.ProfileSuggestionUiModel) {
        setupButtonTheme(model)
        setupButtonText(model)
    }

    private fun setupButtonTheme(model: ProfileSuggestionUiModels.ProfileSuggestionUiModel) {
        with(binding.btnPeopleSubscribe) {
            when {
                model.isSubscribed -> {
                    buttonType = ButtonType.OUTLINE
                    setThrottledClickListener {
                        if (model.hasMutualFriends) {
                            profileUIActionHandler?.invoke(UserProfileUIAction.RemoveFriendSuggestion(model.userId))
                        } else {
                            profileUIActionHandler?.invoke(
                                UserProfileUIAction.UnsubscribeSuggestion(
                                    userId = model.userId,
                                    isApprovedUser = model.isApproved,
                                    topContentMaker = model.isTopContentMaker
                                )
                            )
                        }
                    }
                }

                !model.isSubscribed -> {
                    buttonType = ButtonType.FILLED
                    setThrottledClickListener {
                        if (model.hasMutualFriends) {
                            profileUIActionHandler?.invoke(
                                UserProfileUIAction.AddFriendSuggestion(
                                    userId = model.userId,
                                    isApprovedUser = model.isApproved,
                                    topContentMaker = model.isTopContentMaker
                                )
                            )
                        } else {
                            profileUIActionHandler?.invoke(
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
        with(binding.btnPeopleSubscribe) {
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
        binding.btnSuggestionRemove.setThrottledClickListener {
            profileUIActionHandler?.invoke(UserProfileUIAction.BlockSuggestionById(model.userId))
        }
    }

    companion object {
        const val MIN_MUTUAL_COUNT = 2
    }
}
