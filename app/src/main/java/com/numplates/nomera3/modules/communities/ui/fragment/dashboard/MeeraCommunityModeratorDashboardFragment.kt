package com.numplates.nomera3.modules.communities.ui.fragment.dashboard

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.meera.core.extensions.safeNavigate
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.communities.data.entity.CommunityUserRole
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityEditFragment
import com.numplates.nomera3.presentation.router.IArgContainer

/**
 * Экран "Управление сообществом" для модератора/админа сообщества
 * https://www.figma.com/file/oi39VGnKVAkMekn5CNrQG8/May-2021?node-id=201%3A86448
 * https://nomera.atlassian.net/wiki/spaces/NOM/pages/2288550188/-#Управление-сообществом
 * */
class MeeraCommunityModeratorDashboardFragment : MeeraBaseCommunityDashboardFragment() {

    /*
    * Если экран открыл модератор/админ сообщества,
    * то не отображать кнопку удалить сообщество
    * */
    override fun setExtraViewSettingsByRole() {
        changeDeleteVisibilityState(isVisible = false)
    }

    override fun openCommunityBlackListScreen(communityId: Int) {
        findNavController().safeNavigate(
            resId = R.id.action_meeraCommunityModeratorDashboardFragment_to_meeraCommunityBlacklistFragment,
            bundle = Bundle().apply {
                putInt(IArgContainer.ARG_GROUP_ID, communityId)
            }
        )
    }

    override fun deleteBtnClick() = Unit

    override fun openCommunityMemberListScreen() {
        findNavController().safeNavigate(
            resId = R.id.action_meeraCommunityModeratorDashboardFragment_to_meeraCommunityMembersContainerFragment,
            bundle = Bundle().apply {
                putInt(IArgContainer.ARG_GROUP_ID, viewModel.communityId)
                putInt(IArgContainer.ARG_COMMUNITY_USER_ROLE, CommunityUserRole.AUTHOR)
                putInt(IArgContainer.ARG_COMMUNITY_IS_PRIVATE, viewModel.isPrivateCommunity)
            }
        )
    }

    override fun openCommunityEditScreen() {
        CommunityEditFragment().apply {
            refreshCallback = {
                getCommunityInformation()
                dashboardCallback?.onCommunityInfoChanged()
            }
        }
        findNavController().safeNavigate(
            resId = R.id.action_meeraCommunityModeratorDashboardFragment_to_meeraCommunityEditFragment,
            bundle = Bundle().apply {
                putInt(IArgContainer.ARG_GROUP_ID, viewModel.communityId)
                putBoolean(IArgContainer.ARG_IS_GROUP_CREATOR, false)
            }
        )
    }
}
