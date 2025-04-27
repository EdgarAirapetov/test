package com.numplates.nomera3.modules.chatfriendlist.presentation

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.view.addOnScrollStateDragging
import com.meera.core.extensions.visible
import com.meera.core.utils.DEFAULT_ELEVATION_WHEN_RECYCLER_SCROLL
import com.meera.core.utils.setShadowWhenRecyclerScroll
import com.meera.db.models.userprofile.UserSimple
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentChatFriendListBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatCreatedFromWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriendsWhereProperty
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.chatfriendlist.MeeraChatFriendListAdapter
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.presentation.router.IArgContainer
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MeeraChatFriendListFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_chat_friend_list,
    behaviourConfigState = ScreenBehaviourState.Full
) {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraFragmentChatFriendListBinding::bind)

    private val friendListViewModel by viewModels<ChatFriendListViewModel> {
        App.component.getViewModelFactory()
    }

    private val friendListHeaderAdapter = MeeraCreateGroupChatAdapter {
        friendListViewModel.handleAction(ChatFriendListAction.OpenNewChatScreen)
    }
    private val friendListAdapter = MeeraChatFriendListAdapter { friend ->
        startDialog(friend)
    }
    private val concatAdapter = ConcatAdapter(friendListHeaderAdapter, friendListAdapter)

    private var infoSnackbar: UiKitSnackBar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFriendList()
        setupLiveDataObservers()
        setupFindFriendsView()
    }

    override fun onStart() {
        super.onStart()
        setupToolbar()
    }

    override fun onResume() {
        super.onResume()
        setupFriendSearch()
    }


    override fun onStop() {
        super.onStop()
        infoSnackbar?.dismiss()
    }

    private fun setupToolbar() {
        NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().clearListeners()

        binding.apply {
            chatFriendListNaw.backButtonClickListener = {
                findNavController().popBackStack()
            }

            chatFriendListNaw.title = getString(R.string.start_chat)
            setShadowWhenRecyclerScroll(
                recyclerView = rvFriendList,
                elevation = DEFAULT_ELEVATION_WHEN_RECYCLER_SCROLL,
                views = arrayOf(vgAppbar)
            )
        }
    }

    private fun setupFriendList() {
        binding.rvFriendList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = concatAdapter
            addOnScrollStateDragging { binding.isSearchFriends.hideKeyboard() }
        }
    }

    private fun setupLiveDataObservers() {
        friendListViewModel.friendList.distinctUntilChanged().observe(viewLifecycleOwner) {
            friendListHeaderAdapter.setVisibility(isVisible = true)
            friendListAdapter.submitList(null)
            friendListAdapter.submitList(it)
        }

        friendListViewModel.viewEventsLiveData.observe(viewLifecycleOwner) { viewEvent ->
            handleEvent(viewEvent)
        }
        friendListViewModel.effect
            .flowWithLifecycle(lifecycle)
            .filterNotNull()
            .onEach { effect ->
                handleEffect(effect)
            }
            .launchIn(lifecycleScope)


    }

    private fun setupFindFriendsView() {
        binding.btnFriendListPlaceholderAction.setThrottledClickListener {
            goToSearchScreen()
        }
    }

    private fun showGroupChatHeader(shouldShow: Boolean) {
        if (shouldShow) {
            concatAdapter.addAdapter(0, friendListHeaderAdapter)
        } else {
            concatAdapter.removeAdapter(friendListHeaderAdapter)
        }
    }

    private fun handleEvent(viewEvent: ChatFriendListViewEvent) {
        when (viewEvent) {
            is ChatFriendListViewEvent.Empty -> clearUi()
            is ChatFriendListViewEvent.FriendList -> showFriendList()
            is ChatFriendListViewEvent.NoFriends -> showEmptyFriendsPlaceholder()
            is ChatFriendListViewEvent.FailedToLoadFriendList -> showFailedToLoadFriendList()
            is ChatFriendListViewEvent.FailedToLoadFriendProfile -> showFailedToLoadFriendProfile()
            is ChatFriendListViewEvent.EmptySearchResult -> showEmptySearchResultPlaceholder()
            is ChatFriendListViewEvent.OpenNewChatScreen -> {
                /** STUB */
            }

            is ChatFriendListViewEvent.ChangeNewGroupChatButtonVisibility ->
                showGroupChatHeader(shouldShow = viewEvent.isVisible)
        }
    }

    private fun handleEffect(effect: ChatFriendListEffect) {
        when (effect) {
            is ChatFriendListEffect.OpenNewChatScreen -> goToNewGroupChatScreen()
        }
    }

    private fun clearUi() {
        binding.apply {
            rvFriendList.gone()
            llFriendListPlaceholder.gone()
        }
    }

    private fun showFriendList() {
        binding.apply {
            rvFriendList.visible()
            llFriendListPlaceholder.gone()
        }
    }

    private fun showEmptyFriendsPlaceholder() {
        binding.apply {
            rvFriendList.gone()
            llFriendListPlaceholder.visible()
            tvFriendListPlaceholderTitle.text = getString(R.string.meera_empty_friends_placeholder_title)
            tvFriendListPlaceholderDescription.apply {
                text = getString(R.string.meera_empty_friends_placeholder_description)
                visible()
            }
            btnFriendListPlaceholderAction.apply {
                visible()
                post { requestLayout() }
            }
        }
    }

    private fun showEmptySearchResultPlaceholder() {
        binding.apply {
            rvFriendList.gone()
            llFriendListPlaceholder.visible()
            tvFriendListPlaceholderTitle.text = getString(R.string.friendlist_search_results_empty)
        }
    }

    private fun showFailedToLoadFriendList() = showToast(R.string.chat_friend_list_error_load)

    private fun showFailedToLoadFriendProfile() = showToast(R.string.chat_friend_list_error_dialog)

    private fun showToast(@StringRes messageRes: Int) {
        infoSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(messageRes),
                    avatarUiState = AvatarUiState.ErrorIconState,
                )
            )
        )
        infoSnackbar?.show()
    }

    private fun setupFriendSearch() {
        binding.isSearchFriends.apply {
            doAfterSearchTextChanged { text ->
                if (text.isEmpty()) friendListHeaderAdapter.setVisibility(isVisible = false)
                friendListViewModel.onNewSearchQuery(text)
            }
            setCloseButtonClickedListener {
                requireContext().hideKeyboard(requireView())
            }
        }
    }

    private fun startDialog(friend: UserSimple) {
        lifecycleScope.launch {
            val chatInitProfileSettings = friendListViewModel.getChatInitProfileSettings(friend)
            if (chatInitProfileSettings != null) {
                val args = Bundle().apply {
                    putSerializable(IArgContainer.ARG_WHERE_CHAT_OPEN, AmplitudePropertyWhere.NEW_MESSAGE)
                    putSerializable(
                        IArgContainer.ARG_FROM_WHERE_CHAT_CREATED,
                        AmplitudePropertyChatCreatedFromWhere.COMMUNICATION
                    )
                    putParcelable(
                        IArgContainer.ARG_CHAT_INIT_DATA, ChatInitData(
                            initType = ChatInitType.FROM_PROFILE,
                            userId = friend.userId
                        )
                    )
                }
                findNavController().safeNavigate(
                    R.id.action_chatFriendListFragment_to_chatMessagesFragment,
                    args
                )
            }
        }
    }

    private fun goToNewGroupChatScreen() {
        findNavController().safeNavigate(R.id.action_chatFriendListFragment_to_groupChatListMembersFragment)
    }

    private fun goToSearchScreen() {
        findNavController().safeNavigate(
            resId = R.id.action_chatFriendListFragment_to_searchNavGraph,
            bundle = Bundle().apply {
                putSerializable(
                    IArgContainer.ARG_FIND_FRIENDS_OPENED_FROM_WHERE,
                    AmplitudeFindFriendsWhereProperty.MESSAGE
                )
            }
        )
    }

}
