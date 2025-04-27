package com.numplates.nomera3.modules.communities.ui.fragment.dashboard

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.utils.showCommonError
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraCommunityAdministrationFragmentBinding
import com.numplates.nomera3.modules.communities.ui.entity.CommunityConstant.UNKNOWN_COMMUNITY_ID
import com.numplates.nomera3.modules.communities.ui.entity.CommunityInformationScreenUIModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityAdministrationScreenViewModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityDashboardScreenEvent.CommunityDeletionFailed
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityDashboardScreenEvent.CommunityDeletionStart
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityDashboardScreenEvent.CommunityDeletionSuccess
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityDashboardScreenEvent.CommunityInfoLoadingFailed
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityDashboardScreenEvent.CommunityInfoLoadingStart
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityDashboardScreenEvent.CommunityInfoLoadingSuccess
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Базовый экран "Управление сообществом", от которого наследуются экраны для
 * админа и создателя сообщества
 * */
abstract class MeeraBaseCommunityDashboardFragment :
    MeeraBaseDialogFragment(
        layout = R.layout.meera_community_administration_fragment,
        behaviourConfigState = ScreenBehaviourState.Full
    ) {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    var dashboardCallback: Callback? = null

    interface Callback {
        fun onCommunityInfoChanged()
    }

    protected val viewModel by viewModels<CommunityAdministrationScreenViewModel>()

    private val binding by viewBinding(MeeraCommunityAdministrationFragmentBinding::bind)
    private var adapter: MeeraBaseCommunityDashboardAdapter? = null
    private val deleteBtnClickableState = MutableStateFlow(true)
    private val blackListBtnClickableState = MutableStateFlow(true)
    private val usersBtnClickableState = MutableStateFlow(true)
    private val groupInformationBtnClickableState = MutableStateFlow(true)

    private val deleteProgressBarState = MutableStateFlow(false)
    private val blackListProgressBarState = MutableStateFlow(false)
    private val usersProgressBarState = MutableStateFlow(false)
    private val groupInformationProgressBarState = MutableStateFlow(false)

    private var deleteBtnVisibilityState: Boolean = true

    abstract fun openCommunityEditScreen()
    abstract fun openCommunityMemberListScreen()
    abstract fun setExtraViewSettingsByRole()
    abstract fun openCommunityBlackListScreen(communityId: Int)
    abstract fun deleteBtnClick()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.injectDependencies()

        viewModel.communityId = arguments
            ?.getInt(ARG_GROUP_ID)
            ?: UNKNOWN_COMMUNITY_ID
        viewModel.isPrivateCommunity = arguments
            ?.getInt(IArgContainer.ARG_COMMUNITY_IS_PRIVATE)
            ?: 1



        initView()

        setViewClickListeners()
        setLiveDataListeners()
        getCommunityInformation()
        setExtraViewSettingsByRole()
    }

    //TODO вернуть при объединении экранов Сообщества в новой навигации
