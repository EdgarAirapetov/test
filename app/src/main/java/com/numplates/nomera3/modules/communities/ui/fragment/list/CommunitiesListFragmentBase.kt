package com.numplates.nomera3.modules.communities.ui.fragment.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.core.utils.NSnackbar
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentGroupsListBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityFollow
import com.numplates.nomera3.modules.communities.ui.entity.CommunityListItemUIModel
import com.numplates.nomera3.modules.communities.ui.viewevent.CommunityViewEvent
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunitySubscriptionViewModel
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import kotlinx.coroutines.launch

abstract class CommunitiesListFragmentBase : BaseFragmentNew<FragmentGroupsListBinding>() {

    var refreshListsCallback: (() -> Unit)? = null

    private val subscriptionViewModel by viewModels<CommunitySubscriptionViewModel>(
        factoryProducer = { App.component.getViewModelFactory() },
        ownerProducer = { requireActivity() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
    }

    protected fun subscribeGroup(model: CommunityListItemUIModel, position: Int) {
        subscriptionViewModel.subscribeCommunity(model, AmplitudePropertyWhereCommunityFollow.ALL_COMMUNITY, position)
    }

    private fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                subscriptionViewModel.viewEvent.collect(::handleViewEvents)
            }
        }
    }

    private fun handleViewEvents(event: CommunityViewEvent) {
        when (event) {
            is CommunityViewEvent.SuccessSubscribeCommunity -> {
                onSubscribeGroup(event.position)
            }

            is CommunityViewEvent.SuccessUnsubscribeCommunity -> {
                onUnsubscribeGroup(event.position)
            }

            is CommunityViewEvent.SuccessUnsubscribePrivateCommunity -> {
                onUnSubscribePrivateGroup(event.position)
            }

            is CommunityViewEvent.SuccessSubscribePrivateCommunity -> {
                onSubscribePrivateGroup(event.position)
            }

            is CommunityViewEvent.FailureGetCommunityInfo -> {
                showErrorMessage(R.string.group_error_load_group_data)
            }

            is CommunityViewEvent.FailureSubscribeCommunity -> {
                showErrorMessage(R.string.group_error_subscribe_group)
            }

            is CommunityViewEvent.FailureUnsubscribeCommunity -> {
                showErrorMessage(R.string.group_error_unsubscribe_group)
            }
            else -> {}
        }
    }

    private fun onSubscribeGroup(position: Int?) {
        position?.let {
            setSubscriptionStatus(position, true)
            showSuccessMessage(R.string.group_subscription_success)
        }
    }

    private fun onSubscribePrivateGroup(position: Int?) {
        position?.let {
            setSubscriptionStatus(position, true)
            showSuccessMessage(R.string.group_private_subscription_success)
        }
    }

    private fun onUnsubscribeGroup(position: Int?) {
        position?.let {
            setSubscriptionStatus(position, false)
        }
    }

    private fun onUnSubscribePrivateGroup(position: Int?) {
        position?.let {
            setSubscriptionStatus(position, false)
        }
    }

    private fun showSuccessMessage(@StringRes messageRes: Int) {
        NSnackbar.with(requireView())
            .typeSuccess()
            .text(getString(messageRes))
            .show()
    }

    private fun showErrorMessage(@StringRes messageRes: Int) {
        NSnackbar.with(requireView())
            .typeError()
            .text(getString(messageRes))
            .show()
    }

    private fun setSubscriptionStatus(position: Int, subscribed: Boolean) {
        setCommunitySubscriptionStatus(position, subscribed)
        refreshUserGroupsList()
    }

    protected fun showProgress(inProgress: Boolean) {
        if (inProgress) binding?.progressBarList?.visible()
        else binding?.progressBarList?.gone()
    }

    open fun refreshUserGroupsList() {}

    open fun refreshAllGroupsList() {}

    open fun setCommunitySubscriptionStatus(position: Int, subscribed: Boolean) {}

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGroupsListBinding
        get() = FragmentGroupsListBinding::inflate
}
