package com.numplates.nomera3.presentation.view.fragments.newchat

import android.Manifest
import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.empty
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.observeOnce
import com.meera.core.extensions.pxToDp
import com.meera.core.extensions.returnReadExternalStoragePermissionAfter33
import com.meera.core.extensions.returnWriteExternalStoragePermissionAfter33
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.graphics.ExifUtils
import com.meera.db.models.chatmembers.ChatMember
import com.meera.db.models.dialog.DialogEntity
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.MEDIA_EXT_GIF
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.USER_TYPE_ADMIN
import com.numplates.nomera3.data.newmessenger.USER_TYPE_COMPANION
import com.numplates.nomera3.data.newmessenger.USER_TYPE_CREATOR
import com.numplates.nomera3.databinding.FragmentGroupChatEditBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.chat.ChatFragmentNew
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_CREATE_OR_EDIT_CHAT
import com.numplates.nomera3.presentation.view.adapter.newchat.FriendsProfileListAdapterNew
import com.numplates.nomera3.presentation.view.callback.ProfileVehicleListCallback
import com.numplates.nomera3.presentation.view.ui.OverlapDecoration
import com.numplates.nomera3.presentation.view.utils.NSupport
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.utils.NToast.Companion.with
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.TedBottomPicker
import com.numplates.nomera3.presentation.view.widgets.BannerLayoutManager
import com.numplates.nomera3.presentation.viewmodel.ChatGroupEditViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatGroupViewEvent
import timber.log.Timber
import java.io.File

