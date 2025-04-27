package com.numplates.nomera3.modules.communities.ui.fragment.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.meera.core.extensions.click
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.invisible
import com.meera.core.extensions.loadGlideCircleWithPlaceHolder
import com.meera.core.extensions.tryCatch
import com.meera.core.extensions.visible
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentCommunityAdministrationBinding
import com.numplates.nomera3.modules.communities.ui.entity.CommunityConstant.UNKNOWN_COMMUNITY_ID
import com.numplates.nomera3.modules.communities.ui.entity.CommunityInformationScreenUIModel
import com.numplates.nomera3.modules.communities.ui.fragment.blacklist.CommunityBlacklistFragment
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityAdministrationScreenViewModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityDashboardScreenEvent.CommunityDeletionFailed
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityDashboardScreenEvent.CommunityDeletionStart
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityDashboardScreenEvent.CommunityDeletionSuccess
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityDashboardScreenEvent.CommunityInfoLoadingFailed
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityDashboardScreenEvent.CommunityInfoLoadingStart
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityDashboardScreenEvent.CommunityInfoLoadingSuccess
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID
import com.numplates.nomera3.presentation.view.utils.NToast

/**
 * Базовый экран "Управление сообществом", от которого наследуются экраны для
 * админа и создателя сообщества
 *
 * https://www.figma.com/file/oi39VGnKVAkMekn5CNrQG8/May-2021?node-id=201%3A86448
 * https://www.figma.com/file/oi39VGnKVAkMekn5CNrQG8/May-2021?node-id=202%3A85028
 * https://nomera.atlassian.net/wiki/spaces/NOM/pages/2288550188/-#Управление-сообществом
 * */
