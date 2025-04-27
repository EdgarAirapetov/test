package com.numplates.nomera3.modules.chatgroup.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.ErrorSnakeState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.FriendEntity
import com.numplates.nomera3.databinding.MeeraFragmentGroupChatListMembersBinding
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.viewmodel.ChatGroupFriendListViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatGroupEffect
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatGroupViewEvent
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val MEMBERS_REQUEST_LIMIT = 1000

class MeeraGroupChatListMembersFragment: MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_group_chat_list_members,
    behaviourConfigState = ScreenBehaviourState.Full
), MeeraGroupChatListMembersAdapter.MeeraOnGroupMembersListener {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraFragmentGroupChatListMembersBinding::bind)

    private val viewModelMembers by viewModels<ChatGroupFriendListViewModel> {
        App.component.getViewModelFactory()
    }
    private val listAdapter = MeeraGroupChatListMembersAdapter(this)
    private var infoSnackbar: UiKitSnackBar? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecycler()
        setupLiveDataObservers()
        requestGetFriends()
        addNewUsersWhenRoomExists()
        checkScreenMode()
    }

    override fun onResume() {
        super.onResume()
        searchUser()
    }

    override fun onStop() {
        super.onStop()
        infoSnackbar?.dismiss()
    }

    override fun onMemberSelected(user: FriendEntity) {
        viewModelMembers.selectFriend(user)
    }

    private fun setupToolbar() {
        binding.apply {
            chatListMembersNaw.backButtonClickListener = {
                findNavController().popBackStack()
            }
            chatListMembersNaw.title = getString(R.string.meera_members_selection)
            setShadowWhenRecyclerScroll(
                recyclerView = rvMembers,
                elevation = DEFAULT_ELEVATION_WHEN_RECYCLER_SCROLL,
                views = arrayOf(vgAppbarGroupChatListMebers)
            )
        }
    }

    private fun addNewUsersWhenRoomExists() {
        arguments?.getLong(IArgContainer.ARG_ROOM_ID)?.let { roomId ->
            if (roomId > 0) {
                binding.chatListMembersNaw.title = getString(R.string.add_members)
            }
        }
    }

    // Check transit from addUsers or addAdmins screens
    private fun checkScreenMode() {
        val roomId = arguments?.getLong(IArgContainer.ARG_ROOM_ID)
        val isRoomAddAdmin = arguments?.getBoolean(IArgContainer.ARG_ROOM_ADD_ADMIN)
        if (isRoomAddAdmin != null && isRoomAddAdmin == true) {
            addNewMembers(roomId)
        } else {
            gotoNewGroupChatScreen(roomId)
        }
    }

    private fun setupRecycler() {
        binding.rvMembers.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = listAdapter
            itemAnimator = null
            addOnScrollStateDragging { binding.isSearchMembers.hideKeyboard() }
        }
    }

    private fun setupLiveDataObservers() {
        viewModelMembers.liveListUsers.observe(viewLifecycleOwner) { userList ->
            submitList(userList)
            shouldShowPlaceholder(shouldShow = userList.isEmpty())
            editButtonChooseSelector(friends = userList)
        }

        viewModelMembers.liveViewEvents.observe(viewLifecycleOwner) { viewEvents ->
            handleEvents(viewEvents)
        }
        viewModelMembers.effect
            .flowWithLifecycle(lifecycle)
            .filterNotNull()
            .onEach { effect ->
                handleEffect(effect)
            }
            .launchIn(lifecycleScope)
    }

    private fun submitList(users: List<FriendEntity>) {
        val updList = mutableListOf<FriendEntity>()
        updList.addAll(users)
        if (users.isNotEmpty()) listAdapter.submitList(updList)
    }

    // Load friends depends on transit screen status (addUsers / addAdmins)
    private fun requestGetFriends() {
        val roomId = arguments?.getLong(IArgContainer.ARG_ROOM_ID) ?: 0
        val isRoomAddAdmin = arguments?.getBoolean(IArgContainer.ARG_ROOM_ADD_ADMIN) ?: false
        val userId: Long = viewModelMembers.getUserUid()

        viewModelMembers.getFriends(
            roomId = roomId,
            isRoomAdmin = isRoomAddAdmin,
            userId = userId,
            limit = MEMBERS_REQUEST_LIMIT,
            offset = 0,
        )
    }

    private fun handleEvents(event: ChatGroupViewEvent) {
        when (event) {
            is ChatGroupViewEvent.ErrorLoadFriendList -> showAlertError(R.string.error_chat_load_friends)
            is ChatGroupViewEvent.AdminsAdded -> addAdminSuccess()
            is ChatGroupViewEvent.MembersAdded -> addMembersSuccess()
            is ChatGroupViewEvent.ErrorUserMessage -> findNavController().popBackStack()
            is ChatGroupViewEvent.ErrorChooseFriends -> showAlertError(R.string.group_chat_choose_friend)
            is ChatGroupViewEvent.EditChatUsers -> Unit
            else -> Unit
        }
    }

    private fun handleEffect(effect: ChatGroupEffect) {
        when(effect) {
            is ChatGroupEffect.EditChatUsers -> gotoGroupEditScreen(effect.roomId)
        }
    }

    private fun searchUser() {
        binding.isSearchMembers.apply {
            doAfterSearchTextChanged { text ->
                viewModelMembers.onNewQuery(text)
            }
            setCloseButtonClickedListener {
                requireContext().hideKeyboard(requireView())
            }
        }
    }

    private fun editButtonChooseSelector(friends: List<FriendEntity>) {
        val checkedItem = friends.find { user -> user.isChecked }
        binding.btnNextCreateGroupChat.isEnabled = checkedItem != null
    }

    private fun addNewMembers(roomId: Long?) {
        binding.btnNextCreateGroupChat.setThrottledClickListener {
            val members = viewModelMembers.getListFriends()
            if (members.isNotEmpty()) {
                viewModelMembers.addNewAdmins(roomId, members.toList())
            } else {
                showAlertError(R.string.group_chat_choose_friend)
            }
        }
    }

    private fun gotoNewGroupChatScreen(roomId: Long?) {
        binding.btnNextCreateGroupChat.setThrottledClickListener {
            viewModelMembers.gotoEditGroupScreen(roomId)
        }
    }

    private fun gotoGroupEditScreen(roomId: Long?) {
        findNavController().safeNavigate(
            resId = R.id.action_groupChatListMembersFragment_to_createGroupChatFragment,
            bundle = Bundle().apply {
                putLong(IArgContainer.ARG_ROOM_ID, roomId ?: 0)
                putSerializable(IArgContainer.ARG_CREATE_OR_EDIT_CHAT, MeeraCreateGroupChatFragment.ChatInfoScreenMode.CREATING_CHAT)
            }
        )
    }

    private fun showAlertError(@StringRes messageRes: Int) {
        showErrorMessage(messageRes)
    }

    private fun addAdminSuccess() {
        showSnackbar(R.string.group_chat_admins_added)
        findNavController().popBackStack()
    }

    private fun addMembersSuccess() {
        showSnackbar(R.string.group_chat_members_added)
        findNavController().popBackStack()
    }

    private fun showSnackbar(@StringRes messageRes: Int) {
        infoSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(messageRes),
                    avatarUiState = AvatarUiState.SuccessIconState,
                )
            )
        )
        infoSnackbar?.show()
    }

    private fun showErrorMessage(@StringRes messageRes: Int) {
        infoSnackbar = UiKitSnackBar.makeError(
            view = requireView(),
            params = SnackBarParams(
                errorSnakeState = ErrorSnakeState(
                    messageText = getText(messageRes)
                )
            )
        )
        infoSnackbar?.show()
    }

    private fun shouldShowPlaceholder(shouldShow: Boolean) {
        if (shouldShow) {
            binding.apply {
                rvMembers.gone()
                llFriendListPlaceholder.visible()
                tvFriendListPlaceholderTitle.text =  getString(R.string.friendlist_search_results_empty)
            }
        } else {
            binding.llFriendListPlaceholder.gone()
            binding.rvMembers.visible()
        }
    }

}
