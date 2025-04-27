package com.numplates.nomera3.modules.userprofile.ui.viewholder

import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraItemProfileContactSyncBinding
import com.numplates.nomera3.modules.userprofile.ui.entity.ProfileSuggestionUiModels
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction
import com.numplates.nomera3.presentation.view.holder.BaseItemViewHolder

private const val HORIZONTAL_PADDING = 6

class MeeraProfileContactSyncHolder(
    private val binding: MeeraItemProfileContactSyncBinding
) : BaseItemViewHolder<ProfileSuggestionUiModels.SuggestionSyncContactUiModel, MeeraItemProfileContactSyncBinding>(
    binding
) {

    private var profileUIActionHandler: ((UserProfileUIAction) -> Unit)? = null

    init {
        initListeners()
    }

    fun setProfileUIActionHandler(profileUIActionHandler: ((UserProfileUIAction) -> Unit)?) {
        this.profileUIActionHandler = profileUIActionHandler
    }

    fun clearResources() {
        binding.root.setOnClickListener(null)
    }

    override fun bind(item: ProfileSuggestionUiModels.SuggestionSyncContactUiModel) {
        super.bind(item)
        binding.tvContactSync.setCustomPadding(HORIZONTAL_PADDING, 0, HORIZONTAL_PADDING, 0)
    }

    private fun initListeners() {
        binding.root.setThrottledClickListener {
            profileUIActionHandler?.invoke(UserProfileUIAction.HandleNavigateSyncContactsUiAction)
        }
    }
}
