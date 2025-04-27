package com.numplates.nomera3.modules.communities.ui.fragment.dashboard

import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.communities.data.entity.CommunityUserRole
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityEditFragment
import com.numplates.nomera3.modules.communities.ui.fragment.members.CommunityMembersContainerFragment
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder
import com.meera.core.extensions.click
import com.meera.core.extensions.visible
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID

/**
 * Экран "Управление сообществом" для создателя сообщества
 * https://www.figma.com/file/oi39VGnKVAkMekn5CNrQG8/May-2021?node-id=202%3A85028
 * https://nomera.atlassian.net/wiki/spaces/NOM/pages/2288550188/-#Управление-сообществом
 * */
class CommunityCreatorDashboardFragment : BaseCommunityDashboardFragment() {

    /*
    * Если экран открыл создатель сообщества,
    * то отобразить кнопку удалить сообщество
    * */
    override fun setExtraViewSettingsByRole() {
        initDeleteCommunityOptionView()
    }

    override fun openCommunityMemberListScreen() {
        add(
            CommunityMembersContainerFragment(),
            Act.LIGHT_STATUSBAR,
            Arg(ARG_GROUP_ID, viewModel.communityId),
            Arg(IArgContainer.ARG_COMMUNITY_USER_ROLE, CommunityUserRole.AUTHOR),
            Arg(IArgContainer.ARG_COMMUNITY_IS_PRIVATE, viewModel.isPrivateCommunity)
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
            Arg(IArgContainer.ARG_IS_GROUP_CREATOR, true)
        )
    }

    private fun initDeleteCommunityOptionView() {
        communityDeleteOptionContainerView?.visible()
        communityDeleteOptionContainerView?.click {
            showDeleteCommunityConfirmationDialog()
        }
    }

    private fun showDeleteCommunityConfirmationDialog() {
        ConfirmDialogBuilder()
            .setHeader(getString(R.string.delete_community_confirmation_dialog_header))
            .setDescription(getString(R.string.delete_community_confirmation_dialog_description))
            .setRightBtnText(getString(R.string.delete_community_confirmation_dialog_action_btn_text))
            .setRightClickListener { onCommunityDeletionSuccess() }
            .setLeftBtnText(getString(R.string.delete_community_confirmation_dialog_cancel_btn_text))
            .show(childFragmentManager)
    }
}