//    override fun onReturnTransitionFragment() {
//        super.onReturnTransitionFragment()
//        getCommunityInformation()
//    }


    protected fun getCommunityInformation() {
        viewModel.getCommunityInformation()
    }

    protected fun setViewClickListeners() {
        binding?.vNavView?.backButtonClickListener = {
            findNavController().popBackStack()
        }
    }

    protected fun setLiveDataListeners() {
        viewModel.eventLiveData.observe(viewLifecycleOwner) { newEvent ->
            when (newEvent) {
                is CommunityInfoLoadingStart -> onCommunityInfoLoadingStart()
                is CommunityInfoLoadingFailed -> onCommunityInfoLoadingFailed()
                is CommunityInfoLoadingSuccess -> onCommunityInfoLoadingSuccess(newEvent.uiModel)

                is CommunityDeletionStart -> onCommunityDeletionStart()
                is CommunityDeletionFailed -> onCommunityDeletionFailed()
                is CommunityDeletionSuccess -> onCommunityDeletionSuccess()
            }
        }
    }

    protected fun onCommunityDeletionSuccess() {
        moveBackWithSkipPreviousFragment()
        setDeletionResultForUserCommunityListFragment()
        viewModel.amplitudeHelper.logCommunityDeleted()
    }

    protected fun onCommunityInfoLoadingSuccess(uiModel: CommunityInformationScreenUIModel?) {
        if (uiModel != null) {
            showData(uiModel)
        }
    }

    protected fun showData(uiModel: CommunityInformationScreenUIModel) {
        adapter?.setSettingsModel(uiModel)
        hideProgressBar()
        unlockClickableViews()
    }

    protected fun onCommunityInfoLoadingStart() {
        lockClickableViews()
        showProgressBar()
    }

    protected fun showProgressBar() {
        blackListProgressBarState.value = true
        usersProgressBarState.value = true
        groupInformationProgressBarState.value = true
    }

    protected fun hideProgressBar() {
        blackListProgressBarState.value = false
        usersProgressBarState.value = false
        groupInformationProgressBarState.value = false
    }

    protected fun lockClickableViews() {
        usersBtnClickableState.value = false
        blackListBtnClickableState.value = false
        groupInformationBtnClickableState.value = false
    }

    protected fun unlockClickableViews() {
        usersBtnClickableState.value = true
        blackListBtnClickableState.value = true
        groupInformationBtnClickableState.value = true
    }

    protected fun onCommunityInfoLoadingFailed() {
        showAlertNToastAtScreenTop(R.string.community_info_loading_error_text)
    }

    protected fun changeDeleteVisibilityState(isVisible: Boolean) {
        deleteBtnVisibilityState = isVisible
    }

    private fun settingsClickListener(action: MeeraBaseCommunityDashboardAction) {
        when (action) {
            is MeeraBaseCommunityDashboardAction.DeleteGroup -> {
                lifecycleScope.launch {
                    deleteBtnClickableState.collect {
                        action.clickableState.invoke(it)
                    }

                }
                lifecycleScope.launch {
                    deleteProgressBarState.collect {
                        action.progressBarState.invoke(it)
                    }
                }
                deleteBtnClick()
            }

            is MeeraBaseCommunityDashboardAction.SettingsGroup -> {
                openCommunityEditScreen()
                lifecycleScope.launch {
                    groupInformationBtnClickableState.collect {
                        action.clickableState.invoke(it)
                    }
                }
                lifecycleScope.launch {
                    groupInformationProgressBarState.collect {
                        action.progressBarState.invoke(it)
                    }
                }
            }

            is MeeraBaseCommunityDashboardAction.Users -> {
                openCommunityMemberListScreen()
                lifecycleScope.launch {
                    usersBtnClickableState.collect {
                        action.clickableState.invoke(it)
                    }
                }
                lifecycleScope.launch {
                    usersProgressBarState.collect {
                        action.progressBarState.invoke(it)
                    }
                }
            }

            is MeeraBaseCommunityDashboardAction.BlackList -> {
                if (viewModel.communityId != UNKNOWN_COMMUNITY_ID) {
                    openCommunityBlackListScreen(viewModel.communityId)
                    lifecycleScope.launch {
                        blackListBtnClickableState.collect {
                            action.clickableState.invoke(it)
                        }
                    }
                    lifecycleScope.launch {
                        blackListProgressBarState.collect {
                            action.progressBarState.invoke(it)
                        }
                    }
                } else {
                    showAlertNToastAtScreenTop(R.string.community_general_error)
                }
            }
        }
    }

    private fun showAlertNToastAtScreenTop(@StringRes stringRes: Int) {
        showCommonError(getText(stringRes), requireView())
    }

    private fun initView() {
        adapter = MeeraBaseCommunityDashboardAdapter(
            settingsListItem = MeeraBaseCommunitySettingsType.entries,
            deleteIsVisible = deleteBtnVisibilityState,
            clickListener = ::settingsClickListener
        )
        binding?.rvSettingsList?.adapter = adapter
    }

    private fun setDeletionResultForUserCommunityListFragment() =
        viewModel.deletionCommunityStart()


    // переход назад к списку групп, минуя открытый экран группы, которая была удалена
    private fun moveBackWithSkipPreviousFragment() {
        findNavController().popBackStack(R.id.meeraCommunitiesListsContainerFragment, false)
    }

    private fun onCommunityDeletionStart() {
        deleteBtnClickableState.value = false
    }

    private fun onCommunityDeletionFailed() {
        deleteBtnClickableState.value = true

        showAlertNToastAtScreenTop(R.string.community_deletion_error_text)
    }
}
