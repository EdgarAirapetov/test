package com.numplates.nomera3.modules.redesign.fragments.main.map.participant

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentParticipantsListBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.maps.ui.events.participants.list.EventParticipantsListViewModel
import com.numplates.nomera3.modules.maps.ui.events.participants.list.model.EventParticipantsListUiAction
import com.numplates.nomera3.modules.maps.ui.events.participants.list.model.EventParticipantsListUiEffect
import com.numplates.nomera3.modules.maps.ui.events.participants.list.model.EventParticipantsListUiModel
import com.numplates.nomera3.modules.maps.ui.events.participants.list.model.EventParticipantsParamsUiModel
import com.numplates.nomera3.modules.maps.ui.events.participants.list.model.ParticipantRemoveOption
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.fragments.main.map.participant.adapter.MeeraEventParticipantsListItemAdapter
import com.numplates.nomera3.modules.redesign.fragments.main.map.participant.adapter.MeeraMapParticipantsItemDecorator
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator

class MeeraEventParticipantsListFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_participants_list,
    ScreenBehaviourState.EventParticipants
) {

    private val binding by viewBinding(MeeraFragmentParticipantsListBinding::bind)

    private val viewModel by viewModels<EventParticipantsListViewModel> { App.component.getViewModelFactory() }

    private var uiModel: EventParticipantsListUiModel? = null
    override val containerId: Int
        get() = R.id.fragment_first_container_view

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parseArguments()
        setupUi()
    }

    private fun parseArguments() {
        arguments?.let {
            viewModel.handleUiAction(
                EventParticipantsListUiAction.ViewInitialized(
                    EventParticipantsParamsUiModel(
                        eventId = it.getLong(ARG_EVENT_ID),
                        postId = it.getLong(ARG_POST_ID),
                        participantsCount = it.getInt(ARG_PARTICIPANTS_COUNT)
                    )
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshParticipantsList()
    }

    private fun setupUi() {
        val localBinding = binding ?: return
        binding.rvParticipantsListItems.addItemDecoration(MeeraMapParticipantsItemDecorator(requireContext()))
        localBinding.rvParticipantsListItems.adapter = MeeraEventParticipantsListItemAdapter(viewModel::handleUiAction)
        RecyclerViewPaginator(
            recyclerView = localBinding.rvParticipantsListItems,
            onLast = { uiModel?.isLastPage.isTrue() },
            isLoading = { uiModel?.isLoadingNextPage.isTrue() },
            loadMore = { viewModel.handleUiAction(EventParticipantsListUiAction.LoadNextPageRequested) },
        ).apply {
            endWithAuto = true
        }
        localBinding.ivParticipantsListBack.setThrottledClickListener {
            findNavController().popBackStack()
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
        (localBinding.rvParticipantsListItems.adapter as MeeraEventParticipantsListItemAdapter).submitList(uiModel.items)
        localBinding.tvParticipantsListCount.text = requireActivity().pluralString(R.plurals.group_members_plural, uiModel.participantsCount)
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
        NavigationManager.getManager().mainMapFragment.closeSnippet(true)
        NavigationManager.getManager().topNavController.safeNavigate(
            R.id.action_peoplesFragment_to_userInfoFragment,
            bundleOf(
                IArgContainer.ARG_USER_ID to userId,
                IArgContainer.ARG_TRANSIT_FROM to AmplitudePropertyWhere.POSSIBLE_MEMBER_EVENT.property)
            )
    }

    private fun showParticipantMenu(userId: Long, participantRemoveOption: ParticipantRemoveOption) {
        val menu = MeeraMenuBottomSheet(context)
        menu.addItem(
            title = R.string.map_events_participants_menu_open_profile,
            icon = R.drawable.ic_outlined_user_s
        ) {
            openUserProfile(userId)
        }
        when (participantRemoveOption) {
            ParticipantRemoveOption.CanLeave -> menu.addItem(
                title = R.string.map_events_participants_menu_leave,
                icon = R.drawable.ic_outlined_delete_m
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
