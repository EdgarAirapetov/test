package com.numplates.nomera3.modules.services.ui.viewholder

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.meera.core.extensions.longClick
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.people.ApprovedIconSize
import com.meera.uikit.widgets.people.TopAuthorApprovedUserModel
import com.meera.uikit.widgets.userpic.UserpicStoriesStateEnum
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemServicesUserBinding
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiAction
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUserUiModel

class MeeraServicesUserViewHolder(
    private val binding: MeeraItemServicesUserBinding,
    private val actionListener: (MeeraServicesUiAction) -> Unit
) : ViewHolder(binding.root) {

    fun bind(item: MeeraServicesUserUiModel) {
        setupText(item)
        setupAvatar(item)
        setupListeners(item)
    }

    private fun setupText(item: MeeraServicesUserUiModel) {
        binding.unvServicesUser.text = item.userName
        binding.unvServicesUser.enableTopContentAuthorApprovedUser(
            params = TopAuthorApprovedUserModel(
                customIconTopContent = R.drawable.ic_approved_author_gold_10,
                approvedIconSize = ApprovedIconSize.SMALL,
                approved = item.approved,
                interestingAuthor = item.interestingAuthor
            )
        )
        binding.tvServicesUserUsername.text = StringBuilder("@").append(item.uniqueName).toString()
    }

    private fun setupAvatar(item: MeeraServicesUserUiModel) {
        binding.upiServicesUser.setConfig(
            config = UserpicUiModel(
                storiesState = item.storiesStateEnum,
                userAvatarUrl = item.avatarUrl
            )
        )
    }

    private fun setupListeners(item: MeeraServicesUserUiModel) {
        binding.root.setThrottledClickListener { actionListener.invoke(MeeraServicesUiAction.UserClick(item.id)) }
        if (item.storiesStateEnum != UserpicStoriesStateEnum.NO_STORIES) {
            binding.upiServicesUser.setThrottledClickListener {
                actionListener.invoke(MeeraServicesUiAction.UserMomentClick(item.id, binding.upiServicesUser))
            }
            binding.upiServicesUser.longClick {
                actionListener.invoke(MeeraServicesUiAction.UserClick(item.id))
            }
        } else {
            binding.upiServicesUser.setThrottledClickListener {
                actionListener.invoke(MeeraServicesUiAction.UserClick(item.id))
            }
        }
    }

}
