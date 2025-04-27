package com.numplates.nomera3.modules.complains.ui.details

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.widget.RxTextView
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.enums.PermissionState
import com.meera.core.dialogs.ConfirmDialogBuilder
import com.meera.core.extensions.animateHeight
import com.meera.core.extensions.click
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setTint
import com.meera.core.extensions.visible
import com.meera.core.permission.PERMISSION_MEDIA_CODE
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_IMAGE
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_IMAGE_GIF
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_VIDEO
import com.meera.core.utils.imagecapture.ui.ImageCaptureUtils
import com.meera.core.utils.imagecapture.ui.getImageFromCamera
import com.meera.core.utils.imagecapture.ui.model.ImageCaptureResultModel
import com.meera.core.utils.listeners.OrientationScreenListener
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.meera.core.utils.tedbottompicker.TedBottomSheetPermissionActionsListener
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_api.model.MediaControllerNeedEditResponse
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.noomeera.nmrmediatools.NMRVideoAmplitude
import com.numplates.nomera3.App
import com.numplates.nomera3.MEDIA_GALLERY_ITEM_COUNT_IN_ROW
import com.numplates.nomera3.MEDIA_GALLERY_ITEM_PADDING
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentComplainDetailsBinding
import com.numplates.nomera3.di.CACHE_DIR
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetAppInfoAsyncUseCase
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegate
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegateImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.complains.ui.ComplainsNavigator
import com.numplates.nomera3.modules.complains.ui.ComplaintFlowInteraction
import com.numplates.nomera3.modules.complains.ui.ComplaintFlowResult
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_MOMENT_ID
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_ROOM_ID
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_SEND_RESULT
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_USER_ID
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_WHERE_VALUE
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAIN_TYPE
import com.numplates.nomera3.modules.complains.ui.KEY_EXTRA_USER_COMPLAIN
import com.numplates.nomera3.modules.complains.ui.change.ChangeReasonBottomSheet
import com.numplates.nomera3.modules.complains.ui.confirm.ConfirmComplainDialog
import com.numplates.nomera3.modules.complains.ui.details.UserComplaintDetailsEvent.FinishComplaintFlow
import com.numplates.nomera3.modules.complains.ui.model.UserComplainUiModel
import com.numplates.nomera3.modules.complains.ui.reason.ComplainType
import com.numplates.nomera3.modules.feed.ui.getScreenWidth
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.modules.uploadpost.ui.adapter.AttachmentPostAdapter
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentPostModel
import com.numplates.nomera3.modules.user.ui.entity.ComplainReasonId
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SHOW_MEDIA_GALLERY
import com.numplates.nomera3.presentation.utils.isEditorTempFile
import com.numplates.nomera3.presentation.view.adapter.MediaListAdapter
import com.numplates.nomera3.presentation.view.adapter.MediaListAdapter.MediaItemGallery
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.ui.mediaViewer.ImageViewerData
import com.numplates.nomera3.presentation.view.ui.mediaViewer.MediaViewer
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.pager.RecyclingPagerAdapter
import com.numplates.nomera3.presentation.view.utils.CoreTedBottomPickerActDependencyProvider
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.utils.camera.CameraOrientation
import com.numplates.nomera3.presentation.view.utils.camera.CameraProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Named

private const val DEFAULT_ANIMATION_DURATION = 100L
private const val DELAY_FOR_SHOW_MEDIA_LIST = 500L

