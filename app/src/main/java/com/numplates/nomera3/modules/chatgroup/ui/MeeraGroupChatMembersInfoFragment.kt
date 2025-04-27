package com.numplates.nomera3.modules.chatgroup.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.gone
import com.meera.core.extensions.observeOnce
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.utils.DEFAULT_ELEVATION_WHEN_RECYCLER_SCROLL
import com.meera.core.utils.setShadowWhenRecyclerScroll
import com.meera.db.models.chatmembers.ChatMember
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.ErrorSnakeState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.USER_TYPE_ADMIN
import com.numplates.nomera3.data.newmessenger.USER_TYPE_COMPANION
import com.numplates.nomera3.data.newmessenger.USER_TYPE_CREATOR
import com.numplates.nomera3.data.newmessenger.USER_TYPE_MEMBER
import com.numplates.nomera3.databinding.MeeraFragmentGroupChatMembersInfoBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.viewmodel.ChatGroupShowUsersViewModel
import com.numplates.nomera3.presentation.viewmodel.NOT_YET_CREATED_ROOM_ID
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatGroupViewEvent

class MeeraGroupChatMembersInfoFragment: MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_group_chat_members_info,
    behaviourConfigState = ScreenBehaviourState.Full
), MeeraChatMemberInfoCallback {

    private val binding by viewBinding(MeeraFragmentGroupChatMembersInfoBinding::bind)
    private val showUsersViewModel by viewModels<ChatGroupShowUsersViewModel> { App.component.getViewModelFactory() }

    private var pagedUsersAdapter: MeeraGroupChatMembersInfoAdapter? = null
    private var roomId: Long? = null
    private var userType: String? = null
    private var userRole = USER_TYPE_MEMBER
    private var infoSnackbar: UiKitSnackBar? = null

    override val containerId: Int
        get() = R.id.fragment_first_container_view


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        roomId = arguments?.getLong(IArgContainer.ARG_ROOM_ID) ?: NOT_YET_CREATED_ROOM_ID
        userType = arguments?.getString(IArgContainer.ARG_ROOM_USER_TYPE)
        setupToolbar()
        initMembersTypes()
        initViewEventObservables()
        showUsersViewModel.startObserveRemoveMemberEvents()
    }

    override fun onStop() {
        super.onStop()
        infoSnackbar?.dismiss()
    }

    override fun onAvatarClicked(user: ChatMember?) {
        user?.userId?.let { id -> openUserProfile(id) }
    }

    private fun openUserProfile(userId: Long) {
        findNavController().safeNavigate(
            resId = R.id.action_groupChatMembersInfoFragment_to_userInfoFragment,
            bundle = Bundle().apply {
                putLong(IArgContainer.ARG_USER_ID, userId)
                putString(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.NEW_GROUP_CHAT.property)
            }
        )
    }

    override fun onGroupUserDotsClicked(user: ChatMember) {
        userType?.let { type ->
            var dialogType: MeeraGroupChatMembersInfoDialogType = MeeraGroupChatMembersInfoDialogType.AdminScreenCreator
            if (type == USER_TYPE_ADMIN) {
                dialogType = handleActionMenuForAdminsScreen(user)
            } else if (type == USER_TYPE_COMPANION) {
                dialogType = handleActionMenuForMembersScreen(user)
            }

            MeeraGroupChatMembersInfoDialog().show(
                fm = childFragmentManager,
                dialogType = dialogType,
                clickAction = { action -> handleBottomMenuDialogClickAction(action, user)}
            )
        }
    }

    private fun handleBottomMenuDialogClickAction(
        action: MeeraGroupChatMembersInfoDialogClickAction,
        user: ChatMember
    ) {
        val userId = user.userId
        val myUid = showUsersViewModel.getUserUid()
        when(action) {
            is MeeraGroupChatMembersInfoDialogClickAction.OpenProfile -> {
                openUserProfile(user.userId)
            }
            MeeraGroupChatMembersInfoDialogClickAction.AddAdmin -> {
                showUsersViewModel.setUserAdmin(roomId, userId)
            }
            MeeraGroupChatMembersInfoDialogClickAction.RemoveAdmin -> {
                showUsersViewModel.removeAdminFromChat(roomId, userId)
            }
            MeeraGroupChatMembersInfoDialogClickAction.BlockMember -> {
                showUsersViewModel.blockChatUser(myUid, user.userId, true, user)
            }
            MeeraGroupChatMembersInfoDialogClickAction.UnBlockMember -> {
                showUsersViewModel.blockChatUser(myUid, user.userId, false, user)
            }
            MeeraGroupChatMembersInfoDialogClickAction.RemoveMember -> {
                showUsersViewModel.removeUserFromChat(roomId, userId)
            }
        }
    }

    private fun handleActionMenuForAdminsScreen(user: ChatMember): MeeraGroupChatMembersInfoDialogType {
        return if (userRole == USER_TYPE_CREATOR && user.userId != showUsersViewModel.getUserUid()) {
            MeeraGroupChatMembersInfoDialogType.AdminScreenCreator
        } else {
            MeeraGroupChatMembersInfoDialogType.AdminScreenMember
        }
    }

    private fun handleActionMenuForMembersScreen(user: ChatMember): MeeraGroupChatMembersInfoDialogType {
        val isBlockUser = ((user.user.isBlockedByMe == null || user.user.isBlockedByMe == false)).not()
        val isShowAddAdmin = user.type == USER_TYPE_MEMBER
        return when (user.type) {
            USER_TYPE_CREATOR -> handleActionMenuForMembersScreenCreator(isBlockUser)
            USER_TYPE_ADMIN -> handleActionMenuForMembersScreenAdmin(isBlockUser, isShowAddAdmin)
            USER_TYPE_MEMBER -> handleActionMenuForMembersScreenMember(isBlockUser, isShowAddAdmin)
            else -> MeeraGroupChatMembersInfoDialogType.None
        }
    }

    private fun handleActionMenuForMembersScreenCreator(isBlockUser: Boolean): MeeraGroupChatMembersInfoDialogType {
        return if (userRole == USER_TYPE_CREATOR) {
            MeeraGroupChatMembersInfoDialogType.MembersScreenTypeCreatorRoleCreator
        } else {
            MeeraGroupChatMembersInfoDialogType.MembersScreenTypeCreatorRoleNotCreator(isBlockUser)
        }
    }

    private fun handleActionMenuForMembersScreenAdmin(
        isBlockUser: Boolean,
        isShowAddAdmin: Boolean
    ): MeeraGroupChatMembersInfoDialogType {
        return when (userRole) {
            USER_TYPE_CREATOR -> {
                return MeeraGroupChatMembersInfoDialogType.MembersScreenTypeAdminRoleCreator(isBlockUser, isShowAddAdmin)
            }
            USER_TYPE_ADMIN -> {
                return MeeraGroupChatMembersInfoDialogType.MembersScreenTypeAdminRoleAdmin(isBlockUser)
            }
            else -> MeeraGroupChatMembersInfoDialogType.None
        }
    }

    private fun handleActionMenuForMembersScreenMember(
        isBlockUser: Boolean,
        isShowAddAdmin: Boolean
    ): MeeraGroupChatMembersInfoDialogType {
        return when (userRole) {
            USER_TYPE_CREATOR -> {
                return MeeraGroupChatMembersInfoDialogType.MembersScreenTypeMemberRoleCreator(isBlockUser, isShowAddAdmin)
            }
            USER_TYPE_ADMIN -> {
                return MeeraGroupChatMembersInfoDialogType.MembersScreenTypeMemberRoleAdmin(isBlockUser, isShowAddAdmin)
            }
            else -> MeeraGroupChatMembersInfoDialogType.None
        }
    }

    private fun setupToolbar() {
        binding.apply {
            setShadowWhenRecyclerScroll(
                recyclerView = rvMembers,
                elevation = DEFAULT_ELEVATION_WHEN_RECYCLER_SCROLL,
                views = arrayOf(chatMembersNaw, tvAddAdmin)
            )
            chatMembersNaw.backButtonClickListener = { findNavController().popBackStack() }
        }
    }

    private fun setupRecycler(userRole: String) {
        pagedUsersAdapter = MeeraGroupChatMembersInfoAdapter(
            isShowDotsMenuItem = isShowDotsMenuItem(userRole),
            ownUid = showUsersViewModel.getUserUid(),
            memberItemListener = this
        )
        binding.rvMembers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pagedUsersAdapter
        }
    }

    private fun isShowDotsMenuItem(userRole: String): Boolean =
        userRole == USER_TYPE_CREATOR || userRole == USER_TYPE_ADMIN

    private fun initMembersTypes() {
        userType?.let { type ->
            if (type == USER_TYPE_ADMIN) {
                showAdmins(roomId)
            } else if (type == USER_TYPE_COMPANION) {
                showChatUsers(roomId)
            }
        }
    }

    private fun showChatUsers(roomId: Long?) {
        binding.tvAddAdmin.gone()
        showUsersViewModel.initPagingMembers(roomId, false)
        showUsersViewModel.checkGroupAdmin(roomId)
        showUsersViewModel.liveChatUserRole.observeOnce(viewLifecycleOwner) { role ->
            this.userRole = role
            setupRecycler(role)
            observeChatMembers()
        }
        showUsersViewModel.observeMoments()
    }

    private fun observeChatMembers() {
        showUsersViewModel.livePagedUsers.observe(viewLifecycleOwner) { members ->
            submitUsersAndSetScreenTitle(members)
        }
    }

    private fun showAdmins(roomId: Long?) {
        showUsersViewModel.checkGroupAdmin(roomId)
        showUsersViewModel.initPagingMembers(roomId, isAdmin = true)
        showUsersViewModel.liveChatUserRole.observeOnce(viewLifecycleOwner) { role ->
            this.userRole = role
            if (role == USER_TYPE_MEMBER) binding.tvAddAdmin.gone()
            setupRecycler(role)
            observeChatAdmins()
        }

        binding.tvAddAdmin.setThrottledClickListener {
            roomId?.let {
                findNavController().safeNavigate(
                    resId = R.id.action_groupChatMembersInfoFragment_to_groupChatListMembersFragment,
                    bundle = Bundle().apply {
                        putLong(IArgContainer.ARG_ROOM_ID, roomId)
                        putBoolean(IArgContainer.ARG_ROOM_ADD_ADMIN, true)
                    }
                )
            }
        }
    }

    private fun observeChatAdmins() {
        showUsersViewModel.livePagedUsers.observe(viewLifecycleOwner, Observer { admins ->
            addAdminsToCache(admins)
            submitUsersAndSetScreenTitle(admins)
        })
    }

    private fun addAdminsToCache(users: List<ChatMember>) {
        showUsersViewModel.addAdminsToCache(users)
    }

    private fun submitUsersAndSetScreenTitle(users: PagedList<ChatMember>) {
        pagedUsersAdapter?.submitList(users)
        binding.tvChatMembersTitle.text = context?.pluralString(R.plurals.group_members_plural, users.size)
    }

    private fun initViewEventObservables() {
        showUsersViewModel.liveViewEventShowUsers.observe(viewLifecycleOwner) { viewEvent ->
            handleViewEvents(viewEvent)
        }
    }

    private fun handleViewEvents(event: ChatGroupViewEvent) {
        when (event) {
            is ChatGroupViewEvent.ErrorUserMessage -> showErrorMessage(R.string.error_message_went_wrong)
            is ChatGroupViewEvent.OnSuccessBlockUser -> {
                event.isBlock?.let { isBlock ->
                    if (isBlock) {
                        showSnackbar(R.string.friends_block_user_success)
                    } else {
                        showSnackbar(R.string.friends_unblock_user_success)
                    }
                }
            }
            else -> Unit
        }
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

}
