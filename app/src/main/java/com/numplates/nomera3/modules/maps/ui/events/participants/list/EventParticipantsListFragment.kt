package com.numplates.nomera3.modules.maps.ui.events.participants.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentParticipantsListBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.maps.ui.events.participants.list.adapter.EventParticipantsListItemAdapter
import com.numplates.nomera3.modules.maps.ui.events.participants.list.model.EventParticipantsListUiAction
import com.numplates.nomera3.modules.maps.ui.events.participants.list.model.EventParticipantsListUiEffect
import com.numplates.nomera3.modules.maps.ui.events.participants.list.model.EventParticipantsListUiModel
import com.numplates.nomera3.modules.maps.ui.events.participants.list.model.EventParticipantsParamsUiModel
import com.numplates.nomera3.modules.maps.ui.events.participants.list.model.ParticipantRemoveOption
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator

class EventParticipantsListFragment : BaseFragmentNew<FragmentParticipantsListBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentParticipantsListBinding
        get() = FragmentParticipantsListBinding::inflate

    private val viewModel by viewModels<EventParticipantsListViewModel> { App.component.getViewModelFactory() }

    private var uiModel: EventParticipantsListUiModel? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parseArguments()
        setupUi()
    }

    private fun parseArguments() {
        arguments?.let {
            viewModel.handleUiAction(EventParticipantsListUiAction.ViewInitialized(
                EventParticipantsParamsUiModel(
                    eventId = it.getLong(ARG_EVENT_ID),
                    postId = it.getLong(ARG_POST_ID),
                    participantsCount = it.getInt(ARG_PARTICIPANTS_COUNT)
                )
            ))
        }
    }

    override fun onStartFragment() {
        super.onStartFragment()
        viewModel.refreshParticipantsList()
    }

    private fun setupUi() {
        val localBinding = binding ?: return
        localBinding.rvParticipantsListItems.adapter = EventParticipantsListItemAdapter(viewModel::handleUiAction)
        RecyclerViewPaginator(
            recyclerView = localBinding.rvParticipantsListItems,
            onLast = { uiModel?.isLastPage.isTrue() },
            isLoading = { uiModel?.isLoadingNextPage.isTrue() },
            loadMore = { viewModel.handleUiAction(EventParticipantsListUiAction.LoadNextPageRequested) },
        ).apply {
            endWithAuto = true
        }
        localBinding.ivParticipantsListBack.setThrottledClickListener {
            act.navigateBack()
        }
        localBinding.srlParticipantsListRefresh.setOnRefreshListener {
            viewModel.handleUiAction(EventParticipantsListUiAction.RefreshRequested)
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiEffectsFlow.collect(::handleUiEffect)
        }
        viewModel.liveUiModel.observe(viewLifecycleOwner, ::handleUiModel)
    }

    private fun handleUiModel(uiModel: EventParticipantsListUiModel) {
        this.uiModel = uiModel
        val localBinding = binding ?: return
        (localBinding.rvParticipantsListItems.adapter as EventParticipantsListItemAdapter).submitList(uiModel.items)
        localBinding.tvParticipantsListCount.text = uiModel.participantsCountString
        localBinding.tvParticipantsListCount.visible()
        localBinding.srlParticipantsListRefresh.isRefreshing = uiModel.isRefreshing
    }

    private fun handleUiEffect(uiEffect: EventParticipantsListUiEffect) {
        when (uiEffect) {
            is EventParticipantsListUiEffect.OpenUserProfile -> openUserProfile(uiEffect.userId)
            is EventParticipantsListUiEffect.ShowParticipantMenu -> showParticipantMenu(
                userId = uiEffect.userId,
                participantRemoveOption = uiEffect.removeOption
            )
        }
    }

    private fun openUserProfile(userId: Long) {
        add(
            fragment = UserInfoFragment(),
            isLightStatusBar = Act.LIGHT_STATUSBAR,
            args = arrayOf(
                Arg(IArgContainer.ARG_USER_ID, userId),
                Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.POSSIBLE_MEMBER_EVENT.property))

        )
    }

    private fun showParticipantMenu(userId: Long, participantRemoveOption: ParticipantRemoveOption) {
        val menu = MeeraMenuBottomSheet(context)
        menu.addItem(
            title = R.string.map_events_participants_menu_open_profile,
            icon = R.drawable.ic_user
        ) {
            openUserProfile(userId)
        }
        when (participantRemoveOption) {
            ParticipantRemoveOption.CanLeave -> menu.addItem(
                title = R.string.map_events_participants_menu_leave,
                icon = R.drawable.ic_remove_user
            ) {
                viewModel.handleUiAction(EventParticipantsListUiAction.LeaveEventClicked)
            }
            ParticipantRemoveOption.CanRemove -> menu.addItem(
                title = R.string.map_events_participants_menu_remove,
                icon = R.drawable.ic_remove_user
            ) {
                viewModel.handleUiAction(EventParticipantsListUiAction.RemoveParticipantClicked(userId))
            }
            ParticipantRemoveOption.RemoveNotAvailable -> Unit
        }
        menu.showWithTag(manager = childFragmentManager, tag = PARTICIPANT_MENU_TAG)
    }

    companion object {
        const val ARG_PARTICIPANTS_COUNT = "ARG_PARTICIPANTS_COUNT"
        const val ARG_EVENT_ID = "ARG_EVENT_ID"
        const val ARG_POST_ID = "ARG_POST_ID"

        private const val PARTICIPANT_MENU_TAG = "PARTICIPANT_MENU_TAG"
    }
}
