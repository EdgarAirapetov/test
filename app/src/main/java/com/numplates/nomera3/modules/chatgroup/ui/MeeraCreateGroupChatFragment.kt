package com.numplates.nomera3.modules.chatgroup.ui

import android.Manifest
import android.hardware.camera2.CameraCharacteristics
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.enums.PermissionState
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.empty
import com.meera.core.extensions.observeOnce
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.meera.core.utils.tedbottompicker.TedBottomSheetPermissionActionsListener
import com.meera.core.utils.tedbottompicker.models.MediaViewerCameraTypeEnum
import com.meera.db.models.chatmembers.ChatMember
import com.meera.db.models.dialog.DialogEntity
import com.meera.media_controller_common.MediaControllerOpenPlace
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
import com.numplates.nomera3.databinding.MeeraFragmentCreateGroupChatBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.chat.MeeraChatFragment
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.utils.MeeraCoreTedBottomPickerActDependencyProvider
import com.numplates.nomera3.presentation.viewmodel.ChatGroupEditViewModel
import com.numplates.nomera3.presentation.viewmodel.NOT_YET_CREATED_ROOM_ID
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatGroupViewEvent
import timber.log.Timber

private const val DELAY_BEFORE_CLEAR_VIEWS_MILLIS = 1000L
const val MEMBERS_COUNT_FOR_SHOW_COUNTER = 3
private const val MAX_NAME_LENGTH = 45
private const val MAX_DESCRIPTION_LENGTH = 150

class MeeraCreateGroupChatFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_create_group_chat,
    behaviourConfigState = ScreenBehaviourState.Full
), BasePermission by BasePermissionDelegate(),
    BaseLoadImages by BaseLoadImagesDelegate(), TedBottomSheetPermissionActionsListener {

    private val binding by viewBinding(MeeraFragmentCreateGroupChatBinding::bind)
    private val viewModel by viewModels<ChatGroupEditViewModel> { App.component.getViewModelFactory() }
    private var groupAvatarPath: String? = null
    private var infoSnackbar: UiKitSnackBar? = null
    private var dialog: DialogEntity? = null
    private var currentTitle: String? = null
    private var currentDescription: String? = null
    private var mediaPicker: TedBottomSheetDialogFragment? = null

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val roomId = arguments?.getLong(IArgContainer.ARG_ROOM_ID)
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        setupToolbar()
        setupChatInfoDataFilters()
        initChatMembersClickListeners()
        setupCreateOrEditChatLabels()
        requestListMembers(roomId)
        resolveCreateOrEditGroupChat(roomId)
        initObservables(roomId)
        observeChangeDescriptionText()
        checkFields(binding.etGroupChatName.text?.trim().toString())
    }

    override fun onStop() {
        super.onStop()
        infoSnackbar?.dismiss()
        viewModel.clearCacheAdmins()
    }

    private fun setupToolbar() {
        binding.newGroupChatNaw.backButtonClickListener = { findNavController().popBackStack() }
    }

    private fun setupChatInfoDataFilters() {
        binding.etGroupChatName.filters = arrayOf(InputFilter.LengthFilter(MAX_NAME_LENGTH))
        binding.etGroupChatDescription.filters = arrayOf(InputFilter.LengthFilter(MAX_DESCRIPTION_LENGTH))
    }

    private fun initChatMembersClickListeners() {
        binding.apply {
            cellGroupChatMembers.setThrottledClickListener { gotoEditUsersScreen(dialog?.roomId, USER_TYPE_COMPANION) }
            cellGroupChatAdmins.setThrottledClickListener { gotoEditUsersScreen(dialog?.roomId, USER_TYPE_ADMIN) }
        }
    }


    private fun setupCreateOrEditChatLabels() {
        val createOrEdit = arguments?.getSerializable(IArgContainer.ARG_CREATE_OR_EDIT_CHAT) ?: return
        when (createOrEdit) {
            ChatInfoScreenMode.CREATING_CHAT -> {
                binding.apply {
                    newGroupChatNaw.title = getString(R.string.meera_group_chat_create_title)
                    tvCreateGroupChat.text = getString(R.string.group_chat_create_button)
                }
            }

            ChatInfoScreenMode.EDITING_CHAT -> {
                binding.apply {
                    newGroupChatNaw.title = getString(R.string.group_chat_edit_title)
                    tvCreateGroupChat.text = getString(R.string.group_chat_edit_button)
                }
            }
        }
    }

    private fun requestListMembers(roomId: Long?) {
        if (isRoomExists(roomId)) {
            viewModel.showChatMembers(roomId)
        } else {
            viewModel.showNotYetCreatedMembers()
        }
    }

    private fun resolveCreateOrEditGroupChat(roomId: Long?) {
        if (isRoomExists(roomId)) {
            editGroupChatData(roomId)
        } else {
            createGroupChat()
        }
    }

    private fun isRoomExists(roomId: Long?) = roomId != null && roomId > NOT_YET_CREATED_ROOM_ID

    private fun createGroupChat() {
        groupAvatarPath?.let { imagePath -> loadAvatarWithGlide(imagePath) }
        chooseAvatarGroupChat()
        binding.tvCreateGroupChat.setThrottledClickListener {
            val chatName = binding.etGroupChatName.text.trim().toString()
            val chatDescription = binding.etGroupChatDescription.text.trim().toString()
            if (chatName.isEmpty()) {
                showMessage(R.string.chat_name_cant_be_empty)
            } else {
                binding.tvCreateGroupChat.isClickable = false
                viewModel.createGroupChat(
                    chatName = chatName,
                    description = chatDescription,
                    avatarPath = groupAvatarPath,
                )
            }
        }
    }

    private fun chooseAvatarGroupChat() {
        binding.apply {
            ivGroupAvatar.setThrottledClickListener {
                checkPermissionsForMediaPicker()
            }
        }
    }

    private fun handleEvents(event: ChatGroupViewEvent) {
        when (event) {
            is ChatGroupViewEvent.GroupChatCreated -> handleSuccessResultGroupChatCreated(event.dialog)
            is ChatGroupViewEvent.GroupChatDeleted -> chatDeletedAction()
            is ChatGroupViewEvent.ErrorChatDeleted -> showMessage(R.string.delete_chat_failure_toast)
            is ChatGroupViewEvent.ErrorUserMessage -> showMessage(R.string.error_try_later)
            is ChatGroupViewEvent.CompleteAvatarChangedWhenEdit -> {
                deleteTemporaryImageFile(groupAvatarPath)
                saveTextGroupChatData(roomId = event.roomId, chatName = event.name, chatDescription = event.description)
            }

            is ChatGroupViewEvent.SuccessAvatarChangedWhenCreateChat -> {
                binding.tvCreateGroupChat.isClickable = true
                actionUploadAvatarWhenCreateChat(event.room, isShowError = false)
            }

            is ChatGroupViewEvent.ErrorAvatarChangedWhenCreateChat -> {
                binding.tvCreateGroupChat.isClickable = true
                actionUploadAvatarWhenCreateChat(event.room, isShowError = true)
            }

            else -> Unit
        }
    }

    private fun handleSuccessResultGroupChatCreated(room: DialogEntity?) {
        groupAvatarPath?.let { path ->
            if (path.isEmpty()) {
                binding.tvCreateGroupChat.isClickable = true
                gotoGroupChat(room, transitWithDelay = false)
            } else {
                viewModel.changeAvatarWhenCreateChat(
                    roomId = room?.roomId,
                    imagePath = path,
                    room = room
                )
            }
        } ?: let {
            binding.tvCreateGroupChat.isClickable = true
            gotoGroupChat(room)
        }
    }

    private fun checkPermissionsForMediaPicker() {
        checkMediaPermissions(object : PermissionDelegate.Listener {

            override fun onGranted() {
                openMediaPicker(PermissionState.GRANTED)
            }

            override fun onDenied() {
                openMediaPicker(PermissionState.NOT_GRANTED_CAN_BE_REQUESTED)
            }

            override fun needOpenSettings() {
                openMediaPicker(PermissionState.NOT_GRANTED_OPEN_SETTINGS)
            }
        })
    }

    private fun openMediaPicker(permissionState: PermissionState) {
        mediaPicker = loadSingleImageUri(
            activity = requireActivity(),
            viewLifecycleOwner = viewLifecycleOwner,
            type = MediaControllerOpenPlace.Avatar,
            cameraType = MediaViewerCameraTypeEnum.CAMERA_ORIENTATION_FRONT,
            suggestionsMenu = SuggestionsMenu(this, SuggestionsMenuType.ROAD),
            permissionState = permissionState,
            tedBottomSheetPermissionActionsListener = this,
            loadImagesCommonCallback = MeeraCoreTedBottomPickerActDependencyProvider(
                act = (requireActivity() as MeeraAct),
                onReadyImageUri = { imageUri ->
                    val imagePath = imageUri.path.orEmpty()
                    this@MeeraCreateGroupChatFragment.groupAvatarPath = imagePath
                    loadAvatarWithGlide(imagePath)
                }),
            cameraLensFacing = CameraCharacteristics.LENS_FACING_FRONT
        )

    }

    override fun onGalleryRequestPermissions() {
        setMediaPermissions()
    }

    override fun onGalleryOpenSettings() {
        requireContext().openSettingsScreen()
    }

    override fun onCameraRequestPermissions(fromMediaPicker: Boolean) {
        setPermissionsWithSettingsOpening(
            listener = object : PermissionDelegate.Listener {
                override fun onGranted() {
                    mediaPicker?.openCamera()
                }

                override fun needOpenSettings() {
                    onCameraOpenSettings()
                }

                override fun onError(error: Throwable?) {
                    onCameraOpenSettings()
                }
            }, Manifest.permission.CAMERA
        )
    }

    override fun onCameraOpenSettings() {
        MeeraConfirmDialogBuilder().setHeader(R.string.camera_settings_dialog_title)
            .setDescription(R.string.camera_settings_dialog_description)
            .setTopBtnText(R.string.camera_settings_dialog_action)
            .setBottomBtnText(R.string.camera_settings_dialog_cancel).setCancelable(false)
            .setTopClickListener { requireContext().openSettingsScreen() }.show(childFragmentManager)
    }

    private fun editGroupChatData(roomId: Long?) {
        binding.tvCreateGroupChat.setThrottledClickListener {
            val chatName = binding.etGroupChatName.text.trim().toString()
            val chatDescription = binding.etGroupChatDescription.text.trim().toString()

            groupAvatarPath?.let { path ->
                if (path.isNotEmpty()) {
                    viewModel.changeAvatarWhenEditChat(
                        roomId = roomId,
                        imagePath = groupAvatarPath.orEmpty(),
                        name = chatName,
                        description = chatDescription
                    )
                } else {
                    saveTextGroupChatData(roomId, chatName, chatDescription)
                }
            } ?: saveTextGroupChatData(roomId, chatName, chatDescription)
        }
    }

    private fun saveTextGroupChatData(roomId: Long?, chatName: String, chatDescription: String) {
        when {
            chatName != currentTitle && chatDescription == currentDescription ->
                viewModel.changeChatTitle(roomId, chatName)

            chatName == currentTitle && chatDescription != currentDescription ->
                viewModel.changeChatDescription(roomId, chatDescription)

            chatName != currentTitle && chatDescription != currentDescription -> {
                viewModel.changeChatTitle(roomId, chatName)
                viewModel.changeChatDescription(roomId, chatDescription, enableViewEvent = true)
            }

            else -> {
                findNavController().popBackStack()
            }
        }
    }

    private fun initObservables(roomId: Long?) {
        observeMembers()
        observeHandleEvents()
        observeTitleDescriptionData()
        observeExistsGroupChatData(roomId)
        observeChatNameTextChanges()
    }

    private fun observeChangeDescriptionText() {
        binding.apply {
            etGroupChatDescription.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                if (hasFocus)
                    etGroupChatDescription.hint = ""
                else
                    etGroupChatDescription.hint = resources.getString(R.string.chat_group_description)
            }
        }
    }

    private fun checkFields(text: String?) {
        binding.apply {
            tvCreateGroupChat.isClickable = !text.isNullOrEmpty()
            if (text.isNullOrEmpty().not()) {
                tvCreateGroupChat.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.uiKitColorForegroundLink
                    )
                )
            } else {
                tvCreateGroupChat.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.uiKitColorForegroundSecondary
                    )
                )
            }
        }
    }

    private fun observeMembers() {
        viewModel.liveSelectedChatMembers.observe(viewLifecycleOwner) { members ->
            setupUsersRow(
                view = binding.rowGroupChatMembers,
                members = members.members
            )
            setupUsersRow(
                view = binding.rowGroupChatAdmins,
                members = members.admins
            )
        }
    }

    private fun observeHandleEvents() {
        viewModel.liveGroupEditEvents.observe(viewLifecycleOwner) { viewEvent ->
            handleEvents(viewEvent)
        }
    }

    private fun observeTitleDescriptionData() {
        viewModel.liveViewEventsOnce.observeOnce(viewLifecycleOwner) { event ->
            when (event) {
                is ChatGroupViewEvent.SuccessTitleChanged, ChatGroupViewEvent.SuccessDescriptionChanged -> {
                    findNavController().popBackStack()
                }

                is ChatGroupViewEvent.OnSuccessReloadDialogs -> {
                    doDelayed(DELAY_BEFORE_CLEAR_VIEWS_MILLIS) {
                        transitToChatScreen(event.room)
                    }
                }

                else -> {
                    Timber.e("Unknown ChatGroup view event")
                }
            }
        }
    }

    private fun observeExistsGroupChatData(roomId: Long?) {
        roomId?.let { id ->
            if (id > 0) {
                viewModel.getGroupData(id).observe(viewLifecycleOwner, Observer { dialog ->
                    if (dialog != null) {
                        renderGroupInfoViews(dialog)
                    }
                })

                viewModel.getChatMembers(id).observe(viewLifecycleOwner, Observer { members ->
                    renderGroupChatMembers(members)
                    renderGroupChatAdmins(members)
                })
            }
        }
    }

    private fun observeChatNameTextChanges() {
        binding.etGroupChatName.addTextChangedListener { checkFields(it?.trim().toString()) }
    }

    private fun renderGroupInfoViews(dialog: DialogEntity) {
        this.dialog = dialog
        this.currentTitle = dialog.title
        this.currentDescription = dialog.description
        loadAvatarWithGlide(imagePath = dialog.groupAvatar.orEmpty())

        binding.ivGroupAvatar.setThrottledClickListener {
            checkPermissionsForMediaPicker()
        }

        binding.etGroupChatName.setText(dialog.title)
        binding.etGroupChatDescription.setText(dialog.description)
    }

    private fun renderGroupChatMembers(members: List<ChatMember>) {
        setupUsersRow(
            view = binding.rowGroupChatMembers,
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
            view = binding.rowGroupChatAdmins,
            members = admins
        )
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

    private fun gotoGroupChat(dialog: DialogEntity?, transitWithDelay: Boolean = true) {
        val delayTransitToChatScreen = if (transitWithDelay) DELAY_BEFORE_CLEAR_VIEWS_MILLIS else 0L
        doDelayed(delayTransitToChatScreen) {
            clearGroupInfo()
            transitToChatScreen(dialog)
        }
    }

    private fun actionUploadAvatarWhenCreateChat(dialog: DialogEntity?, isShowError: Boolean) {
        deleteTemporaryImageFile(groupAvatarPath)
        if (isShowError) showMessage(R.string.profile_update_avatar_fail)
        gotoGroupChat(dialog)
    }

    private fun deleteTemporaryImageFile(imagePath: String?) {
        viewModel.deleteTempImageFile(imagePath)
    }

    private fun clearGroupInfo() {
        groupAvatarPath = null
        binding.etGroupChatName.setText(String.empty())
        binding.etGroupChatDescription.setText(String.empty())
    }

    private fun loadAvatarWithGlide(imagePath: String) {
        binding.ivGroupAvatar.apply {
            Glide.with(this@MeeraCreateGroupChatFragment)
                .load(imagePath)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.meera_group_chat_avatar_placeholder)
                .into(this)
        }
    }

    private fun chatDeletedAction() {
        showMessage(R.string.delete_chat_success_toast)
    }

    private fun showMessage(@StringRes messageRes: Int) {
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

    private fun transitToChatScreen(dialog: DialogEntity?) {
        findNavController().safeNavigate(
            resId = R.id.action_createGroupChatFragment_to_chatMessagesFragment,
            bundle = Bundle().apply {
                putSerializable(IArgContainer.ARG_WHERE_CHAT_OPEN, AmplitudePropertyWhere.NEW_GROUP_CHAT)
                putParcelable(
                    IArgContainer.ARG_CHAT_INIT_DATA, ChatInitData(
                        initType = ChatInitType.FROM_LIST_ROOMS,
                        roomId = dialog?.roomId
                    )
                )
                putSerializable(IArgContainer.ARG_CHAT_TRANSIT_FROM, MeeraChatFragment.TransitFrom.GROUP_CHAT)
            }
        )
    }

    private fun gotoEditUsersScreen(roomId: Long?, userType: String) {
        findNavController().safeNavigate(
            resId = R.id.action_createGroupChatFragment_to_groupChatMembersInfoFragment,
            bundle = Bundle().apply {
                putLong(IArgContainer.ARG_ROOM_ID, roomId ?: 0)
                putString(IArgContainer.ARG_ROOM_USER_TYPE, userType)
            }
        )
    }

    enum class ChatInfoScreenMode {
        CREATING_CHAT,
        EDITING_CHAT
    }

}
