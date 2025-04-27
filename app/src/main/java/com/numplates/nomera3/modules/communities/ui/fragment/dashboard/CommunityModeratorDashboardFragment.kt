package com.numplates.nomera3.modules.communities.ui.fragment.dashboard

import com.numplates.nomera3.Act
import com.numplates.nomera3.modules.communities.data.entity.CommunityUserRole
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityEditFragment
import com.numplates.nomera3.modules.communities.ui.fragment.members.CommunityMembersContainerFragment
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.IArgContainer
import com.meera.core.extensions.gone
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_COMMUNITY_IS_PRIVATE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_COMMUNITY_USER_ROLE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID

/**
 * Экран "Управление сообществом" для модератора/админа сообщества
 * https://www.figma.com/file/oi39VGnKVAkMekn5CNrQG8/May-2021?node-id=201%3A86448
 * https://nomera.atlassian.net/wiki/spaces/NOM/pages/2288550188/-#Управление-сообществом
 * */
class CommunityModeratorDashboardFragment : BaseCommunityDashboardFragment() {

    /*
    * Если экран открыл модератор/админ сообщества,
    * то не отображать кнопку удалить сообщество
    * */
    override fun setExtraViewSettingsByRole() {
        communityDeleteOptionContainerView?.gone()
    }

    override fun openCommunityMemberListScreen() {
        add(
            CommunityMembersContainerFragment(),
            Act.LIGHT_STATUSBAR,
            Arg(ARG_GROUP_ID, viewModel.communityId),
            Arg(ARG_COMMUNITY_USER_ROLE, CommunityUserRole.MODERATOR),
            Arg(ARG_COMMUNITY_IS_PRIVATE, viewModel.isPrivateCommunity)
        )
    }

    override fun openCommunityEditScreen() {
        val fragment =  CommunityEditFragment().apply {
            refreshCallback = {
                getCommunityInformation()
                dashboardCallback?.onCommunityInfoChanged()
            }
        }
        add(
           fragment,
            Act.LIGHT_STATUSBAR,
            Arg(IArgContainer.ARG_GROUP_ID, viewModel.communityId),
            Arg(IArgContainer.ARG_IS_GROUP_CREATOR, false)
        )
    }
}
