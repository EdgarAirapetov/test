package com.numplates.nomera3.modules.services.ui.viewholder

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.userpic.UserpicStoriesStateEnum
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraSearchRecentUserItemBinding
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiAction
import com.numplates.nomera3.modules.services.ui.entity.ServicesCommunityUiModel

class MeeraServicesCommunityViewHolder(
    private val binding: MeeraSearchRecentUserItemBinding,
    private val actionListener: (MeeraServicesUiAction) -> Unit
) : ViewHolder(binding.root) {

    fun bind(item: ServicesCommunityUiModel) {
        binding.apply {
            upiRecentUser.setConfig(UserpicUiModel(
                storiesState = UserpicStoriesStateEnum.NO_STORIES,
                userAvatarUrl = item.avatarUrl.ifEmpty { null },
                userAvatarRes = R.drawable.ic_empty_avatar,
                userAvatarErrorPlaceholder = R.drawable.ic_empty_avatar
            ))
            tvRecentUserName.text = item.name
            root.setThrottledClickListener { actionListener.invoke(MeeraServicesUiAction.CommunityClick(item.id)) }
        }
    }

}