class ChatGroupEditFragment :
    BaseFragmentNew<FragmentGroupChatEditBinding>(),
    BasePermission by BasePermissionDelegate()
{

    private val viewModelGroupEdit by viewModels<ChatGroupEditViewModel> { App.component.getViewModelFactory() }

    private var currentTitle:String? = null
    private var currentDescription: String? = null

    private var dialog: DialogEntity? = null

    private lateinit var friendsAdapter: FriendsProfileListAdapterNew
    private var widthContainerMembers = 0

    private lateinit var adminsAdapter: FriendsProfileListAdapterNew
    private var widthContainerAdmins = 0

    private var groupAvatarPath:String? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGroupChatEditBinding
        get() = FragmentGroupChatEditBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        setupCreateOrEditChatLabels()
        setupStatusBar(view)
        setupToolbar()

        // Get roomId if EDIT Group chat
        val roomId = arguments?.getLong(IArgContainer.ARG_ROOM_ID)

        // Show list user avatars
        roomId?.let { id ->
            if (id > 0) {
                viewModelGroupEdit.showChatMembers(id)
            } else {
                viewModelGroupEdit.showNotYetCreatedChatMembers()
            }
        }

        // Create NEW Group chat Or edit
        if (roomId != null && roomId == 0L) {
            createGroupChat()
        } else {
            editGroupChatData(roomId)
        }

        initChatMembersClickListeners()
        initChatMembersRecyclerList()
        initAdminsRecyclerList()

        initLiveObservers(roomId)

        binding?.etGroupChatDescription?.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus)
                binding?.etGroupChatDescription?.hint = ""
            else
                binding?.etGroupChatDescription?.hint = resources.getString(R.string.chat_group_description)
        }
        checkFields(binding?.etGroupChatName?.text?.toString())
    }

    private fun setupCreateOrEditChatLabels() {
        val createOrEdit = arguments?.getSerializable(ARG_CREATE_OR_EDIT_CHAT) ?: return
        when (createOrEdit) {
            ChatInfoScreenMode.CREATING_CHAT -> {
                binding?.apply {
                    tvHeaderGroupChatEditTop.text = getString(R.string.group_chat_create_title)
                    tvCreateGroupChat.text = getString(R.string.group_chat_create_button)
                }
            }
            ChatInfoScreenMode.EDITING_CHAT -> {
                binding?.apply {
                    tvHeaderGroupChatEditTop.text = getString(R.string.group_chat_edit_title)
                    tvCreateGroupChat.text = getString(R.string.group_chat_edit_button)
                }
            }
        }
    }

    private fun initLiveObservers(roomId: Long?) {
        viewModelGroupEdit.liveSelectedMembers.observe(viewLifecycleOwner, Observer { members ->
            binding?.tvGroupChatMembersCount?.text = members.size.toString()
            friendsAdapter.setData(members)
        })

        // Group chat NOT YET Created => Show admin (Only Im)
        viewModelGroupEdit.liveSelectedAdmin.observe(viewLifecycleOwner, Observer { members ->
            binding?.tvGroupChatAdminsCount?.text = members.size.toString()
            adminsAdapter.setData(members)
        })

        // Observe create group result
        viewModelGroupEdit.liveGroupEditEvents.observe(viewLifecycleOwner, Observer { viewEvent ->
            handleEvents(viewEvent)
        })

        // Observe save title and description events
        viewModelGroupEdit.liveViewEventsOnce.observeOnce(viewLifecycleOwner, Observer { event ->
            when(event) {
                is ChatGroupViewEvent.SuccessTitleChanged,
                    ChatGroupViewEvent.SuccessDescriptionChanged -> {
                    clearGroupInfo()
                    act.onBackPressed()
                }
                is ChatGroupViewEvent.OnSuccessReloadDialogs -> {
                    transitToChatScreen(event.room)
                    doDelayed(DELAY_BEFORE_CLEAR_VIEWS_MILLIS) {
                        clearGroupInfo()
                    }
                }
                else -> { Timber.e("Unknown ChatGroup view event") }
            }
        })

        // Observe group chat info (if exists)
        roomId?.let { id ->
            if (id > 0) {
                viewModelGroupEdit.getGroupData(id).observe(viewLifecycleOwner, Observer { dialog ->
                    if (dialog != null) {
                        renderGroupInfoViews(dialog)
                    }
                })

                viewModelGroupEdit.getChatMembers(id).observe(viewLifecycleOwner, Observer { members ->
                    // Limit pins
                    val maxPins = pxToDp(widthContainerMembers) / 36
                    if (members.size > maxPins) {
                        friendsAdapter.setData(members.subList(0, maxPins))
                    } else {
                        friendsAdapter.setData(members)
                    }

                    renderGroupChatAdmins(members)
                })
            }
        }

        binding?.etGroupChatName?.addTextChangedListener {
            checkFields(it?.toString())
        }
    }

    private fun checkFields(text: String?) {
        binding?.tvCreateGroupChat?.isClickable = !text.isNullOrEmpty()
        if (!text.isNullOrEmpty()) {
            binding?.tvCreateGroupChat?.setTextColor(ContextCompat.getColor(act, R.color.colorPrimary))
        } else {
            binding?.tvCreateGroupChat?.setTextColor(ContextCompat.getColor(act, R.color.ui_gray))
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

    private fun createGroupChat() {
        createNewGroupChatAvatar()
        binding?.tvCreateGroupChat?.setThrottledClickListener {
            val chatName = binding?.etGroupChatName?.text.toString()
            val chatDescription = binding?.etGroupChatDescription?.text.toString()
            if (chatName.isEmpty()) {
                showErrorUserMessage(getString(R.string.chat_name_cant_be_empty))
            } else {
                binding?.tvCreateGroupChat?.isClickable = false
                viewModelGroupEdit.createGroupChat(
                    chatName = chatName,
                    description = chatDescription,
                    avatarPath = groupAvatarPath,
                )
            }
        }
    }

    private fun createNewGroupChatAvatar() {
        binding?.ivGroupChatAvatar?.setOnClickListener {
            loadImageWithPicker { imagePath ->
                this.groupAvatarPath = imagePath
                binding?.ivGroupChatAvatar?.let { imageView ->
                    Glide.with(this@ChatGroupEditFragment)
                        .load(imagePath)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.group_chat_avatar_circle)
                        .into(imageView)
                }
            }
        }
    }

    /**
     * Edit group chat
     */
    private fun editGroupChatData(roomId: Long?) {
        // Push Ok button
        binding?.tvCreateGroupChat?.setOnClickListener {
            val chatName = binding?.etGroupChatName?.text.toString()
            val chatDescription = binding?.etGroupChatDescription?.text.toString()

            // Upload avatar
            groupAvatarPath?.let { path ->
                if (path.isNotEmpty()) {
                    viewModelGroupEdit.changeChatAvatar(roomId, groupAvatarPath ?: String.empty(),
                            {
                                deleteTemporaryImageFile(groupAvatarPath)
                                saveTextGroupChatData(roomId, chatName, chatDescription)
                            },
                            {
                                deleteTemporaryImageFile(groupAvatarPath)
                                saveTextGroupChatData(roomId, chatName, chatDescription)
                            })
                } else {
                    saveTextGroupChatData(roomId, chatName, chatDescription)
                }
            } ?: saveTextGroupChatData(roomId, chatName, chatDescription)
        }
    }

    private fun saveTextGroupChatData(roomId: Long?, chatName: String, chatDescription: String) {
        // Only chat name
        if (chatName != currentTitle && chatDescription == currentDescription) {
            viewModelGroupEdit.changeChatTitle(roomId, chatName)
        }
        // Only chat description
        if (chatName == currentTitle && chatDescription != currentDescription) {
            viewModelGroupEdit.changeChatDescription(roomId, chatDescription)
        }
        // Chat name and chat description at the same time
        if (chatName != currentTitle && chatDescription != currentDescription) {
            viewModelGroupEdit.changeChatTitle(roomId, chatName)
            viewModelGroupEdit.changeChatDescription(roomId, chatDescription, true)
        }
        // Nothing changes
        if (chatName == currentTitle && chatDescription == currentDescription) {
            clearGroupInfo()
            act.onBackPressed()
        }
    }

    private fun handleEvents(event: ChatGroupViewEvent) {
        when (event) {
            // If group created
            is ChatGroupViewEvent.GroupChatCreated -> {
                val dialog = event.dialog

                groupAvatarPath?.let { path ->
                    if (path.isEmpty()) {
                        binding?.tvCreateGroupChat?.isClickable = true
                        gotoGroupChat(event.dialog)
                    } else {
                        // Upload group chat avatar after create new group
                        binding?.progressUploadAvatar?.visible()
                        viewModelGroupEdit.changeChatAvatar(dialog?.roomId, path, {
                            binding?.tvCreateGroupChat?.isClickable = true
                            actionUploadAvatarWhenCreateChat(dialog, false)
                        }, {
                            binding?.tvCreateGroupChat?.isClickable = true
                            actionUploadAvatarWhenCreateChat(dialog, true)
                        })
                    }
                } ?: let {
                    binding?.tvCreateGroupChat?.isClickable = true
                    gotoGroupChat(event.dialog)
                }
            }

            is ChatGroupViewEvent.GroupChatDeleted -> chatDeletedAction()
            is ChatGroupViewEvent.ErrorChatDeleted ->
                NToast.with(view)
                        .text(getString(R.string.delete_chat_failure_toast))
                        .show()
            is ChatGroupViewEvent.ErrorUserMessage -> showErrorUserMessage(event.mesage)
            else -> {}
        }
    }

    private fun actionUploadAvatarWhenCreateChat(dialog: DialogEntity?, isShowError: Boolean) {
        binding?.progressUploadAvatar?.gone()
        deleteTemporaryImageFile(groupAvatarPath)
        if (isShowError) {
            NToast.with(view)
                    .text(getString(R.string.profile_update_avatar_fail))
                    .show()
        }
        gotoGroupChat(dialog)
    }

    private fun gotoGroupChat(dialog: DialogEntity?) {
        viewModelGroupEdit.reloadDialogs(dialog?.roomId)
    }

    private fun transitToChatScreen(dialog: DialogEntity?) {
        act.returnToFirstAndOpen(
            ChatFragmentNew(),
            Arg(IArgContainer.ARG_WHERE_CHAT_OPEN, AmplitudePropertyWhere.NEW_GROUP_CHAT),
            Arg(IArgContainer.ARG_CHAT_INIT_DATA, ChatInitData(
                initType = ChatInitType.FROM_LIST_ROOMS,
                roomId = dialog?.roomId
            ))
        )
    }

    private fun renderGroupInfoViews(dialog: DialogEntity) {
        this.dialog = dialog

        currentTitle = dialog.title
        currentDescription = dialog.description

        setGroupAvatar(dialog.groupAvatar ?: String.empty(), true)

        // Upload group avatarSmall
        binding?.ivGroupChatAvatar?.setOnClickListener {
            loadImageWithPicker { imagePath ->
                imagePath?.let {
                    this.groupAvatarPath = imagePath
                    setGroupAvatar(imagePath, false)
                }
            }

            /*loadSingleImage(MediaViewerEnum.COMMON) { imagePath ->
                this.groupAvatarPath = imagePath
                setGroupAvatar(imagePath, false)
            }*/
        }

        binding?.etGroupChatName?.setText(dialog.title)
        binding?.etGroupChatDescription?.setText(dialog.description)
        binding?.tvGroupChatMembersCount?.text = dialog.membersCount.toString()
        binding?.tvGroupChatAdminsCount?.text = dialog.adminsCount.toString()

        // Delete group chat (ONLY Creator can delete group chat)
        if (dialog.creator.userId == viewModelGroupEdit.getUserUid()) {
            binding?.tvDeleteGroupChat?.visible()
            binding?.tvDeleteGroupChat?.setOnClickListener {
                showDeleteGroupDialog(dialog.roomId)
            }
        }

        // Goto users screen
        binding?.tvGroupChatMembers?.setOnClickListener {
            gotoEditUsersScreen(dialog.roomId, USER_TYPE_COMPANION)
        }

        // Goto admins screen
        binding?.tvGroupChatAdmins?.setOnClickListener {
            gotoEditUsersScreen(dialog.roomId, USER_TYPE_ADMIN)
        }
    }

    private fun setGroupAvatar(avatarUrl: String, showOverlay: Boolean) {
        binding?.ivGroupChatAvatar?.let { imageView ->
            Glide.with(this@ChatGroupEditFragment)
                    .load(avatarUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.group_chat_avatar_circle)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?,
                                                  model: Any?,
                                                  target: Target<Drawable>?,
                                                  isFirstResource: Boolean): Boolean {
                            Timber.e("Group avatar load failed:${e?.message}")
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?,
                                                     model: Any?,
                                                     target: Target<Drawable>?,
                                                     dataSource: DataSource?,
                                                     isFirstResource: Boolean): Boolean {
                            if (showOverlay) {
                                binding?.ivGroupChatAvatarOverlay?.visible()
                            } else {
                                binding?.ivGroupChatAvatarOverlay?.gone()
                            }
                            return false
                        }
                    })
                    .into(imageView)
        }
    }

    private fun deleteTemporaryImageFile(imagePath: String?) {
        viewModelGroupEdit.deleteTempImageFile(imagePath)
    }

    private fun gotoEditUsersScreen(roomId: Long?, userType: String) {
        add(ChatGroupShowUsersFragment(), Act.LIGHT_STATUSBAR,
                Arg(IArgContainer.ARG_ROOM_ID, roomId),
                Arg(IArgContainer.ARG_ROOM_USER_TYPE, userType))
    }

    private fun initChatMembersClickListeners() {
        binding?.tvGroupChatMembers?.setThrottledClickListener {
            gotoEditUsersScreen(dialog?.roomId, USER_TYPE_COMPANION)
        }
        binding?.tvGroupChatAdmins?.setThrottledClickListener {
            gotoEditUsersScreen(dialog?.roomId, USER_TYPE_ADMIN)
        }
    }

    private fun initChatMembersRecyclerList() {
        val bannerLayoutManagerMembers =
                BannerLayoutManager(context, RecyclerView.HORIZONTAL, false)
        friendsAdapter = FriendsProfileListAdapterNew.Builder(act)
                .callback(object : ProfileVehicleListCallback() {
                    override fun getZeroDataImageId() = 0

                    override fun getZeroDataText() = String.empty()

                    override fun onClick(holder: RecyclerView.ViewHolder?) {
                        gotoEditUsersScreen(dialog?.roomId, USER_TYPE_COMPANION)
                    }
                })
                .data(mutableListOf())
                .build()

        binding?.rvGroupChatMembers?.apply {
            setHasFixedSize(true)
            layoutManager = bannerLayoutManagerMembers
            isNestedScrollingEnabled = false
            adapter = friendsAdapter
            addItemDecoration(OverlapDecoration(dpToPx(18)))
        }

        binding?.containerPinMembers?.post {
            widthContainerMembers = binding?.containerPinMembers?.width ?: 0
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
                        gotoEditUsersScreen(dialog?.roomId, USER_TYPE_ADMIN)
                    }
                })
                .data(mutableListOf())
                .build()

        binding?.rvGroupChatAdmins?.apply {
            setHasFixedSize(true)
            layoutManager = bannerLayoutManagerAdmins
            isNestedScrollingEnabled = false
            adapter = adminsAdapter
            addItemDecoration(OverlapDecoration(dpToPx(18)))
        }

        binding?.containerPinAdmins?.post {
            widthContainerAdmins = binding?.containerPinAdmins?.width ?: 0
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
        } else {
            adminsAdapter.setData(admins)
        }
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

    private fun chatDeletedAction() {
        NToast.with(view)
                .text(getString(R.string.delete_chat_success_toast))
                .typeSuccess()
                .show()
        act.returnToTargetFragment(0, true)
    }

    private fun showErrorUserMessage(message: String?) {
        message?.let {
            NToast.with(view)
                    .text(message)
                    .show()
        }
    }

    private fun clearGroupInfo() {
        binding?.etGroupChatName?.setText(String.empty())
        binding?.etGroupChatDescription?.setText(String.empty())
    }

    enum class ChatInfoScreenMode {
        CREATING_CHAT,
        EDITING_CHAT;
    }

    private fun loadImageWithPicker(onImageReady: (imagePath: String?) -> Unit) {
        setPermissions(
            object : PermissionDelegate.Listener {
                override fun onGranted() {
                    TedBottomPicker
                        .with(act)
                        .showGalleryTile(false)
                        .showTitle(false)
                        .setWithPreview(MediaControllerOpenPlace.Common)
                        .setPreviewMaxCount(Int.MAX_VALUE)
                        .setCameraLensFacing(CameraCharacteristics.LENS_FACING_BACK)
                        .show { uriNew: Uri? ->
                            handleUriResult(uriNew, onImageReady)
                        }
                }

                override fun onDenied() {
                    showCommonError(R.string.you_must_grant_permissions)
                }

                override fun onError(error: Throwable?) {
                    showCommonError(R.string.you_must_grant_permissions)
                }
            },
            Manifest.permission.CAMERA,
            returnReadExternalStoragePermissionAfter33(),
            returnWriteExternalStoragePermissionAfter33(),
        )
    }

    private fun handleUriResult(
        uriNew: Uri?,
        onImageReady: (imagePath: String?) -> Unit
    ) {
        Handler(Looper.getMainLooper()).post {
            val path = uriNew?.path ?: String.empty()
            val extension = path.substring(path.lastIndexOf("."))

            if (extension != MEDIA_EXT_GIF) {
                var photoFile: File?
                var bitmap: Bitmap?
                try {
                    photoFile = if (viewModelGroupEdit.isGooglePhoto(uriNew)) {
                        File(viewModelGroupEdit.saveImageFromGoogleDrives(uriNew))
                    } else {
                        File(NSupport.getPath(act, uriNew))
                    }
                    val options = BitmapFactory.Options()
                    bitmap = BitmapFactory.decodeFile(photoFile.absolutePath, options)
                } catch (e: Exception) {
                    e.printStackTrace()
                    bitmap = null
                    photoFile = null
                }
                if (bitmap != null) {
                    val matrix = Matrix()
                    matrix.postRotate(ExifUtils.setNormalOrientation(photoFile)) // 90
                    bitmap = Bitmap.createBitmap(
                        bitmap,
                        0,
                        0,
                        bitmap.width,
                        bitmap.height,
                        matrix,
                        true
                    )
                    val temp = viewModelGroupEdit.createImageFile()
                    val filePath =
                        viewModelGroupEdit.saveBitmapInFile(bitmap, temp.absolutePath)
                    if (view != null) {
                        view?.post {
                            try {
                                onImageReady(filePath)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } else {
                    with(act)
                        .text(getString(R.string.error_upload_image))
                        .show()
                }
            } else {
                if (view != null) {
                    view?.post {
                        try {
                            onImageReady(uriNew?.path)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }



    companion object {
        private const val DELAY_BEFORE_CLEAR_VIEWS_MILLIS = 1000L
    }
}
