package com.numplates.nomera3.modules.communities.ui.fragment.dashboard

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.safeNavigate
import com.meera.uikit.widgets.buttons.ButtonType
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.communities.data.entity.CommunityUserRole
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityEditFragment
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID

/**
 * Экран "Управление сообществом" для создателя сообщества
 * https://www.figma.com/file/oi39VGnKVAkMekn5CNrQG8/May-2021?node-id=202%3A85028
 * https://nomera.atlassian.net/wiki/spaces/NOM/pages/2288550188/-#Управление-сообществом
 * */
class MeeraCommunityCreatorDashboardFragment : MeeraBaseCommunityDashboardFragment() {

    /*
    * Если экран открыл создатель сообщества,
    * то отобразить кнопку удалить сообщество
    * */
    override fun setExtraViewSettingsByRole() {
        initDeleteCommunityOptionView()
    }

    override fun openCommunityBlackListScreen(communityId: Int) {
        findNavController().safeNavigate(
            resId = R.id.action_meeraCommunityCreatorDashboardFragment_to_meeraCommunityBlacklistFragment,
            bundle = Bundle().apply {
                putInt(ARG_GROUP_ID, communityId)
            }
        )
    }

    override fun deleteBtnClick() {
        showDeleteCommunityConfirmationDialog()
    }

    override fun openCommunityMemberListScreen() {
        findNavController().safeNavigate(
            resId = R.id.action_meeraCommunityCreatorDashboardFragment_to_meeraCommunityMembersContainerFragment,
            bundle = Bundle().apply {
                putInt(ARG_GROUP_ID, viewModel.communityId)
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
            resId = R.id.action_meeraCommunityCreatorDashboardFragment_to_meeraCommunityEditFragment,
            bundle = Bundle().apply {
                putInt(ARG_GROUP_ID, viewModel.communityId)
                putBoolean(IArgContainer.ARG_IS_GROUP_CREATOR, true)
            }
        )
    }

    private fun initDeleteCommunityOptionView() {
        changeDeleteVisibilityState(isVisible = true)
    }

    private fun showDeleteCommunityConfirmationDialog() {
        MeeraConfirmDialogBuilder()
            .setHeader(R.string.delete_community_confirmation_dialog_header)
            .setDescription(R.string.delete_community_confirmation_dialog_header)
            .setTopBtnText(R.string.delete_community_confirmation_dialog_action_btn_text)
            .setTopBtnType(ButtonType.FILLED)
            .setTopClickListener { onCommunityDeletionSuccess() }
            .setBottomBtnText(R.string.delete_community_confirmation_dialog_cancel_btn_text)
            .show(childFragmentManager)
    }
}
