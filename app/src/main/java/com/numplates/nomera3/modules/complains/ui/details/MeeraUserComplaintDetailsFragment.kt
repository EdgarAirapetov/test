package com.numplates.nomera3.modules.complains.ui.details

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding2.widget.RxTextView
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.enums.PermissionState
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.common.MEDIA_PICKER_HALF_EXTENDED_RATIO
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.getScreenHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.extensions.setBackgroundTint
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setTint
import com.meera.core.extensions.sp
import com.meera.core.extensions.visible
import com.meera.core.permission.PERMISSION_MEDIA_CODE
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_IMAGE
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_IMAGE_GIF
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_VIDEO
import com.meera.core.utils.imagecapture.ui.ImageCaptureUtils
import com.meera.core.utils.imagecapture.ui.getImageFromCamera
import com.meera.core.utils.imagecapture.ui.model.ImageCaptureResultModel
import com.meera.core.utils.listeners.OrientationScreenListener
import com.meera.core.utils.showCommonError
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.meera.core.utils.tedbottompicker.TedBottomSheetPermissionActionsListener
import com.meera.core.utils.tedbottompicker.models.MediaViewerPreviewModeParams
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_api.model.MediaControllerNeedEditResponse
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.noomeera.nmrmediatools.NMRVideoAmplitude
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraAddComplaintFragmentBinding
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetAppInfoAsyncUseCase
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegate
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegateImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.complains.ui.ComplainsNavigator
import com.numplates.nomera3.modules.complains.ui.ComplaintFlowInteraction
import com.numplates.nomera3.modules.complains.ui.ComplaintFlowResult
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_MOMENT_ID
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_ROOM_ID
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_USER_ID
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_WHERE_VALUE
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAIN_TYPE
import com.numplates.nomera3.modules.complains.ui.KEY_EXTRA_USER_COMPLAIN
import com.numplates.nomera3.modules.complains.ui.model.UserComplainUiModel
import com.numplates.nomera3.modules.complains.ui.reason.ComplainType
import com.numplates.nomera3.modules.feed.ui.getScreenWidth
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.modules.uploadpost.ui.adapter.AttachmentPostAdapter
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentPostModel
import com.numplates.nomera3.modules.user.ui.entity.ComplainReasonId
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SHOW_MEDIA_GALLERY
import com.numplates.nomera3.presentation.utils.isEditorTempFile
import com.numplates.nomera3.presentation.view.adapter.MediaListAdapter
import com.numplates.nomera3.presentation.view.adapter.MediaListAdapter.MediaItemGallery.Companion.TYPE_CAMERA
import com.numplates.nomera3.presentation.view.adapter.MediaListAdapter.MediaItemGallery.Companion.TYPE_GALLERY
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.ui.mediaViewer.ImageViewerData
import com.numplates.nomera3.presentation.view.ui.mediaViewer.MediaViewer
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.pager.RecyclingPagerAdapter
import com.numplates.nomera3.presentation.view.ui.mediaViewer.listeners.OnDismissListener
import com.numplates.nomera3.presentation.view.utils.MeeraCoreTedBottomPickerActDependencyProvider
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


private const val OPEN_KEYBOARD_DELAY = 400L
private const val MEDIA_PREVIEW_MARGIN_TOP = 12
private const val EVENT_TITLE_MARGIN_BOTTOM = 16
private const val POST_INPUT_BOTTOM_OFFSET = 30
private const val POST_INPUT_TEXT_SIZE = 16
private const val POST_INPUT_MIN_LINES_VISIBLE = 2
private const val MEDIA_PICKER_OFFSET = 50
private const val COMPLAIN_WHERE_ERROR = "This property should be specified."

