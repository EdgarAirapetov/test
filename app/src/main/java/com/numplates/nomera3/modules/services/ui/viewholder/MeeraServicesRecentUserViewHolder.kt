package com.numplates.nomera3.modules.services.ui.viewholder

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.meera.core.extensions.clickAnimate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.uikit.widgets.userpic.UserpicStoriesStateEnum
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraSearchRecentUserItemBinding
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUserUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiAction

class MeeraServicesRecentUserViewHolder(
    private val binding: MeeraSearchRecentUserItemBinding,
    private val actionListener: (MeeraServicesUiAction) -> Unit
) : ViewHolder(binding.root) {

    fun bind(item: RecentUserUiModel) {
        binding.apply {
            root.setThrottledClickListener {
                upiRecentUser.clickAnimate()
                actionListener.invoke(MeeraServicesUiAction.UserClick(item.uid))
            }
            tvRecentUserName.text = item.name
            val storiesState = when {
                item.hasMoments && item.hasNewMoments -> UserpicStoriesStateEnum.NEW
                item.hasMoments -> UserpicStoriesStateEnum.VIEWED
                else -> UserpicStoriesStateEnum.NO_STORIES
            }
            upiRecentUser.setConfig(
                UserpicUiModel(
                    storiesState = storiesState,
                    userAvatarUrl = item.image,
                    userAvatarErrorPlaceholder = identifyAvatarByGender(item.gender)
                )
            )
        }
    }

    private fun identifyAvatarByGender(gender: Int?): Int {
        return if (gender.toBoolean()) {
            R.drawable.ic_man_avatar_placeholder
        } else {
            R.drawable.ic_woman_avatar_placeholder
        }
    }

}
