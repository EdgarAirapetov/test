package com.numplates.nomera3.presentation.view.fragments.newchat

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.extensions.dp
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.FriendEntity
import com.numplates.nomera3.databinding.FragmentChatMembersSelectionBinding
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.adapter.newchat.ChatGroupRecyclerAdapter
import com.numplates.nomera3.presentation.view.fragments.HorizontalLineDivider
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.ChatGroupFriendListViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatGroupViewEvent
import timber.log.Timber

class ChatGroupFriendListFragment : BaseFragmentNew<FragmentChatMembersSelectionBinding>(),
        ChatGroupRecyclerAdapter.OnGroupFriendsListener {

    private lateinit var chatGroupAdapter: ChatGroupRecyclerAdapter

    private val viewModelMembers by viewModels<ChatGroupFriendListViewModel> { App.component.getViewModelFactory() }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentChatMembersSelectionBinding
        get() = FragmentChatMembersSelectionBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStatusBar(view)
        setupToolbar()
        setupPlaceholder()

        val roomId = arguments?.getLong(IArgContainer.ARG_ROOM_ID)
        val isRoomAddAdmin = arguments?.getBoolean(IArgContainer.ARG_ROOM_ADD_ADMIN)

        // When invoice new users and roomId already exists
        addNewUsers(roomId)

        Handler().postDelayed({
            try {
                requestGetFriends()

                // Check transit from addUsers or addAdmins screens
                if (isRoomAddAdmin != null && isRoomAddAdmin == true) {
                    addNewAdmins(roomId)
                } else {
                    gotoEditGroupScreen(roomId)
                }

                setupRecyclerList()

                setupLiveDataObservers()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 300)
    }

    override fun onResume() {
        super.onResume()
        searchUser()
    }

    override fun onFriendSelected(user: FriendEntity) {
        viewModelMembers.selectFriend(user)
    }

    private fun setupStatusBar(holder: View?) {
        val statusBar = holder?.findViewById<View>(R.id.status_bar_friend_list)
        val params = statusBar?.layoutParams as AppBarLayout.LayoutParams
        params.height = context.getStatusBarHeight()
        statusBar.layoutParams = params
    }

    // Load friends depends on transit screen status (addUsers / addAdmins)
    private fun requestGetFriends() {
        val roomId = arguments?.getLong(IArgContainer.ARG_ROOM_ID)
        val isRoomAddAdmin = arguments?.getBoolean(IArgContainer.ARG_ROOM_ADD_ADMIN) ?: false
        val userId: Long = viewModelMembers.getUserUid()

        viewModelMembers.getFriends(
            roomId = roomId,
            isRoomAdmin = isRoomAddAdmin,
            userId = userId,
            limit = 1000,
            offset = 0,
        )
    }

    private fun setupToolbar() {
        binding?.btnGoBack?.setOnClickListener {
            binding?.sbvMembersFilter?.clearText()
            act.onBackPressed()
        }
    }

    private fun setupRecyclerList() {
        chatGroupAdapter = ChatGroupRecyclerAdapter(act, this)
        val dividerDecorator = maybeGetListDecorator()
        binding?.rvFriendList?.apply {
            setHasFixedSize(true)
            adapter = chatGroupAdapter
            itemAnimator = null
            if (dividerDecorator != null) {
                addItemDecoration(dividerDecorator)
            }
        }
    }

    private fun maybeGetListDecorator(): HorizontalLineDivider? {
        val dividerDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.drawable_friend_list_divider_decoration)
        return if (dividerDrawable != null) {
            HorizontalLineDivider(
                dividerDrawable = dividerDrawable,
                paddingLeft = 20.dp,
                paddingRight = 20.dp
            )
        } else null
    }

    private fun setupLiveDataObservers() {
        // Show friends
        viewModelMembers.liveListUsers.observe(viewLifecycleOwner) { userList ->
            chatGroupAdapter.submitList(userList)
            Timber.d("liveListUsers: $userList;")
            shouldShowPlaceholder(shouldShow = userList.isEmpty())
            editButtonChooseSelector(friends = userList)
        }

        // Observe and handle view events
        viewModelMembers.liveViewEvents.observe(viewLifecycleOwner) { viewEvents ->
            handleEvents(viewEvents)
        }
    }

    private fun searchUser() {
        binding?.apply {
            val flow = sbvMembersFilter.getTextChangesFlow()
                .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            lifecycleScope.launchWhenResumed {
                flow.collect {
                    viewModelMembers.onNewQuery(it.toString()) }
            }
        }
    }

    /**
     * Add new Group users
     */
    private fun gotoEditGroupScreen(roomId: Long?) {
        binding?.tvNextStep?.setOnClickListener {
            viewModelMembers.gotoEditGroupScreen(roomId)
        }
    }

    /**
     * Add new admins
     */
    private fun addNewAdmins(roomId: Long?) {
        binding?.tvNextStep?.setOnClickListener {
            val listAdmins = viewModelMembers.getListFriends()
            if (listAdmins.isNotEmpty()) {
                viewModelMembers.addNewAdmins(roomId, listAdmins.toList())
            } else {
                NToast.with(view)
                        .text(getString(R.string.group_chat_choose_friend))
                        .typeAlert()
                        .show()
            }
        }
    }

    private fun handleEvents(event: ChatGroupViewEvent) {
        when (event) {
            is ChatGroupViewEvent.ErrorLoadFriendList ->
                NToast.with(view)
                        .text(getString(R.string.error_chat_load_friends))
                        .show()
            is ChatGroupViewEvent.AdminsAdded -> addAdminSuccess()
            is ChatGroupViewEvent.MembersAdded -> addMembersSuccess()
            is ChatGroupViewEvent.ErrorUserMessage -> act.onBackPressed()
            is ChatGroupViewEvent.ErrorChooseFriends -> showAlertError(getString(R.string.group_chat_choose_friend))
            is ChatGroupViewEvent.EditChatUsers -> gotoGroupEditScreen(event.roomId)
            else -> {}
        }
    }

    private fun gotoGroupEditScreen(roomId: Long?) {
        add(
            ChatGroupEditFragment(),
            Act.LIGHT_STATUSBAR,
            Arg(IArgContainer.ARG_ROOM_ID, roomId),
            Arg(
                IArgContainer.ARG_CREATE_OR_EDIT_CHAT,
                ChatGroupEditFragment.ChatInfoScreenMode.CREATING_CHAT
            )
        )
    }

    /**
     * Change color edit button depends on friends selected
     */
    private fun editButtonChooseSelector(friends: List<FriendEntity>) {
        val checkedItem = friends.find { user -> user.isChecked }
        if (checkedItem != null) {
            binding?.tvNextStep?.setTextColor(ContextCompat.getColor(act, R.color.colorPrimary))
        } else {
            binding?.tvNextStep?.setTextColor(ContextCompat.getColor(act, R.color.ui_gray))
        }
    }

    private fun setupPlaceholder() {
        binding?.vgPlaceholderContainer?.apply {
            ivPlaceholderImage.setImageDrawable(
                ContextCompat.getDrawable(act as Context, R.drawable.ic_empty_search_results)
            )
            tvPlaceholderText.text = getString(R.string.placeholder_empty_search_result)
            tvPlaceholderAction.gone()
            root.gone()
        }
    }

    private fun shouldShowPlaceholder(shouldShow: Boolean) {
        binding?.vgPlaceholderContainer?.root?.isVisible = shouldShow
    }

    private fun addNewUsers(roomId: Long?) {
        roomId?.let {
            if (roomId > 0) {
                val toolbarTitle = view?.findViewById<TextView>(R.id.tv_header_members_selection_top)
                toolbarTitle?.text = getString(R.string.invite_to_chat)
            }
        }
    }

    private fun showAlertError(message: String) {
        NToast.with(view)
            .text(message)
            .typeAlert()
            .show()
    }

    private fun addAdminSuccess() {
        NToast.with(view)
                .text(getString(R.string.group_chat_admins_added))
                .typeSuccess()
                .show()
        act.onBackPressed()
    }

    private fun addMembersSuccess() {
        NToast.with(view)
                .text(getString(R.string.group_chat_members_added))
                .typeSuccess()
                .show()
        act.onBackPressed()
    }
}
