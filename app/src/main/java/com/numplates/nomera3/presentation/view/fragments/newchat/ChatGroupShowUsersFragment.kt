package com.numplates.nomera3.presentation.view.fragments.newchat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.extensions.dp
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.toBoolean
import com.meera.db.models.chatmembers.ChatMember
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.USER_TYPE_ADMIN
import com.numplates.nomera3.data.newmessenger.USER_TYPE_COMPANION
import com.numplates.nomera3.data.newmessenger.USER_TYPE_CREATOR
import com.numplates.nomera3.data.newmessenger.USER_TYPE_MEMBER
import com.numplates.nomera3.databinding.FragmentChatShowUsersBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentScreenOpenWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_ROOM_USER_TYPE
import com.numplates.nomera3.presentation.view.adapter.newchat.IOnGroupUsersClicked
import com.numplates.nomera3.presentation.view.adapter.newchat.PagedGroupChatUsersAdapter
import com.numplates.nomera3.presentation.view.fragments.HorizontalLineDivider
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.ChatGroupShowUsersViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatGroupViewEvent


/**
 * Fragment for show list group members or admins
 */
class ChatGroupShowUsersFragment : BaseFragmentNew<FragmentChatShowUsersBinding>(),
    IOnGroupUsersClicked {

    private lateinit var pagedUsersAdapter: PagedGroupChatUsersAdapter

    private val showUsersViewModel by viewModels<ChatGroupShowUsersViewModel> { App.component.getViewModelFactory() }

    private var roomId: Long? = null
    private var userType: String? = null
    private var userRole = USER_TYPE_MEMBER

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentChatShowUsersBinding
        get() = FragmentChatShowUsersBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStatusBar(view)
        setupToolbar()
        roomId = arguments?.getLong(IArgContainer.ARG_ROOM_ID)
        userType = arguments?.getString(ARG_ROOM_USER_TYPE)

        // Init recycler
        pagedUsersAdapter = PagedGroupChatUsersAdapter(this)
        binding?.rvShowUsers?.apply {
            layoutManager = LinearLayoutManager(act)
            adapter = pagedUsersAdapter
            addItemDecoration(
                HorizontalLineDivider(
                    dividerDrawable = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.shared_divider_item_shape
                    )!!,
                    paddingLeft = 20.dp,
                    paddingRight = 20.dp
                )
            )
        }

        userType?.let { type ->
            if (type == USER_TYPE_ADMIN) {
                showChatAdmins(roomId)
            } else if (type == USER_TYPE_COMPANION) {
                showChatUsers(roomId)
            }
        }

        // Observers
        showUsersViewModel.liveViewEventShowUsers.observe(viewLifecycleOwner, Observer { viewEvent ->
            handleViewEvents(viewEvent)
        })
        binding?.srlSearchFriends?.isEnabled = false

        showUsersViewModel.startObserveRemoveMemberEvents()
    }

    private fun setupStatusBar(holder: View?) {
        val statusBar = holder?.findViewById<View>(R.id.status_bar_show_users)
        val params = statusBar?.layoutParams as AppBarLayout.LayoutParams
        params.height = context.getStatusBarHeight()
        statusBar.layoutParams = params
    }

    private fun setupToolbar() {
        binding?.toolbarShowUsers?.setNavigationIcon(R.drawable.arrowback)
        binding?.toolbarShowUsers?.setNavigationOnClickListener { act.onBackPressed() }
    }

    private fun showChatUsers(roomId: Long?) {
        hideAddAdminContainer()
        binding?.tvHeaderShowUsersTop?.text = getString(R.string.members)
        showUsersViewModel.initPagingMembers(roomId, false)
        showUsersViewModel.checkGroupAdmin(roomId)
        showUsersViewModel.liveChatUserRole.observe(viewLifecycleOwner, Observer { role ->
            this.userRole = role
        })

        showUsersViewModel.livePagedUsers.observe(viewLifecycleOwner, Observer { members ->
            pagedUsersAdapter.submitList(members)
            if (members.size > 0) {
                binding?.tvHeaderShowUsersTop?.text = getString(R.string.count_members, members.size)
            }
        })

        showUsersViewModel.observeMoments()
    }

    private fun showChatAdmins(roomId: Long?) {
        showUsersViewModel.checkGroupAdmin(roomId)
        showUsersViewModel.liveChatUserRole.observe(viewLifecycleOwner, Observer { role ->
            this.userRole = role
            if (role == USER_TYPE_MEMBER) {
                hideAddAdminContainer()
            }
        })

        binding?.tvHeaderShowUsersTop?.text = getString(R.string.administrators)

        showUsersViewModel.initPagingMembers(roomId, true)

        showUsersViewModel.livePagedUsers.observe(viewLifecycleOwner, Observer {
            pagedUsersAdapter.submitList(it)
        })

        // Add new admins (show users list)
        binding?.tvAddAdministrator?.setOnClickListener {
            add(
                ChatGroupFriendListFragment(), Act.LIGHT_STATUSBAR,
                Arg(IArgContainer.ARG_ROOM_ID, roomId),
                Arg(IArgContainer.ARG_ROOM_ADD_ADMIN, true)
            )
        }
    }

    override fun onGroupUserItemClicked(user: ChatMember?) {
        /** STUB */
    }

    override fun onAvatarClicked(
        user: ChatMember?,
        view: View?,
        hasNewMoments: Boolean?
    ) {
        if (isAvailableTransitToMoments(user)
            && (activity?.application as? FeatureTogglesContainer)?.momentsFeatureToggle?.isEnabled == true
        ) {
            user?.userId?.let { uid ->
                act.openUserMoments(
                    userId = uid,
                    fromView = view,
                    openedWhere = AmplitudePropertyMomentScreenOpenWhere.GROUP_CHAT_PARTICIPANTS,
                    viewedEarly = hasNewMoments?.not()
                )
            }
        }
    }

    override fun onGroupUserDotsClicked(user: ChatMember) {
        userType?.let { type ->
            if (type == USER_TYPE_ADMIN) {
                // Bottom menu for list admins screen
                if (userRole == USER_TYPE_CREATOR && user.userId != showUsersViewModel.getUserUid()) {
                    showBottomDialogCreatorAdminsScreen(user)
                } else {
                    showBottomDialogOpenProfile(user)
                }
            } else if (type == USER_TYPE_COMPANION) {
                // Bottom menu for list members screen
                when (user.type) {
                    USER_TYPE_CREATOR -> {
                        if (userRole == USER_TYPE_CREATOR) {
                            showBottomDialogOpenProfile(user)
                        } else {
                            showBottomDialogMember(user)
                        }
                    }

                    USER_TYPE_ADMIN -> {
                        when (userRole) {
                            USER_TYPE_CREATOR -> showBottomDialogCreator(user)
                            USER_TYPE_ADMIN -> showBottomDialogMember(user)
                            else -> showBottomDialogMember(user)
                        }
                    }

                    USER_TYPE_MEMBER -> {
                        when (userRole) {
                            USER_TYPE_CREATOR -> showBottomDialogCreator(user)
                            USER_TYPE_ADMIN -> showBottomDialogAdmin(user)
                            else -> showBottomDialogMember(user)
                        }
                    }
                }
            }
        }
    }

    /**
     * Bottom menu for list admins screen
     */
    private fun showBottomDialogCreatorAdminsScreen(user: ChatMember) {
        val userId = user.userId
        val menu = MeeraMenuBottomSheet(context)
        menu.addItem(R.string.menu_chat_delete_admin, R.drawable.remove_admin_group_menu_item) {
            showUsersViewModel.removeAdminFromChat(roomId, userId)
        }
        addItemMenuDeleteFromChat(menu, userId)
        menu.show(childFragmentManager)
    }

    private fun showBottomDialogOpenProfile(user: ChatMember) {
        val menu = MeeraMenuBottomSheet(context)
        addItemMenuOpenProfile(menu, user.userId)
        menu.show(childFragmentManager)
    }

    private fun showBottomDialogCreator(user: ChatMember) {
        val userId = user.userId
        val menu = MeeraMenuBottomSheet(context)
        addItemMenuOpenProfile(menu, userId)
        addItemMenuCall()
        if (user.type == USER_TYPE_MEMBER) {
            addItemMenuAddAdmin(menu, userId)
        } else if (user.type == USER_TYPE_ADMIN) {
            addItemMenuRemoveAdmin(menu, userId)
        }
        addItemMenuNotifications()
        addItemMenuDeleteFromChat(menu, userId)
        if (user.user.isBlockedByMe == null || user.user.isBlockedByMe == false)
            addItemMenuBlockUser(menu, user)
        else addItemMenuUnblockUser(menu, user)
        menu.show(childFragmentManager)
    }

    private fun showBottomDialogAdmin(user: ChatMember) {
        val userId = user.userId
        val menu = MeeraMenuBottomSheet(context)
        addItemMenuOpenProfile(menu, userId)
        addItemMenuCall()
        if (user.type == USER_TYPE_MEMBER) {
            addItemMenuAddAdmin(menu, userId)
        } else if (user.type == USER_TYPE_ADMIN) {
            addItemMenuRemoveAdmin(menu, userId)
        }
        addItemMenuNotifications()
        if (user.type != USER_TYPE_CREATOR || user.type != USER_TYPE_ADMIN) {
            addItemMenuDeleteFromChat(menu, userId)
        }
        if (user.user.isBlockedByMe == null || user.user.isBlockedByMe == false) {
            addItemMenuBlockUser(menu, user)
        } else {
            addItemMenuUnblockUser(menu, user)
        }
        menu.show(childFragmentManager)
    }

    private fun showBottomDialogMember(user: ChatMember) {
        val userId = user.userId
        val menu = MeeraMenuBottomSheet(context)
        addItemMenuOpenProfile(menu, userId)
        addItemMenuCall()
        addItemMenuNotifications()
        if (user.user.isBlockedByMe == null || user.user.isBlockedByMe == false)
            addItemMenuBlockUser(menu, user)
        else addItemMenuUnblockUser(menu, user)
        menu.show(childFragmentManager)
    }

    private fun addItemMenuOpenProfile(menu: MeeraMenuBottomSheet, userId: Long) {
        menu.addItem(R.string.menu_chat_open_profile, R.drawable.open_profile_menu_item) {
            add(
                UserInfoFragment(), Act.LIGHT_STATUSBAR,
                Arg(IArgContainer.ARG_USER_ID, userId),
                Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.CHAT.property)
            )
        }
    }

    private fun addItemMenuAddAdmin(menu: MeeraMenuBottomSheet, userId: Long) {
        menu.addItem(R.string.menu_chat_set_administrator, R.drawable.add_admin_group_menu_item) {
            showUsersViewModel.setUserAdmin(roomId, userId)
        }
    }

    private fun addItemMenuRemoveAdmin(menu: MeeraMenuBottomSheet, userId: Long) {
        menu.addItem(R.string.menu_chat_delete_admin, R.drawable.remove_admin_group_menu_item) {
            showUsersViewModel.removeAdminFromChat(roomId, userId)
        }
    }

    private fun addItemMenuCall() {
        /** STUB */
    }

    private fun addItemMenuDeleteFromChat(menu: MeeraMenuBottomSheet, userId: Long) {
        menu.addItem(R.string.menu_chat_delete_from_chat, R.drawable.remove_menu_item) {
            showUsersViewModel.removeUserFromChat(roomId, userId)
        }
    }

    private fun addItemMenuNotifications() {
        /** STUB */
    }

    private fun addItemMenuBlockUser(menu: MeeraMenuBottomSheet, user: ChatMember) {
        showUsersViewModel.getUserUid().let { myUid ->
            if (myUid != user.userId) {
                menu.addItem(R.string.menu_chat_block_user, R.drawable.block_user_menu_item) {
                    showUsersViewModel.blockChatUser(myUid, user.userId, true, user)
                }
            }
        }
    }

    private fun addItemMenuUnblockUser(menu: MeeraMenuBottomSheet, user: ChatMember) {
        showUsersViewModel.getUserUid().let { myUid ->
            if (myUid != user.userId) {
                menu.addItem(R.string.unblock_user_txt, R.drawable.block_user_menu_item_v2) {
                    showUsersViewModel.blockChatUser(myUid, user.userId, false, user)
                }
            }
        }
    }

    private fun handleViewEvents(event: ChatGroupViewEvent) {
        when (event) {
            is ChatGroupViewEvent.ErrorUserMessage -> {
                showUserMessage(event.mesage ?: getString(R.string.error_message_went_wrong))
            }
            // Block user
            is ChatGroupViewEvent.OnSuccessBlockUser -> {
                event.isBlock?.let { isBlock ->
                    if (isBlock) {
                        NToast.with(view)
                            .text(getString(R.string.friends_block_user_success))
                            .typeSuccess()
                            .show()
                    } else {
                        NToast.with(view)
                            .text(getString(R.string.friends_unblock_user_success))
                            .typeSuccess()
                            .show()
                    }
                }
            }

            else -> {}
        }
    }

    private fun hideAddAdminContainer() {
        binding?.addAdminContainer?.gone()
        binding?.dividerAddAdmin?.gone()
    }

    private fun showUserMessage(message: String) {
        Toast.makeText(act, message, Toast.LENGTH_LONG).show()
    }

    private fun isAvailableTransitToMoments(member: ChatMember?): Boolean {
        val m = member?.user?.moments
        return m != null && m.hasMoments.toBoolean()
    }

}