const val CACHE_DIR = "CACHE_DIR"

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class MeeraUserComplaintDetailsFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_add_complaint_fragment,
    behaviourConfigState = ScreenBehaviourState.Full
),
    BasePermission by BasePermissionDelegate(),
    BaseLoadImages by BaseLoadImagesDelegate(),
    SaveMediaFileDelegate by SaveMediaFileDelegateImpl(),
    IOnBackPressed,
    TedBottomSheetPermissionActionsListener {

    @Inject
    lateinit var cameraProviderBuilder: CameraProvider.Builder

    @Inject
    lateinit var getAppInfoUseCase: GetAppInfoAsyncUseCase

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    @Inject
    @Named(CACHE_DIR)
    lateinit var cacheDir: File

    @Inject
    lateinit var fileManager: FileManager

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraAddComplaintFragmentBinding::bind)
    private val act: MeeraAct by lazy {
        requireActivity() as MeeraAct
    }

    private var pathImage: String? = null
    private var pathVideo: String? = null
    private var mediaAttachmentUri: Uri? = null

    private var isLockBackPressed = false

    private val disposables = CompositeDisposable()
    private lateinit var mediaAdapter: MediaListAdapter

    private var isPreviewPermitted = false
    private var mediaPickerJob: Job? = null

    private var mediaPreviewMaxHeight: Int = 0

    private var mediaPicker: TedBottomSheetDialogFragment? = null
    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    private var complainUiModel: UserComplainUiModel? = null
    private val complaintViewModel by viewModels<UserComplaintDetailsViewModel> { App.component.getViewModelFactory() }
    private val attachmentsAdapter by lazy(LazyThreadSafetyMode.NONE) { AttachmentPostAdapter(complaintViewModel) }
    private var userId: Long? = null
    private var complaintWhere: AmplitudePropertyWhere? = null
    private var complainType: Int? = null
    private val complainsNavigator by lazy(LazyThreadSafetyMode.NONE) { ComplainsNavigator(requireActivity()) }
    private var complaintFlowResult = ComplaintFlowResult.CANCELLED
    private var complainSendResult: Boolean = true
    private var complaintResultHanding = AtomicBoolean(false)

    private var showMediaGallery: Boolean? = null
    private var optionalMomentId: Long? = null
        set(value) {
            if (value != 0L) field = value
        }
    private var optionalRoomId: Long? = null
        set(value) {
            if (value != 0L) field = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        showMediaGallery = arguments?.getBoolean(ARG_SHOW_MEDIA_GALLERY, false)
        complainUiModel = arguments?.getSerializable(KEY_EXTRA_USER_COMPLAIN) as? UserComplainUiModel
        userId = arguments?.getLong(KEY_COMPLAINT_USER_ID)
        complaintWhere = arguments?.getSerializable(KEY_COMPLAINT_WHERE_VALUE) as? AmplitudePropertyWhere
        optionalMomentId = arguments?.getLong(KEY_COMPLAINT_MOMENT_ID)
        optionalRoomId = arguments?.getLong(KEY_COMPLAINT_ROOM_ID)
        complainType = arguments?.getInt(KEY_COMPLAIN_TYPE, ComplainType.USER.key)

        binding.vCloseAddComplain.setBackIcon(R.drawable.ic_outlined_close_m)

        initTextChangedListener()
        initKeyboardBehavior()
        setupInputs()

        initMediaPickerJob()
        initObservers()
        setupKeyboardHeightProvider()
        act.permissionListener.add(listener)

        showMediaPicker { imageUri -> mediaAttachmentSelected(imageUri) }
    }


    override fun onBackPressed(): Boolean {
        mediaPickerJob?.cancel()
        return binding?.etWrite?.suggestionMenu?.onBackPressed()
            ?: binding?.vPostTextBackground?.getEditText()?.suggestionMenu?.onBackPressed()
            ?: isLockBackPressed
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        orientationScreenListener.onOrientationChanged(newConfig.orientation)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPicker?.dismissAllowingStateLoss()
    }

    override fun onDestroyView() {
        act.permissionListener.remove(listener)
        super.onDestroyView()
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
        keyboardHeightProvider?.release()
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
                    if (fromMediaPicker) {
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

    override fun onResume() {
        super.onResume()
        subscribeViewEvent()
    }

    private val listener: (requestCode: Int, permissions: Array<String>, grantResults: IntArray) -> Unit =
        { requestCode, permissions, grantResults ->
            if (requestCode == PERMISSION_MEDIA_CODE) {
                if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                    mediaPicker?.updateGalleryPermissionState(PermissionState.GRANTED)
                    isPreviewPermitted = true
                    initMediaAdapterClickListener()

                    complaintViewModel.liveGalleryMedia
                        .observe(this@MeeraUserComplaintDetailsFragment.viewLifecycleOwner) {
                            handleMediaUri(it)
                        }

                    complaintViewModel.requestLatestMedia()
                } else {
                    mediaPicker?.updateGalleryPermissionState(PermissionState.NOT_GRANTED_OPEN_SETTINGS)
                    isPreviewPermitted = false
                }
            }
        }

    private fun initMediaPickerJob() {
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

            binding?.addComplaintContentContainer?.setOnClickListener {
                openKeyboardOnComplainTextInput()
            }
        }
    }

    private fun subscribeViewEvent() {
        complainsNavigator.registerChangeReasonListener(this) { complain ->
            complainUiModel = complain
            changeStateAcceptButton()
        }
    }

    private fun handleMediaUri(it: List<Uri>) {
        val collection = mutableListOf<MediaListAdapter.MediaItemGallery>()
        collection.add(MediaListAdapter.MediaItemGallery(type = TYPE_CAMERA))
        it.forEach {
            val isVideo = fileManager.getMediaType(it) == MEDIA_TYPE_VIDEO
            val type =
                if (isVideo) MediaListAdapter.MediaItemGallery.TYPE_VIDEO else MediaListAdapter.MediaItemGallery.TYPE_MEDIA
            val mediaItemGallery = MediaListAdapter.MediaItemGallery(it, type)
            collection.add(mediaItemGallery)
        }
        collection.add(MediaListAdapter.MediaItemGallery(type = TYPE_GALLERY))
        mediaAdapter.resetAndAddData(collection)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupInputs() {
        binding?.etWrite?.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                binding?.etWrite?.requestFocus()
                mediaPicker?.dismissAllowingStateLoss()
                openKeyboardOnComplainTextInput()
            }
            return@setOnTouchListener false
        }
    }

    private fun initObservers() {
        complaintViewModel.attachmentsMedia.observe(viewLifecycleOwner) { attachments ->
            attachments?.let {
                submitItems(attachments)
                setRecyclerVisibility(attachments)
            }
            if (attachments.isEmpty()) mediaPicker?.updateAlreadySelectedMedia(emptyList())
        }

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

                    is UserComplaintDetailsEvent.FinishComplaintFlow -> finishComplaintFlow(isSuccess = event.isSuccess)
                }
            }
            .launchIn(lifecycleScope)

        complaintViewModel.screenState.observe(viewLifecycleOwner) { state ->
            binding?.vSendAddComplain?.isEnabled = false
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
        findNavController().popBackStack()
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
            ComplaintFlowResult.SUCCESS -> {
                complainsNavigator.sendDialogChainResult(
                    complaintReasonId = complainUiModel?.reasonId?.key ?: ComplainReasonId.OTHER.key)
            }

            ComplaintFlowResult.FAILURE,
            ComplaintFlowResult.CANCELLED -> {
                complainsNavigator.sendDialogChainResult(dismissed = true)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setRecyclerVisibility(attachments: List<UIAttachmentPostModel>) {
        if (attachments.isNotEmpty()) {
            binding?.apaiAttachment?.visible()
        } else {
            attachmentsAdapter.notifyDataSetChanged()
            deleteAttachment()
            binding?.apaiAttachment?.gone()
        }
    }

    private fun submitItems(attachments: List<UIAttachmentPostModel>) {
        if (attachments.isNotEmpty()) {
            setAttachmentItem(attachment = attachments[0])
        }
    }

    private fun setAttachmentItem(attachment: UIAttachmentPostModel) {
        calculateMediaPreviewHeight()
        binding?.apaiAttachment?.apply {
            bind(
                actions = complaintViewModel,
                attachment = attachment,
                mediaPreviewMaxWidth = getScreenWidth(),
                mediaPreviewMaxHeight = mediaPreviewMaxHeight,
                isNeedMediaPositioning = true
            )
        }
    }

    private fun initTextChangedListener() {
        binding?.etWrite?.let { inputText ->
            disposables.add(
                RxTextView.textChanges(inputText)
                    .debounce(150, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ text ->
                        if (text.toString().trim().isNotEmpty()) {
                            changeStateAcceptButton()
                        } else {
                            pathImage?.let { path ->
                                if (path.isEmpty()) {
                                    changeStateAcceptButton()
                                }
                            } ?: kotlin.run {
                                changeStateAcceptButton()
                            }
                        }
                    }, { Timber.e(it) })
            )
        }
    }

    private fun initKeyboardBehavior() {
        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            binding.llActionsContainer.setMargins(bottom = if (imeVisible) imeHeight else 0)
            insets
        }
    }

    private fun setupKeyboardHeightProvider() {


        binding?.root?.let { root ->
            keyboardHeightProvider = KeyboardHeightProvider(root)
            keyboardHeightProvider?.observer = { height ->
                if (height > 0) {
                    complaintViewModel.saveKeyboardHeight(height)
                    keyboardHeightProvider?.release()
                }
            }
        }
    }

    private fun initMediaAdapterClickListener() {
        mediaAdapter.onClickListener = { item ->
            when (item.type) {
                MediaListAdapter.MediaItemGallery.TYPE_MEDIA, MediaListAdapter.MediaItemGallery.TYPE_VIDEO -> {
                    item.mediaPreview?.let { uri ->
                        mediaAttachmentSelected(uri)
                    }
                }

                TYPE_CAMERA -> {
                    activity?.getImageFromCamera(object : ImageCaptureUtils.Listener {
                        override fun onResult(result: ImageCaptureResultModel) {
                            mediaAttachmentSelected(result.fileUri)
                        }
                    })
                }

                TYPE_GALLERY -> {
                    showMediaPicker { imageUri -> mediaAttachmentSelected(imageUri) }
                }
            }
        }
    }

    private fun calculateMediaPreviewHeight() {
        val contentContainerHeight: Int =
            getScreenHeight() - complaintViewModel.getKeyboardHeightForPicker()
        val addPostTitleHeight: Int = binding?.etWrite?.height ?: 0
        val addPostTitleFinalHeight: Int =
            if (addPostTitleHeight != 0) addPostTitleHeight + EVENT_TITLE_MARGIN_BOTTOM.dp else 0
        val eventViewsHeight: Int = addPostTitleFinalHeight
        val textPadding: Int = binding?.etWrite?.paint?.descent()?.toInt() ?: 0
        val postInputMargins =
            (binding?.etWrite?.marginTop ?: 0).toInt() + (binding?.etWrite?.marginBottom ?: 0).toInt()
        val postInputHeight: Int =
            POST_INPUT_TEXT_SIZE.sp * POST_INPUT_MIN_LINES_VISIBLE + postInputMargins + textPadding
        val offset = POST_INPUT_BOTTOM_OFFSET.dp - MEDIA_PREVIEW_MARGIN_TOP.dp
        val contentViewsHeight = getActionsContainerHeight() + eventViewsHeight + postInputHeight + offset
        mediaPreviewMaxHeight = contentContainerHeight - contentViewsHeight
    }

    private fun openKeyboard() {
        Timber.d("openKeyboard")
        doDelayed(400) {
            binding?.etWrite?.requestFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(binding?.etWrite, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun openKeyboardOnComplainTextInput(onCompleted: () -> Unit = {}) {
        Timber.d("openKeyboard")
        doDelayed(OPEN_KEYBOARD_DELAY) {
            binding?.etWrite?.let(::doOpenKeyboard)
            onCompleted.invoke()
        }
    }

    private fun doOpenKeyboard(inputView: EditText) {
        if (view != null) {
            inputView.requestFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(inputView, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun setupClickActions() {
        binding?.vCloseAddComplain?.backButtonClickListener = {
            binding?.etWrite?.suggestionMenu?.dismiss()
            context?.hideKeyboard(requireView())
            findNavController().popBackStack()
        }

        binding?.vSendAddComplain?.setThrottledClickListener {
            handleSendPost()
        }

        binding?.ivAttach?.setThrottledClickListener {
            binding?.etWrite?.clearFocus()
            showMediaPicker { imageUri -> mediaAttachmentSelected(imageUri) }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateMediaAdapter() {
        mediaAdapter.notifyDataSetChanged()
    }

    private fun showCameraSettingsDialog() {
        MeeraConfirmDialogBuilder()
            .setHeader(R.string.camera_settings_dialog_title)
            .setDescription(R.string.camera_settings_dialog_description)
            .setTopBtnText(R.string.camera_settings_dialog_action)
            .setBottomBtnText(R.string.camera_settings_dialog_cancel)
            .setTopClickListener {
                requireContext().openSettingsScreen()
            }
            .show(childFragmentManager)
    }

    private fun deleteAttachment() {
        deleteComplainImage()
        deleteVideo()
        mediaAttachmentUri = null
        changeStateAcceptButton()
    }

    private fun handleSendPost() {
        if (!isSendButtonLocked()) {
            context?.hideKeyboard(requireView())
            complaintViewModel.sendComplaint(
                userId = userId ?: return,
                reason = ComplainReasonId.OTHER,
                detailsParams = UserComplaintDetailsParams(
                    comment = binding?.etWrite?.text.toString(),
                    videoPath = pathVideo,
                    imagePath = pathImage,
                ),
                where = complaintWhere ?: error(COMPLAIN_WHERE_ERROR),
                momentId = optionalMomentId,
                roomId = optionalRoomId,
                complainType = complainType ?: ComplainType.USER.key
            )
        } else {
            binding?.vSendAddComplain?.visible()
            binding?.pbSendComplain?.gone()
        }
    }

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

    private fun mediaAttachmentSelected(uri: Uri, afterEdit: Boolean = false) {
        complaintViewModel.handleSelectedEditedMediaUri(afterEdit = afterEdit, uri)
        lifecycleScope.launch {
            val openPlace = complaintViewModel.openPlace
            when (act.getMediaControllerFeature().needEditMedia(uri = uri, openPlace = openPlace)) {
                is MediaControllerNeedEditResponse.VideoTooLong, MediaControllerNeedEditResponse.NeedToCrop -> {
                    view?.hideKeyboard()
                    hideAndResetAttachmentView()
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
                    mediaPicker?.updateAlreadySelectedMedia(complaintViewModel.getSelectedEditedMediaUri())
                }
            }
        }
    }

    private fun hideAndResetAttachmentView() {
        binding?.apaiAttachment?.gone()
        binding?.apaiAttachment?.resetView()
    }

    private fun openEditor(uri: Uri) {
        act.getMediaControllerFeature().open(
            uri = uri,
            callback = editorCallback,
            openPlace = complaintViewModel.openPlace
        )
    }

    private val editorCallback = object : MediaControllerCallback {
        override fun onPhotoReady(resultUri: Uri, nmrAmplitude: NMRPhotoAmplitude?) {
            showImageAttachment(resultUri)
        }

        override fun onVideoReady(resultUri: Uri, nmrAmplitude: NMRVideoAmplitude?) {
            if (resultUri.toString().isEmpty()) {
                showCommonError(getText(R.string.error_while_working_with_image), requireView())
            }
        }

        override fun onCanceled() {
            openKeyboard()
        }

        override fun onError() {
            showCommonError(getText(R.string.error_while_working_with_image), requireView())
        }
    }

    private fun changeStateAcceptButton() {
        if (!isSendButtonLocked()) {
            binding?.vSendAddComplain?.isEnabled = true
            binding?.vSendAddComplain?.setBackgroundTint(R.color.uiKitColorAccentPrimary)
            binding?.vSendAddComplain?.setTint(R.color.black)
        } else {
            binding?.vSendAddComplain?.isEnabled = false
            binding?.vSendAddComplain?.setBackgroundTint(R.color.uiKitColorDisabledTetriary)
            binding?.vSendAddComplain?.setTint(R.color.gray_background_button)
        }
    }

    private fun setNotEditedImage(uri: Uri) {
        showImageAttachment(uri)
    }

    private fun showVideoAttachment(uri: Uri) {
        uri.path?.let { path ->
            deleteVideo()
            pathImage = null
            pathVideo = path
            complaintViewModel.onVideoChosen(path)
            changeStateAcceptButton()
            openKeyboard()
            mediaAttachmentUri = uri
        }
    }

    private fun showImageAttachment(uri: Uri) {
        uri.path?.let { pathPhoto ->
            deleteComplainImage()
            pathVideo = null
            pathImage = pathPhoto
            complaintViewModel.onImageChosen(uri.toString())
            changeStateAcceptButton()
            openKeyboard()
            mediaAttachmentUri = uri
        }
    }

    private fun deleteComplainImage() {
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

    private val orientationScreenListener =
        object : OrientationScreenListener() {
            override fun onOrientationChanged(orientation: Int) {
                orientationChangedListener.invoke(orientation)
            }
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
                .setMeeraAct(meeraAct = act)
                .setImageList(data)
                .startPosition(0)
                .setOrientationChangedListener(orientationScreenListener)
                .onSaveImage { saveImage(it, false) }
                .shareAvailable(false)
                .copyAvailable(false)
                .onDismissListener(OnDismissListener {})
                .setLifeCycle(lifecycle)
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

    private fun getHalfExpandedMediaPickerHeight(): Int = (getScreenHeight() * MEDIA_PICKER_HALF_EXTENDED_RATIO).toInt()

    private fun getCollapsedMediaPickerHeight(): Int =
        complaintViewModel.getKeyboardHeightForPicker() + getActionsContainerHeight() + MEDIA_PICKER_OFFSET.dp

    private fun getActionsContainerHeight(): Int = binding?.llActionsContainer?.height ?: 0

    private fun showMediaPickerWithPermissionState(
        permissionState: PermissionState,
        onPickImageUri: (imageUri: Uri) -> Unit
    ) {
        mediaPicker?.dismissAllowingStateLoss()
        mediaPicker = loadSingleImageUri(
            activity = requireActivity(),
            viewLifecycleOwner = viewLifecycleOwner,
            type = MediaControllerOpenPlace.Common,
            needWithVideo = true,
            showGifs = true,
            suggestionsMenu = SuggestionsMenu(this, SuggestionsMenuType.ROAD),
            permissionState = permissionState,
            tedBottomSheetPermissionActionsListener = this,
            previewModeParams = MediaViewerPreviewModeParams(
                isPreviewModeEnabled = true,
                halfExtendedHeight = getHalfExpandedMediaPickerHeight(),
                collapsedHeight = getCollapsedMediaPickerHeight()
            ),
            selectedEditedMedia = listOf(),
            loadImagesCommonCallback = MeeraCoreTedBottomPickerActDependencyProvider(
                act = act,
                onReadyImageUri = { imageUri ->
                    onPickImageUri(imageUri)
                },
                onImageRemoved = {
                    complaintViewModel.attachmentsMedia.value?.firstOrNull()?.let {
                        complaintViewModel.onItemCloseClick(it)
                    }
                    deleteAttachment()
                    mediaPicker?.updateAlreadySelectedMedia(emptyList())
                },
                onDismissPicker = {
                    showMediaGallery = false
                    updateMediaAdapter()
                    openKeyboard()
                }
            ),
            cameraLensFacing = CameraCharacteristics.LENS_FACING_BACK,
        )
    }

    private fun saveImage(imageUrl: String, toCache: Boolean) {
        saveImage(imageUrl, {}, toCache)
    }

    private fun saveImage(imageUrl: String, onSuccess: (Uri) -> Unit, toCache: Boolean) {
        saveImageOrVideoFile(
            imageUrl = imageUrl,
            act = requireActivity(),
            viewLifecycleOwner = viewLifecycleOwner,
            successListener = onSuccess,
            toCache
        )
    }
}
