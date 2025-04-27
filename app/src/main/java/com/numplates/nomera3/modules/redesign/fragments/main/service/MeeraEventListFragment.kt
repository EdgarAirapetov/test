package com.numplates.nomera3.modules.redesign.fragments.main.service

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.doOnUIThread
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.PaddingState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentEventListBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriendsWhereProperty
import com.numplates.nomera3.modules.comments.ui.fragment.MeeraPostFragmentV2
import com.numplates.nomera3.modules.feed.domain.mapper.toPost
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListType
import com.numplates.nomera3.modules.maps.ui.events.list.filters.model.EventFilterDateUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.filters.model.EventFilterTypeUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventFiltersUpdateUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListItem
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListsUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapUiAction
import com.numplates.nomera3.modules.maps.ui.model.MapUiEffect
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.fragments.main.map.configuration.MeeraMapViewModel
import com.numplates.nomera3.modules.redesign.fragments.main.map.events.adapter.MeeraEventsListsPagerAdapter
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.redesign.util.setHiddenState
import com.numplates.nomera3.modules.search.ui.fragment.SearchMainFragment
import com.numplates.nomera3.modules.share.ui.model.SharingDialogMode
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.addOnPageChangeListener
import com.numplates.nomera3.presentation.view.utils.sharedialog.IOnSharePost
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareBottomSheetData
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareSheet
import kotlinx.coroutines.launch

private const val BACKPESSED_DELAY = 300L
private const val DEFAULT_TOAST_SUCCESS_MESSAGE_BOTTOM_PADDING = 60

class MeeraEventListFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_event_list,
    behaviourConfigState = ScreenBehaviourState.EventList
) {
    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraFragmentEventListBinding::bind)

    private var uiActionListener: ((MapUiAction.EventsListUiAction) -> Unit)? = null

    private val adapter = MeeraEventsListsPagerAdapter { uiAction ->
        uiActionListener?.invoke(uiAction)
    }

    private val mapViewModel: MeeraMapViewModel by viewModels(
        ownerProducer = { NavigationManager.getManager().mainMapFragment }
    )
    private var undoSnackbar: UiKitSnackBar? = null

    private var checkedIndex = DEFAULT_PARTICIPATION_INDEX
    private var uiModel: EventsListsUiModel? = null
    private var contentFragment: MeeraPostFragmentV2? = null

    private fun backPressed(navController: NavController) {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            NavigationManager.getManager().getTopBehaviour()?.setHiddenState()

            doDelayed(BACKPESSED_DELAY) {
                navController.popBackStack(R.id.servicesFragment, false)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backPressed(navController = findNavController())

        binding.layoutEventsListsMain.vpEventsListsMainPages.adapter = adapter
        binding.layoutEventsListsMain.vpEventsListsMainPages.offscreenPageLimit = OFFSCREEN_PAGE_LIMIT

        binding.layoutEventsListsMain.ivEventsListsMainFilters.setThrottledClickListener {
            binding.layoutEventsListsMain.root.gone()
            binding.layoutEventsListsFilters.root.visible()
            binding.llUkscEventsListsFilterParticipation.gone()
        }

        binding.layoutEventsListsFilters.ivEventsListsFiltersBack.setThrottledClickListener {
            binding.layoutEventsListsMain.root.visible()
            binding.layoutEventsListsFilters.root.gone()
            handleParticipationSwitchVisibility()
            handleEventFiltersClosed()
        }

        binding.layoutEventsListsFilters.efwEventsListsFilterTypeWidget.filterChangeListener = {
            handleEventFiltersUpdate()
        }

        binding.layoutEventsListsFilters.efwEventsListsFilterDateWidget.filterChangeListener = {
            handleEventFiltersUpdate()
        }

        binding.layoutEventsListsMain.ukrtlEventsListsMainTabs
            .setupWithViewPager(binding.layoutEventsListsMain.vpEventsListsMainPages)

        binding.layoutEventsListsMain.vpEventsListsMainPages.addOnPageChangeListener(
            onPageSelected = { index ->
                uiActionListener?.invoke(MapUiAction.EventsListUiAction.SelectedPageChanged(index))
                binding.llUkscEventsListsFilterParticipation.visible()
                adapter.notifyDataSetChanged()
            }
        )

        binding.ukscEventsListsFilterParticipation.onCheckedIndexChangeListener = { index ->
            uiActionListener?.invoke(MapUiAction.EventsListUiAction.EventParticipationCategoryChanged(index))
            binding.ukscEventsListsFilterParticipation.isGroupEnabled(false)
        }

        binding.layoutEventsListsFilters.tvEventsListsFiltersReset.setThrottledClickListener {
            binding.layoutEventsListsFilters.efwEventsListsFilterTypeWidget.clear()
            binding.layoutEventsListsFilters.efwEventsListsFilterDateWidget.clear()
        }

        initObservers()
        NavigationManager.getManager().getTopBehaviour()?.state = STATE_COLLAPSED
    }

    private fun initObservers() {
        uiActionListener = mapViewModel::handleUiAction

        mapViewModel.eventsListUiModel.observe(viewLifecycleOwner) {
            setUiModel(it)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            mapViewModel.showShareDialogModel.collect { post ->
                post?.let { meeraOpenRepostMenu(it, mode = SharingDialogMode.SUGGEST_EVENT_SHARING) }
            }
        }

        mapViewModel.handleUiAction(MapUiAction.EventsListUiAction.EventsListPressed)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                mapViewModel.uiEffectsFlow.collect(::handleUiEffect)
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            mapViewModel.uiEffectsFlow.collect(::handleUiEffect)
        }
    }

    private fun handleUiEffect(uiEffect: MapUiEffect) {
        when (uiEffect) {
            is MapUiEffect.OpenEventsListItemDetails -> {
                NavigationManager.getManager().topNavController.safeNavigate(
                    resId = R.id.action_meeraEventListFragment_to_meeraEventDetailsFragment,
                    bundle = bundleOf(
                        IArgContainer.ARG_FEED_POST_ID to uiEffect.eventPost.postId,
                        IArgContainer.ARG_FEED_POST_NEED_TO_UPDATE to true,
                        IArgContainer.ARG_FEED_POST to uiEffect.eventPost.copy(openedFromRoad = true),
                        IArgContainer.ARG_FROM_EVENT_SNIPPET to true
                    )
                )
            }

            else -> Unit
        }
    }

    private fun meeraOpenRepostMenu(post: PostUIEntity, mode: SharingDialogMode = SharingDialogMode.DEFAULT) {
        MeeraShareSheet().show(
            fm = childFragmentManager,
            data = MeeraShareBottomSheetData(
                post = post.toPost(),
                postOrigin = DestinationOriginEnum.OTHER_PROFILE,
                event = post.event,
                mode = mode,
                callback = object : IOnSharePost {
                    override fun onShareFindGroup() {
                        openGroups()
                    }

                    override fun onShareFindFriend() {
                        openSearch()
                    }

                    override fun onShareToGroupSuccess(groupName: String?) {
                        mapViewModel.repostSuccess(post)
                        showToastMessage(getString(R.string.success_repost_to_group, groupName ?: ""))
                    }

                    override fun onShareToRoadSuccess() {
                        mapViewModel.repostSuccess(post)
                        showToastMessage(getString(R.string.success_repost_to_own_road))
                    }

                    override fun onShareToChatSuccess(repostTargetCount: Int) {
                        mapViewModel.repostSuccess(post, repostTargetCount)
                        val strResId = if (post.isEvent()) {
                            R.string.success_event_repost_to_chat
                        } else {
                            R.string.success_repost_to_chat
                        }
                        showToastMessage(getString(strResId))
                    }

                    override fun onPostItemUniqnameUserClick(userId: Long?) {
//                        allowScreenshotPopupShowing()
//                        openUserFragment(userId)
                    }
                }
            )
        )
    }

    private fun showToastMessage(messageString: String?, iconState: AvatarUiState = AvatarUiState.SuccessIconState) =
        doOnUIThread {
            val resultMessage = messageString ?: getString(R.string.reaction_unknown_error)
            undoSnackbar = UiKitSnackBar.make(
                view = requireView(),
                params = SnackBarParams(
                    snackBarViewState = SnackBarContainerUiState(
                        messageText = resultMessage,
                        avatarUiState = iconState,
                    ),
                    duration = BaseTransientBottomBar.LENGTH_SHORT,
                    dismissOnClick = true,
                    paddingState = PaddingState(bottom = DEFAULT_TOAST_SUCCESS_MESSAGE_BOTTOM_PADDING.dp
                )
            )
            )
            undoSnackbar?.show()
        }

    private fun openSearch() {
        findNavController().safeNavigate(
            resId = R.id.action_meeraPostFragmentV2_to_meeraSearchFragment,
            bundle = Bundle().apply {
                putSerializable(
                    IArgContainer.ARG_FIND_FRIENDS_OPENED_FROM_WHERE,
                    AmplitudeFindFriendsWhereProperty.SHARE
                )
            }
        )
    }

    @Suppress("detekt:SwallowedException")
    private fun openGroups() {
        try {
            findNavController().navigate(R.id.action_meeraPostFragmentV2_to_meeraCommunitiesListsContainerFragment)
        } catch (e: Exception) {
            NavigationManager.getManager().topNavController.safeNavigate(R.id.action_meeraPostFragmentV2_to_meeraSearchFragment, bundle = bundleOf(
                IArgContainer.ARG_SEARCH_OPEN_PAGE to SearchMainFragment.PAGE_SEARCH_COMMUNITY
            ))
        }
    }

    fun onScreenshotTaken() {
        contentFragment?.onScreenshotTaken()
    }

    fun selectItem(eventsListType: EventsListType, item: EventsListItem) {
        adapter.selectItem(eventsListType = eventsListType, item = item)
    }

    fun setUiModel(uiModel: EventsListsUiModel) {
        if (uiModel.eventsListsPages.isEmpty()) return
        this.uiModel = uiModel
        val selectedPage = uiModel.eventsListsPages[uiModel.selectedPageIndex]
        handleEventFilterType(selectedPage.filters.eventFilterType)
        handleEventFilterDate(selectedPage.filters.eventFilterDate)
        handleEventParticipationCategory(selectedPage.filters.participationCategoryIndex)
        handleFiltersNonDefault(selectedPage.filters.nonDefaultFilters)
        adapter.setEventListPages(uiModel.eventsListsPages)
        mapViewModel.openEvent?.let {
            adapter.selectItem(it.eventsListType, it)
        }

        binding.layoutEventsListsMain.vpEventsListsMainPages.currentItem = uiModel.selectedPageIndex
        val isStub = selectedPage.eventsListItems.items.any { it is EventsListItem.StubItemUiModel }
        binding.ukscEventsListsFilterParticipation.isGroupEnabled(!isStub)
    }

    private fun handleParticipationSwitchVisibility() {
        val uiModel = this.uiModel ?: return
        val selectedPage = uiModel.eventsListsPages[uiModel.selectedPageIndex]
        binding.llUkscEventsListsFilterParticipation.isVisible = selectedPage.filters.participationCategoryIndex != null
    }

    private fun handleEventFiltersClosed() {
        val eventFilterUpdate = EventFiltersUpdateUiModel(
            eventFilterType = binding.layoutEventsListsFilters.efwEventsListsFilterTypeWidget.getUiModel(),
            eventFilterDate = binding.layoutEventsListsFilters.efwEventsListsFilterDateWidget.getUiModel()
        )
        uiActionListener?.invoke(MapUiAction.EventsListUiAction.EventFiltersChanged(eventFilterUpdate))
    }

    private fun handleEventFiltersUpdate() {
        val filtersInDefault = binding.layoutEventsListsFilters.efwEventsListsFilterTypeWidget.isDefault()
            && binding.layoutEventsListsFilters.efwEventsListsFilterDateWidget.isDefault()
        binding.layoutEventsListsFilters.tvEventsListsFiltersReset.isVisible = filtersInDefault.not()
        binding.layoutEventsListsFilters.tvEventsListsFiltersReset.isEnabled = filtersInDefault.not()
    }

    private fun handleEventFilterType(eventFilterType: EventFilterTypeUiModel) =
        binding.layoutEventsListsFilters.efwEventsListsFilterTypeWidget.setUiModel(eventFilterType)

    private fun handleEventFilterDate(eventFilterDate: EventFilterDateUiModel?) {
        if (eventFilterDate != null) {
            binding.layoutEventsListsFilters.efwEventsListsFilterDateWidget.setUiModel(eventFilterDate)
            binding.layoutEventsListsFilters.efwEventsListsFilterDateWidget.visible()
        } else {
            binding.layoutEventsListsFilters.efwEventsListsFilterDateWidget.gone()
        }
    }

    private fun handleEventParticipationCategory(participationCategoryIndex: Int?) {
        if (participationCategoryIndex != null) {
            binding.llUkscEventsListsFilterParticipation.visible()
            if (checkedIndex != participationCategoryIndex) {
                checkedIndex = participationCategoryIndex
            }
            binding.ukscEventsListsFilterParticipation.setCheckedSegmentByIndex(participationCategoryIndex)
        } else {
            binding.llUkscEventsListsFilterParticipation.gone()
        }
    }

    override fun onStateChanged(newState: Int) {
        when (newState) {
            STATE_HALF_EXPANDED -> {
                if (uiModel?.selectedPageIndex != 0 && uiModel?.selectedPageIndex != null) {
                    binding.llUkscEventsListsFilterParticipation.visible()
                }
            }
        }
        if (newState == STATE_HIDDEN) {
            undoSnackbar?.dismiss()
            findNavController().popBackStack()
        }
        super.onStateChanged(newState)
    }

    override fun onDestroy() {
        super.onDestroy()
        uiActionListener?.invoke(MapUiAction.EventsListUiAction.EventsListsClosed)
        if (!isOpeningEvent) {
            mapViewModel.handleUiAction(MapUiAction.MapDialogClosed)
        }
    }

    private fun handleFiltersNonDefault(nonDefaultFilters: Boolean) {
        binding.layoutEventsListsMain.vEventsListsMainFiltersNonDefault.isVisible = nonDefaultFilters
        binding.layoutEventsListsFilters.tvEventsListsFiltersReset.isEnabled = nonDefaultFilters
        binding.layoutEventsListsFilters.tvEventsListsFiltersReset.isVisible = nonDefaultFilters
    }

    companion object {
        private const val OFFSCREEN_PAGE_LIMIT = 1
        private const val DEFAULT_PARTICIPATION_INDEX = 0
    }
}