abstract class BaseCommunityDashboardFragment :
    BaseFragmentNew<FragmentCommunityAdministrationBinding>() {

    companion object {
        const val COMMUNITY_DELETION_RESULT_REQUEST_KEY = "CommunityDeletionResult"
        const val COMMUNITY_DELETION_RESULT_BUNDLE_KEY = "isCommunityDeletionSuccess"
        const val COMMUNITY_DELETION_ID_BUNDLE_KEY = "communityDeletedId"
    }

    var dashboardCallback: Callback? = null

    interface Callback {
        fun onCommunityInfoChanged()
    }

    protected val viewModel by viewModels<CommunityAdministrationScreenViewModel>()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCommunityAdministrationBinding
        get() = FragmentCommunityAdministrationBinding::inflate

    protected val statusBarStubView: View?
        get() = binding?.statusBarStub

    protected val backButtonView: View?
        get() = binding?.backButton

    protected val communityCoverImageView: ImageView?
        get() = binding?.communityCoverImage

    protected val communityNameTextView: TextView?
        get() = binding?.communityName

    protected val communityNameProgressBar: ContentLoadingProgressBar?
        get() = binding?.communityNameProgressBar

    protected val communityNameInnerContainer: View?
        get() = binding?.communityNameInnerContainer

    protected val communityStartEditTextView: TextView?
        get() = binding?.communityStartEdit

    protected val communityMembersOptionView: View?
        get() = binding?.communityMembersOption

    protected val communityMembersCountTextView: TextView?
        get() = binding?.communityMembersCount

    protected val communityMembersCountProgressBar: ContentLoadingProgressBar?
        get() = binding?.communityMembersCountProgressBar

    protected val communityBlacklistOptionView: View?
        get() = binding?.communityBlacklistOption

    protected val communityBlacklistMembersCountTextView: TextView?
        get() = binding?.communityBlacklistMembersCount

    protected val communityBlacklistMembersCountProgressBar: ContentLoadingProgressBar?
        get() = binding?.communityBlacklistMembersCountProgressBar

    protected val communityDeleteOptionContainerView: View?
        get() = binding?.communityDeleteOptionContainer

    protected val communityDeletionProgressBar: View?
        get() = binding?.communityDeletionProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.injectDependencies()

        viewModel.communityId = arguments
            ?.getInt(ARG_GROUP_ID)
            ?: UNKNOWN_COMMUNITY_ID

        viewModel.isPrivateCommunity = arguments
            ?.getInt(IArgContainer.ARG_COMMUNITY_IS_PRIVATE)
            ?: 1

        setAppropriateStatusBarHeight()
        setViewClickListeners()
        setLiveDataListeners()
        getCommunityInformation()
        setExtraViewSettingsByRole()
    }

    override fun onReturnTransitionFragment() {
        super.onReturnTransitionFragment()
        getCommunityInformation()
    }

    abstract fun setExtraViewSettingsByRole()

    protected fun getCommunityInformation() {
        viewModel.getCommunityInformation()
    }

    protected fun setViewClickListeners() {
        backButtonView?.click {
            act?.onBackPressed()
        }

        communityMembersOptionView?.click {
            openCommunityMemberListScreen()
        }

        communityStartEditTextView?.click {
            openCommunityEditScreen()
        }

        communityBlacklistOptionView?.click {
            if (viewModel.communityId != UNKNOWN_COMMUNITY_ID) {
                openCommunityBlacklistScreen(viewModel.communityId)
            } else {
                showAlertNToastAtScreenTop(R.string.community_general_error)
            }
        }
    }

    abstract fun openCommunityEditScreen()

    abstract fun openCommunityMemberListScreen()

    private fun openCommunityBlacklistScreen(communityId: Int) {
        add(
            CommunityBlacklistFragment(),
            Act.LIGHT_STATUSBAR,
            Arg(ARG_GROUP_ID, communityId)
        )
    }

    protected fun setLiveDataListeners() {
        viewModel.eventLiveData.observe(this, Observer { newEvent ->
            when (newEvent) {
                // для загрузки информации о сообществе
                is CommunityInfoLoadingStart -> onCommunityInfoLoadingStart()
                is CommunityInfoLoadingFailed -> onCommunityInfoLoadingFailed()
                is CommunityInfoLoadingSuccess -> onCommunityInfoLoadingSuccess(newEvent.uiModel)
                // для удаления сообщества
                is CommunityDeletionStart -> onCommunityDeletionStart()
                is CommunityDeletionFailed -> onCommunityDeletionFailed()
                is CommunityDeletionSuccess -> onCommunityDeletionSuccess()
            }
        })
    }

    protected fun onCommunityDeletionSuccess() {
        setDeletionResultForUserCommunityListFragment()
        moveBackWithSkipPreviousFragment()
        viewModel.amplitudeHelper.logCommunityDeleted()
    }

    private fun setDeletionResultForUserCommunityListFragment() =
        viewModel.deletionCommunityStart()


    // переход назад к списку групп, минуя открытый экран группы, которая была удалена
    private fun moveBackWithSkipPreviousFragment() {
        if (act.getFragmentsCount() >= 3) {
            act.returnToTargetFragment(act.getFragmentsCount() - 3, true)
        } else {
            act.onBackPressed()
        }
    }

    private fun onCommunityDeletionStart() {
        communityDeletionProgressBar?.visible()
        communityDeleteOptionContainerView?.isClickable = false
    }

    private fun onCommunityDeletionFailed() {
        communityDeletionProgressBar?.invisible()
        communityDeleteOptionContainerView?.isClickable = true

        showAlertNToastAtScreenTop(R.string.community_deletion_error_text)
    }

    protected fun onCommunityInfoLoadingSuccess(uiModel: CommunityInformationScreenUIModel?) {
        if (uiModel != null) {
            showData(uiModel)
        }
    }

    protected fun showData(uiModel: CommunityInformationScreenUIModel) {
        communityNameTextView?.text = uiModel.communityName
        communityMembersCountTextView?.text = uiModel.communityMembersCount
        communityBlacklistMembersCountTextView?.text = uiModel.communityBlacklistMembersCount

        uiModel.communityCoverImageURL?.also { communityCoverImageURL ->
            communityCoverImageView?.loadGlideCircleWithPlaceHolder(
                communityCoverImageURL,
                R.drawable.community_cover_image_placeholder
            )
        }

        hideProgressBar()
        unlockClickableViews()
    }

    protected fun onCommunityInfoLoadingStart() {
        lockClickableViews()
        showProgressBar()
    }

    protected fun showProgressBar() {
        communityBlacklistMembersCountTextView?.invisible()
        communityBlacklistMembersCountProgressBar?.visible()

        communityMembersCountTextView?.invisible()
        communityMembersCountProgressBar?.visible()

        communityNameInnerContainer?.invisible()
        communityNameProgressBar?.visible()
    }

    protected fun hideProgressBar() {
        communityBlacklistMembersCountProgressBar?.invisible()
        communityBlacklistMembersCountTextView?.visible()

        communityMembersCountProgressBar?.invisible()
        communityMembersCountTextView?.visible()

        communityNameProgressBar?.invisible()
        communityNameInnerContainer?.visible()
    }

    protected fun lockClickableViews() {
        communityMembersOptionView?.isClickable = false
        communityBlacklistOptionView?.isClickable = false
        communityNameInnerContainer?.isClickable = false
    }

    protected fun unlockClickableViews() {
        communityMembersOptionView?.isClickable = true
        communityBlacklistOptionView?.isClickable = true
        communityNameInnerContainer?.isClickable = true
    }

    protected fun onCommunityInfoLoadingFailed() {
        showAlertNToastAtScreenTop(R.string.community_info_loading_error_text)
    }

    protected fun setAppropriateStatusBarHeight() {
        initStatusBarViewHeight<LinearLayoutCompat.LayoutParams>(statusBarStubView)
    }

    private fun showAlertNToastAtScreenTop(@StringRes stringRes: Int) {
        NToast.with(view)
            .text(getString(stringRes))
            .typeAlert()
            .show()

    }

    private fun <T : ViewGroup.MarginLayoutParams> initStatusBarViewHeight(statusBarViewHeight: View?) {
        if (statusBarViewHeight != null) {
            tryCatch {
                val params = statusBarViewHeight.layoutParams as? T
                if (params != null) {
                    params.height = context.getStatusBarHeight()
                    statusBarViewHeight.layoutParams = params
                }
            }
        }
    }
}
