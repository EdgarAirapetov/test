package com.numplates.nomera3.modules.userprofile.ui.viewholder

import com.meera.core.extensions.getDrawableCompat
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.textColor
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemProfileContactSyncBinding
import com.numplates.nomera3.modules.userprofile.ui.entity.ProfileSuggestionUiModels
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction
import com.numplates.nomera3.presentation.view.holder.BaseItemViewHolder

class ProfileContactSyncHolder(
    private val binding: ItemProfileContactSyncBinding,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit = { _ -> }
) : BaseItemViewHolder<ProfileSuggestionUiModels.SuggestionSyncContactUiModel, ItemProfileContactSyncBinding>(binding) {

    init {
        initListeners()
    }

    override fun bind(item: ProfileSuggestionUiModels.SuggestionSyncContactUiModel) {
        super.bind(item)
        handleThemeByAccountType(item.isUserVip)
    }

    private fun handleThemeByAccountType(isVip: Boolean) = with(binding) {
        val backgroundDrawable =
            if (isVip) R.drawable.bg_profile_suggestion_vip else R.drawable.bg_profile_suggestion
        val nameTextColor = if (isVip) R.color.ui_white else R.color.ui_black
        val buttonStyle = if (isVip) root.context.getDrawableCompat(R.drawable.bg_profile_suggestion_action_vip) else
            root.context.getDrawableCompat(R.drawable.background_rect_purple)
        val buttonTextColor = if (isVip) R.color.ui_black else R.color.ui_white
        tvContactSyncLabel.textColor(nameTextColor)
        tvContactSync.textColor(buttonTextColor)
        root.background = root.context.getDrawableCompat(backgroundDrawable)
        tvContactSync.background = buttonStyle
    }

    private fun initListeners() {
        binding.root.setThrottledClickListener {
            profileUIActionHandler(UserProfileUIAction.HandleNavigateSyncContactsUiAction)
        }
    }
}
