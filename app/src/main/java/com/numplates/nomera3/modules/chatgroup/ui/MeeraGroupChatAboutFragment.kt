package com.numplates.nomera3.modules.chatgroup.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.db.models.chatmembers.ChatMember
import com.meera.db.models.dialog.DialogEntity
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.groupusersrow.GroupUsersRowViewConfig
import com.meera.uikit.widgets.groupusersrow.UiKitGroupUsersRow
import com.meera.uikit.widgets.groupusersrow.UsersRowIconSize
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.USER_TYPE_ADMIN
import com.numplates.nomera3.data.newmessenger.USER_TYPE_COMPANION
import com.numplates.nomera3.data.newmessenger.USER_TYPE_CREATOR
import com.numplates.nomera3.databinding.MeeraFragmentAboutGroupChatBinding
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegate
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegateImpl
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.fragments.main.KEY_BUNDLE_TRANSIT_FROM_MAIN
import com.numplates.nomera3.modules.redesign.fragments.main.KEY_CHAT_TRANSIT_FROM_RESULT
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.ui.mediaViewer.ImageViewerData
import com.numplates.nomera3.presentation.view.ui.mediaViewer.MediaViewer
import com.numplates.nomera3.presentation.view.utils.NTime
import com.numplates.nomera3.presentation.viewmodel.ChatGroupEditViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatGroupViewEvent


class MeeraGroupChatAboutFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_about_group_chat,
    behaviourConfigState = ScreenBehaviourState.Full
), SaveMediaFileDelegate by SaveMediaFileDelegateImpl() {
    private val binding by viewBinding(MeeraFragmentAboutGroupChatBinding::bind)
    private val viewModel by viewModels<ChatGroupEditViewModel> { App.component.getViewModelFactory() }
    private var roomId: Long? = null
    private var infoSnackbar: UiKitSnackBar? = null

    override val containerId: Int
        get() = R.id.fragment_first_container_view


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val roomId = arguments?.getLong(IArgContainer.ARG_ROOM_ID)
        setupToolbar()
        viewModel.showChatMembers(roomId)
        initObservables(roomId)
    }

    override fun onStart() {
        super.onStart()
        val roomId = arguments?.getLong(IArgContainer.ARG_ROOM_ID)
        viewModel.reloadDialogs(roomId)
    }

    override fun onStop() {
        super.onStop()
        infoSnackbar?.dismiss()
    }

    private fun setupToolbar() {
        binding.apply {
            aboutGroupChatNaw.backButtonClickListener = {
                findNavController().popBackStack()
            }
        }
    }

    private fun initObservables(roomId: Long?) {
        viewModel.getGroupData(roomId).observe(viewLifecycleOwner) { dialog ->
            if (dialog != null) {
                this.roomId = dialog.roomId
                showGroupChatInfo(dialog)
            }
        }
        viewModel.liveGroupEditEvents.observe(viewLifecycleOwner) { event ->
            handleEvents(event)
        }
        viewModel.getChatMembers(roomId).observe(viewLifecycleOwner) { members ->
            renderGroupChatMembers(members)
            renderGroupChatAdmins(members)
            handleEditGroupChatDataButton(members)
        }
    }

    private fun renderGroupChatMembers(members: List<ChatMember>) {
        setupUsersRow(
            view = binding.rowAboutGroupChatMembers,
            members = members
        )
    }

    private fun renderGroupChatAdmins(members: List<ChatMember>) {
        val admins = mutableListOf<ChatMember>()
        members.forEach { member ->
            if (member.type == USER_TYPE_ADMIN || member.type == USER_TYPE_CREATOR) {
                admins.add(member)
            }
        }

        setupUsersRow(
            view = binding.rowAboutGroupChatAdmins,
            members = admins
        )
        handleAddMemberBtnVisibility(admins)
    }

    private fun handleAddMemberBtnVisibility(members: List<ChatMember>?) {
        if (members.isNullOrEmpty()) return
        members.forEach {
            if (it.user.userId == viewModel.getUserUid()){
                binding.tvAddMembers.visible()
            }
        }
    }

    private fun handleEditGroupChatDataButton(members: List<ChatMember>) {
        roomId?.let { id ->
            for (member in members) {
                if (member.user.userId == viewModel.getUserUid()
                    && (member.type == USER_TYPE_ADMIN || member.type == USER_TYPE_CREATOR)) {
                    binding.tvChangeGroupChat.visible()
                    break
                }
            }

            binding.tvChangeGroupChat.setThrottledClickListener {
                MeeraCreateGroupChatFragment.ChatInfoScreenMode.EDITING_CHAT
                findNavController().safeNavigate(
                    resId = R.id.action_groupChatAboutFragment_to_createGroupChatFragment,
                    bundle = Bundle().apply {
                        putLong(IArgContainer.ARG_ROOM_ID, id)
                        putSerializable(IArgContainer.ARG_CREATE_OR_EDIT_CHAT,
                            MeeraCreateGroupChatFragment.ChatInfoScreenMode.EDITING_CHAT)
                    }
                )
            }
        }
    }

    private fun showGroupChatInfo(room: DialogEntity) {
        room.groupAvatar?.let { showGroupChatAvatar(avatarUrl = it) }
        binding.tvTitleAboutGroupChat.text = room.title
        binding.tvDateAboutGroupChat.text = NTime.timeAgo(room.createdAt / 1000)
        binding.tvDescriptionAboutGroupChat.text = room.description
        handleAddMembersClick(room)
        handleDeleteGroupChatButton(room)
        handleListMembersClick(room)
    }

    private fun handleAddMembersClick(dialog: DialogEntity) {
        binding.tvAddMembers.setThrottledClickListener {
            findNavController().safeNavigate(
                resId = R.id.action_groupChatAboutFragment_to_groupChatListMembersFragment,
                bundle = Bundle().apply {
                    putLong(IArgContainer.ARG_ROOM_ID, dialog.roomId)
                }
            )
        }
    }

    private fun handleDeleteGroupChatButton(room: DialogEntity) {
        if (room.creator.userId == viewModel.getUserUid()) {
            binding.tvDeleteGroupchat.visible()
            binding.tvDeleteGroupchat.setThrottledClickListener {
                showDeleteGroupDialog(room.roomId)
            }
        }
    }

    private fun handleListMembersClick(room: DialogEntity) {
        binding.cellAboutGroupChatMembers.setThrottledClickListener {
            gotoShowUsersScreen(room.roomId, USER_TYPE_COMPANION)
        }
        binding.cellAboutGroupChatAdmins.setThrottledClickListener {
            gotoShowUsersScreen(room.roomId, USER_TYPE_ADMIN)
        }
    }

    private fun showGroupChatAvatar(avatarUrl: String) {
        binding.apply {
            Glide.with(requireContext())
                .load(avatarUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.group_chat_avatar_circle)
                .into(ivAvatarAboutGroupChat)

            previewGroupChatAvatar(ivAvatarAboutGroupChat, avatarUrl)
        }
    }

    private fun previewGroupChatAvatar(imageView: ImageView, avatarUrl: String) {
        if (avatarUrl.isNotEmpty()) {
            imageView.setThrottledClickListener {
                showGroupAvatarImage(avatarUrl, imageView)
            }
        }
    }

    private fun setupUsersRow(view: UiKitGroupUsersRow?, members: List<ChatMember>) {
        val membersCount = members.size
        view?.setConfig(
            GroupUsersRowViewConfig(
                iconSize = UsersRowIconSize.SIZE_32,
                count = if (membersCount > MEMBERS_COUNT_FOR_SHOW_COUNTER) membersCount else null,
                iconUrls = members.map { it.user.avatarSmall.orEmpty() }
            )
        )
    }


    private fun handleEvents(event: ChatGroupViewEvent) {
        when (event) {
            is ChatGroupViewEvent.GroupChatDeleted -> chatDeletedAction()
            is ChatGroupViewEvent.ErrorChatDeleted -> showMessage(R.string.delete_chat_failure_toast, isError = true)
            else -> Unit
        }
    }

    private fun chatDeletedAction() {
        setFragmentResult(KEY_CHAT_TRANSIT_FROM_RESULT, bundleOf(KEY_BUNDLE_TRANSIT_FROM_MAIN to true))
        findNavController().popBackStack(R.id.mainChatFragment, false)
    }

    private fun showMessage(@StringRes messageRes: Int, isError: Boolean = true) {
        val iconState = if (isError) AvatarUiState.ErrorIconState else AvatarUiState.SuccessIconState
        infoSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(messageRes),
                    avatarUiState = iconState,
                )
            )
        )
        infoSnackbar?.show()
    }

    private fun showDeleteGroupDialog(roomId: Long?) {
        showDeleteChatConfirmDialog(roomId)
    }

    private fun showDeleteChatConfirmDialog(roomId: Long?) {
        MeeraConfirmDialogBuilder()
            .setHeader(R.string.meera_delete_group_chat_dialog_title)
            .setDescription(R.string.meera_delete_group_chat_dialog_description)
            .setTopBtnText(R.string.yes)
            .setBottomBtnText(R.string.no)
            .setCancelable(true)
            .setTopClickListener {
                viewModel.deleteGroupChat(roomId, true)
            }
            .show(childFragmentManager)
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
            .transitionFromView(imageView)
            .onSaveImage { saveImage(it)  }
            .setMeeraAct(requireActivity() as MeeraAct)
            .hideDeleteMenuItem()
            .show()
    }

    private fun saveImage(imageUrl: String) {
        saveImageOrVideoFile(
            imageUrl = imageUrl,
            act = requireActivity(),
            viewLifecycleOwner = viewLifecycleOwner,
            successListener = {}
        )
    }

    private fun gotoShowUsersScreen(roomId: Long?, userType: String) {
        roomId?.let {
            findNavController().safeNavigate(
                resId = R.id.action_groupChatAboutFragment_to_groupChatMembersInfoFragment,
                bundle = Bundle().apply {
                    putLong(IArgContainer.ARG_ROOM_ID, roomId)
                    putString(IArgContainer.ARG_ROOM_USER_TYPE, userType)
                }
            )
        }
    }

}
