package com.numplates.nomera3.modules.communities.ui.fragment.dashboard.holder

import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraCommunitySettingsUserListItemBinding
import com.numplates.nomera3.modules.communities.ui.entity.CommunityInformationScreenUIModel
import com.numplates.nomera3.modules.communities.ui.fragment.dashboard.MeeraBaseCommunityDashboardAction

class MeeraBaseCommunitySettingsUsersHolder(
    private val binding: MeeraCommunitySettingsUserListItemBinding,
    private val clickListener: (listener: MeeraBaseCommunityDashboardAction) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(settingsModel: CommunityInformationScreenUIModel?) {
        binding.apply {
            vBandMembers.cellCityText = false
            settingsModel?.communityMembersCount?.let {
                vBandMembers.setRightTextboxValue(it)
            }
            vBandMembers.setRightElementContainerClickable(false)
            vBandMembers.setThrottledClickListener {
                clickListener.invoke(MeeraBaseCommunityDashboardAction.Users(
                    clickableState = {
                        vBandMembers.isClickable = it
                    },
                    progressBarState = {
                        if (it) pbUsers.visible() else pbUsers.gone()
                    }
                ))
            }
            vBlackList.cellCityText = false
            settingsModel?.communityMembersCount?.let {
                vBlackList.setTitleValue(
                    binding.root.context?.getString(R.string.community_blacklist_text) ?: ""
                )
            }
            vBlackList.setRightElementContainerClickable(false)
            vBlackList.setThrottledClickListener {
                clickListener.invoke(MeeraBaseCommunityDashboardAction.BlackList(
                    clickableState = {
                        vBlackList.isClickable = it
                    },
                    progressBarState = {
                        if (it) pbBlackList.visible() else pbBlackList.gone()
                    }
                ))
            }
        }
    }
}
