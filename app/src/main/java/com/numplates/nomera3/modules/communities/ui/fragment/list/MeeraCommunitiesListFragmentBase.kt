package com.numplates.nomera3.modules.communities.ui.fragment.list

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.utils.showCommonError
import com.meera.core.utils.showCommonSuccessMessage
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraGroupsListFragmentBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityFollow
import com.numplates.nomera3.modules.communities.ui.entity.CommunityListItemUIModel
import com.numplates.nomera3.modules.communities.ui.viewevent.CommunityViewEvent
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunitySubscriptionViewModel
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import kotlinx.coroutines.launch

abstract class MeeraCommunitiesListFragmentBase : MeeraBaseDialogFragment(
    layout = R.layout.meera_groups_list_fragment,
    behaviourConfigState = ScreenBehaviourState.Full
) {
    var refreshListsCallback: (() -> Unit)? = null
    val binding by viewBinding(MeeraGroupsListFragmentBinding::bind)
    private val subscriptionViewModel by viewModels<CommunitySubscriptionViewModel>(
        factoryProducer = { App.component.getViewModelFactory() },
        ownerProducer = { requireActivity() }
    )

    override val containerId: Int
        get() = R.id.fragment_first_container_view

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

            else -> Unit
        }
    }

    private fun onSubscribeGroup(position: Int?) {
        position?.let {
            setSubscriptionStatus(position, true)
            showCommonSuccessMessage(getText(R.string.group_joined), requireView())
        }
    }

    private fun onSubscribePrivateGroup(position: Int?) {
        position?.let {
            setSubscriptionStatus(position, true)
            showCommonSuccessMessage(getText(R.string.group_private_subscription_success), requireView())
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

    private fun showErrorMessage(@StringRes messageRes: Int) {
        showCommonError(getText(messageRes), requireView())
    }

    private fun setSubscriptionStatus(position: Int, subscribed: Boolean) {
        setCommunitySubscriptionStatus(position, subscribed)
        refreshUserGroupsList()
    }

    open fun refreshUserGroupsList() {}

    open fun refreshAllGroupsList() {}

    open fun setCommunitySubscriptionStatus(position: Int, subscribed: Boolean) {}
}
