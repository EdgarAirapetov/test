package com.numplates.nomera3.presentation.view.fragments.newchat

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.empty
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.pxToDp
import com.meera.core.extensions.visible
import com.meera.db.models.chatmembers.ChatMember
import com.meera.db.models.dialog.DialogEntity
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.USER_TYPE_ADMIN
import com.numplates.nomera3.data.newmessenger.USER_TYPE_COMPANION
import com.numplates.nomera3.data.newmessenger.USER_TYPE_CREATOR
import com.numplates.nomera3.databinding.FragmentGroupChatAboutBinding
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegate
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegateImpl
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.adapter.newchat.FriendsProfileListAdapterNew
import com.numplates.nomera3.presentation.view.callback.ProfileVehicleListCallback
import com.numplates.nomera3.presentation.view.ui.OverlapDecoration
import com.numplates.nomera3.presentation.view.ui.mediaViewer.ImageViewerData
import com.numplates.nomera3.presentation.view.ui.mediaViewer.MediaViewer
import com.numplates.nomera3.presentation.view.utils.NTime
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.widgets.BannerLayoutManager
import com.numplates.nomera3.presentation.viewmodel.ChatGroupEditViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatGroupViewEvent

class ChatGroupAboutFragment :
    BaseFragmentNew<FragmentGroupChatAboutBinding>(),
    SaveMediaFileDelegate by SaveMediaFileDelegateImpl()
{

    private val viewModelGroupEdit by viewModels<ChatGroupEditViewModel> { App.component.getViewModelFactory() }

    private var dialog: DialogEntity? = null

    private var roomId: Long? = null

    private lateinit var friendsAdapter: FriendsProfileListAdapterNew
    private var widthContainerMembers = 0

    private lateinit var adminsAdapter: FriendsProfileListAdapterNew
    private var widthContainerAdmins = 0

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGroupChatAboutBinding
        get() = FragmentGroupChatAboutBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStatusBar(view)
        setupToolbar()
        val roomId = arguments?.getLong(IArgContainer.ARG_ROOM_ID)
        viewModelGroupEdit.showChatMembers(roomId)
        initMembersRecyclerList()
        initAdminsRecyclerList()
        initObservables(roomId)
    }

    override fun onReturnTransitionFragment() {
        super.onReturnTransitionFragment()
        val roomId = arguments?.getLong(IArgContainer.ARG_ROOM_ID)
        viewModelGroupEdit.reloadDialogs(roomId)
    }

    private fun initObservables(roomId: Long?) {
        viewModelGroupEdit.getGroupData(roomId).observe(viewLifecycleOwner, Observer { dialog ->
            if (dialog != null) {
                this.roomId = dialog.roomId
                renderGroupInfoViews(dialog)
            }
        })

        viewModelGroupEdit.liveGroupEditEvents.observe(viewLifecycleOwner, Observer { event ->
            handleEvents(event)
        })

        viewModelGroupEdit.getChatMembers(roomId).observe(viewLifecycleOwner, Observer { members ->
            // Limit pins
            val maxPins = pxToDp(widthContainerMembers) / 36
            if (members.size > maxPins) {
                friendsAdapter.setData(members.subList(0, maxPins))
            } else
                friendsAdapter.setData(members)


            renderGroupChatAdmins(members)
            gotoChatSettingsScreen(members)
        })
    }

    private fun handleEvents(event: ChatGroupViewEvent) {
        when (event) {
            is ChatGroupViewEvent.GroupChatDeleted -> chatDeletedAction()
            is ChatGroupViewEvent.ErrorChatDeleted ->
                NToast.with(view)
                        .text(getString(R.string.delete_chat_failure_toast))
                        .show()
            else -> {}
        }
    }

    private fun chatDeletedAction() {
        NToast.with(view)
                .text(getString(R.string.delete_chat_success_toast))
                .typeSuccess()
                .show()
        act.returnToTargetFragment(0, true)
    }

    private fun renderGroupInfoViews(dialog: DialogEntity) {
        this.dialog = dialog

        binding?.ivGroupChatAvatar?.let { imageView ->
            Glide.with(this)
                    .load(dialog.groupAvatar)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.group_chat_avatar_circle)
                    .into(imageView)

            // Show group chat avatar
            dialog.groupAvatar?.let { url ->
                if (url.isNotEmpty()) {
                    binding?.ivGroupChatAvatar?.setOnClickListener {
                        showGroupAvatarImage(url, imageView)
                    }
                }
            }
        }

        binding?.tvGroupChatName?.text = dialog.title
        binding?.tvGroupChatCreated?.text = NTime.timeAgo(dialog.createdAt / 1000)
        binding?.tvGroupChatDescription?.text = dialog.description
        binding?.tvGroupChatMembersCount?.text = dialog.membersCount.toString()
        binding?.tvGroupChatAdminsCount?.text = dialog.adminsCount.toString()

        // Add members if Admin or Creator
        binding?.tvGroupChatAddMembers?.setOnClickListener {
            add(ChatGroupFriendListFragment(), Act.LIGHT_STATUSBAR,
                    Arg(IArgContainer.ARG_ROOM_ID, dialog.roomId))
        }

        // Delete group chat (ONLY Creator can delete group chat)
        if (dialog.creator.userId == viewModelGroupEdit.getUserUid()) {
            binding?.tvDeleteGroupChat?.visible()
            binding?.tvDeleteGroupChat?.setOnClickListener {
                showDeleteGroupDialog(dialog.roomId)
            }
        }

        // Goto users screen
        binding?.tvGroupChatMembers?.setOnClickListener {
            gotoShowUsersScreen(dialog.roomId, USER_TYPE_COMPANION)
        }

        // Goto admins screen
        binding?.tvGroupChatAdmins?.setOnClickListener {
            gotoShowUsersScreen(dialog.roomId, USER_TYPE_ADMIN)
        }
    }

    private fun initMembersRecyclerList() {
        val bannerLayoutManagerMembers =
                BannerLayoutManager(context, RecyclerView.HORIZONTAL, false)
        friendsAdapter = FriendsProfileListAdapterNew.Builder(act)
                .callback(object : ProfileVehicleListCallback() {
                    override fun getZeroDataImageId() = 0

                    override fun getZeroDataText() = String.empty()

                    override fun onClick(holder: RecyclerView.ViewHolder?) {
                        dialog?.let { dlg ->
                            gotoShowUsersScreen(dlg.roomId, USER_TYPE_COMPANION)
                        }
                    }
                })
                .data(mutableListOf())
                .build()

        val rvGroupChatMembers = binding?.rvGroupChatMembers
        rvGroupChatMembers?.setHasFixedSize(true)
        rvGroupChatMembers?.layoutManager = bannerLayoutManagerMembers
        rvGroupChatMembers?.isNestedScrollingEnabled = false
        rvGroupChatMembers?.adapter = friendsAdapter
        rvGroupChatMembers?.addItemDecoration(OverlapDecoration(dpToPx(18)))

        binding?.membersPinContainer?.post {
            widthContainerMembers = binding?.membersPinContainer?.width ?: 0
        }
    }

    private fun initAdminsRecyclerList() {
        val bannerLayoutManagerAdmins =
                BannerLayoutManager(context, RecyclerView.HORIZONTAL, false)
        adminsAdapter = FriendsProfileListAdapterNew.Builder(act)
                .callback(object : ProfileVehicleListCallback() {
                    override fun getZeroDataImageId() = 0

                    override fun getZeroDataText() = String.empty()

                    override fun onClick(holder: RecyclerView.ViewHolder?) {
                        dialog?.let { dlg ->
                            gotoShowUsersScreen(dlg.roomId, USER_TYPE_ADMIN)
                        }
                    }
                })
                .data(mutableListOf())
                .build()

        val rvGroupChatAdmins = binding?.rvGroupChatAdmins
        rvGroupChatAdmins?.setHasFixedSize(true)
        rvGroupChatAdmins?.layoutManager = bannerLayoutManagerAdmins
        rvGroupChatAdmins?.isNestedScrollingEnabled = false
        rvGroupChatAdmins?.adapter = adminsAdapter
        rvGroupChatAdmins?.addItemDecoration(OverlapDecoration(dpToPx(18)))

        binding?.adminsPinsContainer?.post {
            widthContainerAdmins = binding?.adminsPinsContainer?.width ?: 0
        }
    }

    /**
     * Show admins list avatars
     */
    private fun renderGroupChatAdmins(members: List<ChatMember>) {
        val admins = mutableListOf<ChatMember>()
        members.forEach { member ->
            if (member.type == USER_TYPE_ADMIN || member.type == USER_TYPE_CREATOR) {
                admins.add(member)
            }
        }

        // Limit pins
        val maxPins = pxToDp(widthContainerAdmins) / 36
        if (admins.size > maxPins) {
            adminsAdapter.setData(admins.subList(0, maxPins))
        } else
            adminsAdapter.setData(admins)

        handleAddMemberBtnVisibility(admins)
    }

    private fun handleAddMemberBtnVisibility(members: List<ChatMember>?) {
        if (members.isNullOrEmpty()) return
        members.forEach {
            if (it.user.userId == viewModelGroupEdit.getUserUid()){
                binding?.tvGroupChatAddMembers?.visible()
            }
        }
    }

    private fun setupStatusBar(holder: View?) {
        val statusBar = holder?.findViewById<View>(R.id.status_bar_group_chat_edit)
        val params = statusBar?.layoutParams as AppBarLayout.LayoutParams
        params.height = context.getStatusBarHeight()
        statusBar.layoutParams = params
    }

    private fun setupToolbar() {
        binding?.toolbarGroupChatEdit?.setNavigationIcon(R.drawable.arrowback)
        binding?.toolbarGroupChatEdit?.setNavigationOnClickListener { act.onBackPressed() }
    }

    private fun showDeleteGroupDialog(roomId: Long?) {
        AlertDialog.Builder(act)
                .setTitle(R.string.delete_chat_dialog_title)
                .setMessage(R.string.delete_chat_dialog_message)
                .setPositiveButton(R.string.general_delete) { dialog, which ->
                    viewModelGroupEdit.deleteGroupChat(roomId, true)
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.general_cancel) { dialog, which ->
                    dialog.dismiss()
                }
                .show()
    }

    /**
     * Show group chat settings button for creator or admin
     */
    private fun gotoChatSettingsScreen(members: List<ChatMember>) {
        roomId?.let { id ->
            for (member in members) {
                if (member.user.userId == viewModelGroupEdit.getUserUid()
                        && (member.type == USER_TYPE_ADMIN || member.type == USER_TYPE_CREATOR)) {
                    binding?.ivGroupChatEdit?.visible()
                    break
                }
            }

            binding?.ivGroupChatEdit?.setOnClickListener {
                add(
                    ChatGroupEditFragment(),
                    Act.LIGHT_STATUSBAR,
                    Arg(IArgContainer.ARG_ROOM_ID, id),
                    Arg(
                        IArgContainer.ARG_CREATE_OR_EDIT_CHAT,
                        ChatGroupEditFragment.ChatInfoScreenMode.EDITING_CHAT
                    )
                )
            }
        }
    }

    private fun gotoShowUsersScreen(roomId: Long?, userType: String) {
        add(ChatGroupShowUsersFragment(), Act.LIGHT_STATUSBAR,
                Arg(IArgContainer.ARG_ROOM_ID, roomId),
                Arg(IArgContainer.ARG_ROOM_USER_TYPE, userType))
    }

    private fun showGroupAvatarImage(imageUrl: String, imageView: ImageView) {
        MediaViewer.with(context)
                .setSingleImage(
                    ImageViewerData(
                        imageUrl = imageUrl,
                        photoID = 0
                )
                )
                .startPosition(0)
                .transitionFromView(imageView)  // Transition with animation
                .onSaveImage { saveImage(it)  }   // saveMediaFile(it)
                .setAct(act)
                .hideDeleteMenuItem()
                .show()
    }

    private fun saveImage(imageUrl: String) {
        saveImageOrVideoFile(
            imageUrl = imageUrl,
            act = act,
            viewLifecycleOwner = viewLifecycleOwner,
            successListener = {}
        )
    }

}