class UserComplainDetailsFragment :
    BaseFragmentNew<FragmentComplainDetailsBinding>(),
    IOnBackPressed,
    BasePermission by BasePermissionDelegate(),
    BaseLoadImages by BaseLoadImagesDelegate(),
    SaveMediaFileDelegate by SaveMediaFileDelegateImpl(),
    TedBottomSheetPermissionActionsListener {

    @Inject
    lateinit var cameraProviderBuilder: CameraProvider.Builder

    @Inject
    lateinit var getAppInfoUseCase: GetAppInfoAsyncUseCase

    @Inject
    @Named(CACHE_DIR)
    lateinit var cacheDir: File

    @Inject
    lateinit var fileManager: FileManager

    private val complaintViewModel by viewModels<UserComplaintDetailsViewModel> { App.component.getViewModelFactory() }
    private val complainsNavigator by lazy(LazyThreadSafetyMode.NONE) { ComplainsNavigator(requireActivity()) }
    private var complainType: Int? = null
    private var pathImage: String? = null
    private var pathVideo: String? = null
    private var mediaAttachmentUri: Uri? = null
    private var isVideoWasCompressed: Boolean = false
    private var isLockBackPressed = false
    private var complainUiModel: UserComplainUiModel? = null
    private var complaintWhere: AmplitudePropertyWhere? = null
    private var complainSendResult: Boolean = true
    private var optionalMomentId: Long? = null
        set(value) {
            if (value != 0L) field = value
        }
    private var optionalRoomId: Long? = null
        set(value) {
            if (value != 0L) field = value
        }
    private val disposables = CompositeDisposable()
    private var isPreviewPermitted = false
    private var mediaPickerJob: Job? = null
    private lateinit var mediaAdapter: MediaListAdapter
    private val attachmentsAdapter by lazy(LazyThreadSafetyMode.NONE) { AttachmentPostAdapter(complaintViewModel) }
    private val orientationScreenListener = object : OrientationScreenListener() {
        override fun onOrientationChanged(orientation: Int) {
            orientationChangedListener.invoke(orientation)
        }
    }

    private var isOpenedRv = false
    private var showMediaGallery: Boolean? = null
    private var userId: Long? = null
    private var complaintFlowResult = ComplaintFlowResult.CANCELLED
    private var complaintResultHanding = AtomicBoolean(false)

    private var mediaPicker: TedBottomSheetDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentComplainDetailsBinding
        get() = FragmentComplainDetailsBinding::inflate

    override fun onBackPressed(): Boolean {
        sendComplaintFlowResult()
        mediaPickerJob?.cancel()
        return binding?.etWrite?.suggestionMenu?.onBackPressed() ?: isLockBackPressed
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        complainType = arguments?.getInt(KEY_COMPLAIN_TYPE, ComplainType.USER.key)
        userId = arguments?.getLong(KEY_COMPLAINT_USER_ID)
        complainUiModel = arguments?.getSerializable(KEY_EXTRA_USER_COMPLAIN) as? UserComplainUiModel
        complaintWhere = arguments?.getSerializable(KEY_COMPLAINT_WHERE_VALUE) as? AmplitudePropertyWhere
        complainSendResult = arguments?.getBoolean(KEY_COMPLAINT_SEND_RESULT) ?: true
        showMediaGallery = arguments?.getBoolean(ARG_SHOW_MEDIA_GALLERY, false)
        optionalMomentId = arguments?.getLong(KEY_COMPLAINT_MOMENT_ID)
        optionalRoomId = arguments?.getLong(KEY_COMPLAINT_ROOM_ID)
        mediaPickerJob = doDelayed(300) {
            val cameraProvider = cameraProviderBuilder
                .cameraOrientation(CameraOrientation.BACK)
                .build()
            mediaAdapter = MediaListAdapter(
                cameraProvider = cameraProvider,
                lifecycleOwner = viewLifecycleOwner,
                fileManager = fileManager,
                mediaEditorViewController = act.getMediaControllerFeature()
            )
            setupClickActions()
            setupRvGallery()
            if (showMediaGallery == false) openKeyboard()
            binding?.addPostContentContainer?.setOnClickListener {
                openKeyboard()
            }
        }
        initAttachmentAdapter()
        initObservers()
        setupStatusBar()
        setupInterfaceByItem(complainUiModel)
        setupEditTextField()

        act.permissionListener.add(listener)
    }

    private val listener: (requestCode: Int, permissions: Array<String>, grantResults: IntArray) -> Unit =
        { requestCode, permissions, grantResults ->
            if (requestCode == PERMISSION_MEDIA_CODE) {
                if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                    mediaPicker?.updateGalleryPermissionState(PermissionState.GRANTED)
                    isPreviewPermitted = true
                    initRvMedia()
                    setupMediaGalleryHeight()

                    complaintViewModel.liveGalleryMedia
                        .observe(this@UserComplainDetailsFragment.viewLifecycleOwner) {
                            handleMediaUri(it)
                        }

                    complaintViewModel.requestLatestMedia()
                } else {
                    mediaPicker?.updateGalleryPermissionState(PermissionState.NOT_GRANTED_OPEN_SETTINGS)
                    binding?.rvGalleryMedia?.gone()
                    isPreviewPermitted = false
                }
            }
        }


    override fun onDestroyView() {
        act.permissionListener.remove(listener)

        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        subscribeViewEvent()
    }

    override fun onPause() {
        super.onPause()
        unSubscribeViewEvent()
    }

    override fun onStart() {
        super.onStart()
        setupTextChangedObservable()
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    // Configuration need to configure mediaviewerView
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        orientationScreenListener.onOrientationChanged(newConfig.orientation)
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
                    if (mediaPicker != null) {
                        mediaPicker?.openCamera()
                    } else {
                        updateMediaAdapter()
                        activity?.getImageFromCamera(object : ImageCaptureUtils.Listener {
                            override fun onResult(result: ImageCaptureResultModel) {
                                mediaAttachmentSelected(result.fileUri)
                            }
                        })
                    }
                }

                override fun needOpenSettings() {
                    onCameraOpenSettings()
                }

                override fun onError(error: Throwable?) {
                    onCameraOpenSettings()
                }
            },
            Manifest.permission.CAMERA
        )
    }

    override fun onCameraOpenSettings() {
        showCameraSettingsDialog()
    }

    private fun showCameraSettingsDialog() {
        ConfirmDialogBuilder()
            .setHeader(getString(R.string.camera_settings_dialog_title))
            .setDescription(getString(R.string.camera_settings_dialog_description))
            .setLeftBtnText(getString(R.string.camera_settings_dialog_cancel))
            .setRightBtnText(getString(R.string.camera_settings_dialog_action))
            .setCancelable(false)
            .setRightClickListener {
                requireContext().openSettingsScreen()
            }
            .show(childFragmentManager)
    }

    private fun updateMediaAdapter() {
        mediaAdapter.notifyDataSetChanged()
    }

    private fun setupStatusBar() {
        binding?.toolbarContentContainer?.setMargins(top = context.getStatusBarHeight())
    }

    private fun setupInterfaceByItem(complain: UserComplainUiModel?) {
        binding?.tvComplainReason?.text = complain?.titleRes?.let { getString(it) } ?: String.empty()
    }

    private fun setupEditTextField() {
        if (complainType == ComplainType.CHAT.key) {
            binding?.etWrite?.hint = getString(R.string.chat_group_complaint_text_reason_hint)
        }
    }

    private fun subscribeViewEvent() {
        complainsNavigator.registerChangeReasonListener(this) { complain ->
            complainUiModel = complain
            setupInterfaceByItem(complain)
            checkUpload()
        }
    }

    private fun unSubscribeViewEvent() {
        complainsNavigator.unregisterChangeReasonListener()
    }

    private fun initObservers() {
        complaintViewModel.complaintEvents
            .onEach { event ->
                when (event) {
                    is UserComplaintDetailsEvent.OnEditImage -> openEditor(
                        uri = Uri.parse(event.path)
                    )

                    is UserComplaintDetailsEvent.OnOpenImage -> openPhoto(event.path)
                    is UserComplaintDetailsEvent.OnVideoPlay -> openVideo(event.path)
                    is UserComplaintDetailsEvent.OnEditVideo -> openEditor(
                        uri = Uri.parse(event.path)
                    )

                    is FinishComplaintFlow -> finishComplaintFlow(isSuccess = event.isSuccess)
                }
            }
            .launchIn(lifecycleScope)

        complaintViewModel.screenState.observe(viewLifecycleOwner) { state ->
            binding?.ivSend?.isEnabled = state.isLockedSendButton
        }
    }

    private fun initAttachmentAdapter() {
        complaintViewModel.attachmentsMedia.observe(viewLifecycleOwner) { attachments ->
            submitItems(attachments)
            setRecyclerVisibility(attachments)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setRecyclerVisibility(attachments: List<UIAttachmentPostModel>) {
        if (attachments.isNotEmpty()) {
            binding?.rvAttachments?.visible()
        } else {
            attachmentsAdapter.notifyDataSetChanged()
            deleteAttachment()
            binding?.rvAttachments?.gone()
        }
    }

    private fun submitItems(attachments: List<UIAttachmentPostModel>) {
        if (binding?.rvAttachments?.adapter == null) {
            binding?.rvAttachments?.adapter = attachmentsAdapter
        }
        attachmentsAdapter.submitList(attachments)
    }

    private fun setupRvGallery() {
        checkMediaPermissions(
            object : PermissionDelegate.Listener {

                override fun onGranted() {
                    isPreviewPermitted = true
                    initRvMedia()
                    setupMediaGalleryHeight()

                    complaintViewModel.liveGalleryMedia
                        .observe(this@UserComplainDetailsFragment.viewLifecycleOwner) {
                            handleMediaUri(it)
                        }

                    complaintViewModel.requestLatestMedia()

                    handleAutoOpenMediaGallery(showMediaGallery)
                }

                override fun onDenied() {
                    binding?.rvGalleryMedia?.gone()
                    isPreviewPermitted = false
                }

                override fun onError(error: Throwable?) {
                    binding?.rvGalleryMedia?.gone()
                    isPreviewPermitted = false
                }
            }
        )
    }

    private fun setupMediaGalleryHeight() {
        binding?.rvGalleryMedia?.post {
            val itemsCountInRow = MEDIA_GALLERY_ITEM_COUNT_IN_ROW
            val itemsPadding = MEDIA_GALLERY_ITEM_PADDING.dp
            binding?.rvGalleryMedia?.layoutParams?.height = (getScreenWidth() / itemsCountInRow) - itemsPadding
        }
    }

    private fun handleAutoOpenMediaGallery(showMediaGallery: Boolean?) {
        if (showMediaGallery != null && showMediaGallery) {
            // loadSingleImageUri - открывается bottomsheet пикер, и возвращает
            // uri выбранной картинки / видео / др
            showMediaPicker { imageUri -> getMediaByPicker(imageUri) }
        }
    }

    private fun getMediaByPicker(
        path: Uri,
        elseAction: () -> Unit = { mediaAttachmentSelected(path) }
    ) {
        lifecycleScope.launch {
            val openPlace = complaintViewModel.openPlace
            when (val response = act.getMediaControllerFeature().needEditMedia(path, openPlace)) {
                is MediaControllerNeedEditResponse.VideoTooLong -> {
                    act.getMediaControllerFeature().showVideoTooLongDialog(
                        openPlace = openPlace,
                        needEditResponse = response,
                        showInMinutes = false
                    ) {
                        openEditor(
                            uri = path
                        )
                    }
                }

                is MediaControllerNeedEditResponse.NeedToCrop -> {
                    openEditor(
                        uri = path
                    )
                }

                else -> {
                    onVideoAddedWithoutEditor()
                    elseAction()
                }
            }
        }
    }

    private fun initRvMedia() {
        binding?.rvGalleryMedia?.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        binding?.rvGalleryMedia?.adapter = mediaAdapter
        if (pathImage == null && pathVideo == null) binding?.rvGalleryMedia?.visible()
        else binding?.rvGalleryMedia?.gone()
        isOpenedRv = true

        mediaAdapter.onClickListener = { item ->
            when (item.type) {
                MediaItemGallery.TYPE_MEDIA, MediaItemGallery.TYPE_VIDEO -> {
                    item.mediaPreview?.let { uri ->
                        mediaAttachmentSelected(uri)
                    }
                }

                MediaItemGallery.TYPE_CAMERA -> {
                    activity?.getImageFromCamera(object : ImageCaptureUtils.Listener {
                        override fun onResult(result: ImageCaptureResultModel) {
                            mediaAttachmentSelected(result.fileUri)
                        }
                    })
                }

                MediaItemGallery.TYPE_GALLERY -> {
                    showMediaPicker { imageUri -> mediaAttachmentSelected(imageUri) }
                }
            }
        }
    }

    private fun handleMediaUri(it: List<Uri>) {
        val collection = mutableListOf<MediaItemGallery>()
        collection.add(MediaItemGallery(type = MediaItemGallery.TYPE_CAMERA))
        it.forEach {
            val isVideo = fileManager.getMediaType(it) == MEDIA_TYPE_VIDEO
            val type = if (isVideo) MediaItemGallery.TYPE_VIDEO else MediaItemGallery.TYPE_MEDIA
            val mediaItemGallery = MediaItemGallery(it, type)
            collection.add(mediaItemGallery)
        }
        collection.add(MediaItemGallery(type = MediaItemGallery.TYPE_GALLERY))
        mediaAdapter.resetAndAddData(collection)
    }

    private fun openKeyboard() {
        Timber.d("openKeyboard")
        doDelayed(400) {
            binding?.etWrite?.requestFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(binding?.etWrite, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun setupClickActions() {
        binding?.ivBackButton?.setOnClickListener {
            binding?.etWrite?.suggestionMenu?.dismiss()
            context?.hideKeyboard(requireView())
            act.onBackPressed()
        }

        binding?.ivSend?.setOnClickListener {
            if (!isSendButtonLocked()) {
                context?.hideKeyboard(requireView())
                val dialog = ConfirmComplainDialog.showDialogInstance(
                    fragmentManager = childFragmentManager,
                    complain = mapComplainUIModel(complainUiModel),
                )
                dialog.setListener(object : ConfirmComplainDialog.Listener {
                    override fun onDismissed() = Unit
                    override fun onConfirmed() {
                        dialog.dismiss()
                        complaintViewModel.sendComplaint(
                            userId = userId ?: return,
                            reason = complainUiModel?.reasonId ?: return,
                            detailsParams = UserComplaintDetailsParams(
                                comment = binding?.etWrite?.text.toString(),
                                videoPath = pathVideo,
                                imagePath = pathImage,
                            ),
                            where = complaintWhere ?: error("This property should be specified."),
                            momentId = optionalMomentId,
                            roomId = optionalRoomId,
                            complainType = complainType ?: ComplainType.USER.key
                        )
                    }
                })
            } else {
                binding?.ivSend?.visible()
                binding?.pbSendComplain?.gone()
            }
        }

        // Image attach
        binding?.ivAttach?.click {
            showMediaPicker { imageUri -> getMediaByPicker(imageUri) }
        }

        val complainClickListener = {
            ChangeReasonBottomSheet.showInstance(
                fragmentManager = childFragmentManager,
                complain = complainUiModel,
                complainType = complainType
            )
        }
        binding?.ivArrowMenu?.setThrottledClickListener(clickListener = complainClickListener)
        binding?.tvComplainReason?.setThrottledClickListener(clickListener = complainClickListener)
    }

    private fun mapComplainUIModel(complain: UserComplainUiModel?): UserComplainUiModel? {
        return when (complainType) {
            ComplainType.USER.key -> complain
            ComplainType.CHAT.key -> complain?.copy(dialogHeaderTitle = R.string.chat_group_complaint_send_dialog_title)
            ComplainType.MOMENT.key -> complain?.copy(dialogHeaderTitle = R.string.user_complain_moment_question_title)
            else -> complain
        }
    }

    private fun deleteAttachment() {
        deletePostImage()
        deleteVideo()
        mediaAttachmentUri = null
        checkUpload()
    }

    private fun mediaAttachmentSelected(uri: Uri) {
        lifecycleScope.launch {
            val openPlace = complaintViewModel.openPlace
            when (val response = act.getMediaControllerFeature().needEditMedia(uri, openPlace)) {
                is MediaControllerNeedEditResponse.VideoTooLong -> {
                    act.getMediaControllerFeature().showVideoTooLongDialog(
                        openPlace = openPlace,
                        needEditResponse = response,
                        showInMinutes = false
                    ) {
                        openEditor(
                            uri = uri
                        )
                    }
                }

                MediaControllerNeedEditResponse.NeedToCrop -> {
                    openEditor(uri)
                }

                MediaControllerNeedEditResponse.NoNeedToEdit -> {
                    when (fileManager.getMediaType(uri)) {
                        MEDIA_TYPE_VIDEO -> {
                            showVideoAttachment(uri)
                        }

                        MEDIA_TYPE_IMAGE, MEDIA_TYPE_IMAGE_GIF -> {
                            setNotEditedImage(uri)
                        }
                    }
                }
            }
        }
    }


    @SuppressLint("BinaryOperationInTimber")
    private fun sendComplaintFlowResult() {
        Timber.d(
            "sendDialogChainResult. complaintResultHanding: " +
                "${complaintResultHanding.get()}, complaintFlowResult: $complaintFlowResult,"
        )
        if (complaintResultHanding.get()) return
        complaintResultHanding.set(true)
        val complaintFlowInteraction = act as? ComplaintFlowInteraction
        complaintFlowInteraction?.setIsFinishing(isFinishing = true)
        complaintFlowInteraction?.finishComplaintFlow(complaintFlowResult)
        when (complaintFlowResult) {
            ComplaintFlowResult.SUCCESS ->
                complainsNavigator.sendDialogChainResult(complaintReasonId = complainUiModel?.reasonId?.key ?: -1)

            ComplaintFlowResult.FAILURE,
            ComplaintFlowResult.CANCELLED ->
                complainsNavigator.sendDialogChainResult(dismissed = true)
        }
    }

    private fun finishComplaintFlow(isSuccess: Boolean) {
        val result =
            userId?.let { userId -> Result.success(userId) } ?: Result.failure(RuntimeException("User id is null"))
        complaintFlowResult = if (isSuccess) ComplaintFlowResult.SUCCESS else ComplaintFlowResult.FAILURE
        sendComplaintFlowResult()
        if (complainSendResult) {
            complainsNavigator.sendAdditionalActionResult(result)
        }
        act.onBackPressed()
    }

    private val editorCallback = object : MediaControllerCallback {
        override fun onPhotoReady(resultUri: Uri, nmrAmplitude: NMRPhotoAmplitude?) {
            showImageAttachment(resultUri)
            nmrAmplitude?.let(complaintViewModel::logPhotoEdits)
        }

        override fun onVideoReady(resultUri: Uri, nmrAmplitude: NMRVideoAmplitude?) {
            if (resultUri.toString().isEmpty()) {
                NToast.with(view)
                    .typeError()
                    .text(getString(R.string.error_while_working_with_image))
                    .show()
                return
            }
            getMediaByPicker(resultUri) {
                onVideoAddedWithEditor()
                showVideoAttachment(resultUri)
            }
            nmrAmplitude?.let(complaintViewModel::logVideoEdits)
        }

        override fun onCanceled() {
            openKeyboard()
        }

        override fun onError() {
            NToast.with(view)
                .typeError()
                .text(getString(R.string.error_while_working_with_image))
                .show()
        }
    }

    // Open Editor and handle result
    private fun openEditor(uri: Uri) {
        complaintViewModel.logEditorOpen(uri)
        act.getMediaControllerFeature().open(
            uri = uri,
            callback = editorCallback,
            openPlace = complaintViewModel.openPlace
        )
    }

    private fun onVideoAddedWithEditor() {
        isVideoWasCompressed = true
    }

    private fun onVideoAddedWithoutEditor() {
        isVideoWasCompressed = false
    }

    private fun setNotEditedImage(uri: Uri) {
        showImageAttachment(uri)
    }

    private fun showImageAttachment(uri: Uri) {
        uri.path?.let { pathPhoto ->
            closeMediaPreview()
            deletePostImage()
            pathVideo = null
            pathImage = pathPhoto
            complaintViewModel.onImageChosen(uri.toString())
            checkUpload()
            openKeyboard()
            mediaAttachmentUri = uri
        }
    }

    private fun showVideoAttachment(uri: Uri) {
        uri.path?.let { path ->
            closeMediaPreview()
            deleteVideo()
            pathImage = null
            pathVideo = path
            complaintViewModel.onVideoChosen(path)
            checkUpload()
            openKeyboard()
            mediaAttachmentUri = uri
        }
    }

    private fun setupTextChangedObservable() {
        binding?.etWrite?.let { inputText ->
            disposables.add(
                RxTextView.textChanges(inputText)
                    .debounce(150, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ text ->
                        if (text.toString().trim().isNotEmpty()) {
                            checkUpload()
                        } else {
                            pathImage?.let { path ->
                                if (path.isEmpty()) {
                                    checkUpload()
                                }
                            } ?: kotlin.run {
                                checkUpload()
                            }
                        }
                    }, { Timber.e(it) })
            )
        }
    }

    /**
     * Check if send button is unlocked for clicking | performing action. This function is
     * also used to select appropriate button style.
     */
    private fun isSendButtonLocked(): Boolean {
        val comment = binding?.etWrite?.text.toString()
        if (pathImage != null || pathVideo != null) return false
        return if (isMatchConditionsToGroupChat(comment)) {
            false
        } else {
            isMatchConditionsSelectedCommentOrImage(comment)
        }
    }

    private fun isMatchConditionsToGroupChat(commentText: String): Boolean {
        return complainType == ComplainType.CHAT.key
            && complainUiModel?.reasonId != ComplainReasonId.OTHER
            && commentText.isEmpty()
    }

    private fun isMatchConditionsSelectedCommentOrImage(comment: String): Boolean {
        return (pathImage == null && pathVideo == null) || comment.isEmpty()
    }

    private fun checkUpload() {
        if (isSendButtonLocked()) {
            binding?.ivSend?.isEnabled = false
            binding?.ivSend?.setTint(R.color.ui_gray)
        } else {
            binding?.ivSend?.isEnabled = true
            binding?.ivSend?.setTint(R.color.ui_purple)
        }
        if (pathImage == null && pathVideo == null) {
            doDelayed(DELAY_FOR_SHOW_MEDIA_LIST, ::openMediaPreview)
        } else {
            closeMediaPreview()
        }
    }


    private fun openMediaPreview() {
        if (isOpenedRv && isPreviewPermitted) return
        binding?.rvGalleryMedia?.visible()
        binding?.rvGalleryMedia?.measure(LayoutParams.MATCH_PARENT, 64.dp)
        binding?.rvGalleryMedia?.measuredHeight?.let {
            binding?.rvGalleryMedia?.animateHeight(
                it,
                DEFAULT_ANIMATION_DURATION
            )
        }
        isOpenedRv = true
    }

    private fun closeMediaPreview() {
        if (!isOpenedRv) return
        binding?.rvGalleryMedia?.visible()
        binding?.rvGalleryMedia?.animateHeight(
            0,
            DEFAULT_ANIMATION_DURATION
        ) {
            binding?.rvGalleryMedia?.gone()
        }
        isOpenedRv = false
    }

    private fun deletePostImage() {
        if (pathImage.isEditorTempFile(cacheDir)) {
            complaintViewModel.deleteImageExceptGif(pathImage)
        }
        pathImage = null
    }

    private fun deleteVideo() {
        if (pathVideo.isEditorTempFile(cacheDir)) {
            complaintViewModel.deleteVideo(pathVideo)
        }
        pathVideo = null
    }

    private fun openVideo(video: String) {
        val imageList = mutableListOf<ImageViewerData>()
        imageList.add(
            ImageViewerData(
                imageUrl = video,
                viewType = RecyclingPagerAdapter.VIEW_TYPE_VIDEO
            )
        )
        showMediaViewer(imageList)
    }

    private fun openPhoto(photo: String) {
        val imageList = mutableListOf<ImageViewerData>()
        imageList.add(
            ImageViewerData(
                imageUrl = photo,
                viewType = RecyclingPagerAdapter.VIEW_TYPE_IMAGE
            )
        )
        showMediaViewer(imageList)
    }

    private fun showMediaViewer(data: MutableList<ImageViewerData>) {
        if (data.isNotEmpty()) {
            MediaViewer.with(context)
                .setImageList(data)
                .startPosition(0)
                .setOrientationChangedListener(orientationScreenListener)
                .onSaveImage { saveImage(it) }
                .setAct(act)
                .setLifeCycle(lifecycle) // need when video shown
                .show()
        }
    }

    private fun showMediaPicker(onPickImageUri: (imageUri: Uri) -> Unit) {
        checkMediaPermissions(
            object : PermissionDelegate.Listener {

                override fun onGranted() {
                    showMediaPickerWithPermissionState(PermissionState.GRANTED, onPickImageUri)
                }

                override fun onDenied() {
                    showMediaPickerWithPermissionState(PermissionState.NOT_GRANTED_CAN_BE_REQUESTED, onPickImageUri)
                }

                override fun needOpenSettings() {
                    showMediaPickerWithPermissionState(PermissionState.NOT_GRANTED_OPEN_SETTINGS, onPickImageUri)
                }
            }
        )
    }

    private fun showMediaPickerWithPermissionState(
        permissionState: PermissionState,
        onPickImageUri: (imageUri: Uri) -> Unit
    ) {
        mediaPicker = loadSingleImageUri(
            activity = act,
            viewLifecycleOwner = viewLifecycleOwner,
            type = complaintViewModel.openPlace,
            needWithVideo = true,
            showGifs = true,
            suggestionsMenu = SuggestionsMenu(act.getCurrentFragment(), SuggestionsMenuType.ROAD),
            permissionState = permissionState,
            tedBottomSheetPermissionActionsListener = this,
            loadImagesCommonCallback = CoreTedBottomPickerActDependencyProvider(
                act = act,
                onReadyImageUri = { imageUri -> onPickImageUri(imageUri) },
                onDismissPicker = {
                    showMediaGallery = false
                    updateMediaAdapter()
                    openKeyboard()
                }
            ),
            cameraLensFacing = CameraCharacteristics.LENS_FACING_BACK,
        )
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
