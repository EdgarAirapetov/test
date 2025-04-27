package com.numplates.nomera3.modules.redesign.fragments.main.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Rect
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.core.view.postDelayed
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.color.MaterialColors.ALPHA_DISABLED
import com.google.android.material.color.MaterialColors.ALPHA_FULL
import com.jakewharton.rxbinding2.widget.RxTextView
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.enums.PermissionState
import com.meera.core.common.MEDIA_PICKER_HALF_EXTENDED_RATIO
import com.meera.core.dialogs.ConfirmDialogBuilder
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.animateHeight
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.getScreenHeight
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.goneAnimation
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.extensions.parcelable
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setTint
import com.meera.core.extensions.sp
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleAnimation
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
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.meera.core.utils.tedbottompicker.TedBottomSheetPermissionActionsListener
import com.meera.core.utils.tedbottompicker.models.MediaViewerPreviewModeParams
import com.meera.db.models.UploadType
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_api.model.MediaControllerNeedEditResponse
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.noomeera.nmravatarssdk.ui.positiveButton
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.noomeera.nmrmediatools.NMRVideoAmplitude
import com.numplates.nomera3.App
import com.numplates.nomera3.MEDIA_GALLERY_ITEM_COUNT_IN_ROW
import com.numplates.nomera3.MEDIA_GALLERY_ITEM_PADDING
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentAddPostBinding
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetAppInfoAsyncUseCase
import com.numplates.nomera3.modules.baseCore.helper.AudioEventListener
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegate
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegateImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudeCreatePostWhichButton
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.chat.helpers.InputReceiveContentListener
import com.numplates.nomera3.modules.common.ActivityToolsProvider
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.getScreenWidth
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurator
import com.numplates.nomera3.modules.maps.ui.events.model.EventLabelUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventParametersUiModel
import com.numplates.nomera3.modules.maps.ui.events.road_privacy.EventRoadPrivacyDialogFragment
import com.numplates.nomera3.modules.maps.ui.events.road_privacy.EventRoadPrivacyDialogHost
import com.numplates.nomera3.modules.music.ui.entity.MusicCellUIEntity
import com.numplates.nomera3.modules.music.ui.entity.event.UserActionEvent
import com.numplates.nomera3.modules.music.ui.entity.state.MusicViewModelState
import com.numplates.nomera3.modules.music.ui.fragment.AddMusicBottomFragment
import com.numplates.nomera3.modules.music.ui.viewmodel.MeeraMusicViewModel
import com.numplates.nomera3.modules.registration.ui.phoneemail.HeaderDialogType
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.MeeraNewPostFormatter
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.ui.base.SuggestedTagListMenu
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.modules.upload.data.post.UploadPostBundle
import com.numplates.nomera3.modules.upload.data.post.toUploadPostBundle
import com.numplates.nomera3.modules.upload.mapper.UploadBundleMapper
import com.numplates.nomera3.modules.upload.util.UPLOAD_BUNDLE_KEY
import com.numplates.nomera3.modules.upload.util.getUploadBundle
import com.numplates.nomera3.modules.uploadpost.ui.ARG_TO_PERSONAL_ROAD
import com.numplates.nomera3.modules.uploadpost.ui.PostEditValidator
import com.numplates.nomera3.modules.uploadpost.ui.adapter.AttachmentPostAdapter
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentPostModel
import com.numplates.nomera3.modules.uploadpost.ui.entity.NotAvailableReasonUiEntity
import com.numplates.nomera3.modules.uploadpost.ui.view.AddPostAttachmentItemView
import com.numplates.nomera3.modules.uploadpost.ui.viewmodel.AddPostViewModel
import com.numplates.nomera3.modules.user.ui.dialog.BlockUserByAdminDialogFragment
import com.numplates.nomera3.modules.user.ui.entity.UserPermissions
import com.numplates.nomera3.presentation.model.enums.RoadSelectionEnum
import com.numplates.nomera3.presentation.model.enums.WhoCanCommentPostEnum
import com.numplates.nomera3.presentation.model.enums.WhoCanCommentPostEnum.COMMUNITY_MEMBERS
import com.numplates.nomera3.presentation.model.enums.WhoCanCommentPostEnum.FRIENDS
import com.numplates.nomera3.presentation.model.enums.getRoadTypeForState
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SHOW_MEDIA_GALLERY
import com.numplates.nomera3.presentation.view.adapter.MediaListAdapter
import com.numplates.nomera3.presentation.view.adapter.MediaListAdapter.MediaItemGallery.Companion.TYPE_CAMERA
import com.numplates.nomera3.presentation.view.adapter.MediaListAdapter.MediaItemGallery.Companion.TYPE_GALLERY
import com.numplates.nomera3.presentation.view.adapter.MediaListAdapter.MediaItemGallery.Companion.TYPE_MEDIA
import com.numplates.nomera3.presentation.view.adapter.MediaListAdapter.MediaItemGallery.Companion.TYPE_VIDEO
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.ui.bottomMenu.RoadSelectBottomSheet
import com.numplates.nomera3.presentation.view.ui.bottomMenu.SurveyBottomMenu
import com.numplates.nomera3.presentation.view.ui.customView.MediaPlayerListener
import com.numplates.nomera3.presentation.view.ui.mediaViewer.ImageViewerData
import com.numplates.nomera3.presentation.view.ui.mediaViewer.MediaViewer
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.pager.RecyclingPagerAdapter
import com.numplates.nomera3.presentation.view.ui.mediaViewer.listeners.OnDismissListener
import com.numplates.nomera3.presentation.view.utils.CoreTedBottomPickerActDependencyProvider
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.utils.apphints.createTooltip
import com.numplates.nomera3.presentation.view.utils.apphints.showAboveView
import com.numplates.nomera3.presentation.view.utils.apphints.showBelowView
import com.numplates.nomera3.presentation.view.utils.camera.CameraOrientation
import com.numplates.nomera3.presentation.view.utils.camera.CameraProvider
import com.numplates.nomera3.presentation.viewmodel.viewevents.AddPostViewEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.PostViewEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.min

private const val DEFAULT_ANIMATION_DURATION = 100L
private const val DELAY_HINT_MS = 1000L
private const val DURATION_HINT_MS = 4000L
private const val DELAY_FOR_SHOW_MEDIA_LIST = 500L
private const val DELAY_FOR_SCROLLING_VIEW_TO_BOTTOM = 30L
private const val OPEN_KEYBOARD_DELAY = 400L
private const val DISABLE_VIEW_DELAY = 1000L
private const val MEDIA_PREVIEW_MARGIN_TOP = 12
private const val EVENT_TITLE_MARGIN_BOTTOM = 16
private const val POST_INPUT_BOTTOM_OFFSET = 30
private const val POST_INPUT_TEXT_SIZE = 16
private const val POST_INPUT_MIN_LINES_VISIBLE = 2

@Deprecated("Заменён единым классом MeeraCreatePostFragment")
class MeeraAddPostFragmentNew
    : BaseFragmentNew<MeeraFragmentAddPostBinding>(),
    BasePermission by BasePermissionDelegate(),
    BaseLoadImages by BaseLoadImagesDelegate(),
    SaveMediaFileDelegate by SaveMediaFileDelegateImpl(),
    IOnBackPressed,
    TedBottomSheetPermissionActionsListener,
    EventRoadPrivacyDialogHost {

    @Inject
    lateinit var cameraProviderBuilder: CameraProvider.Builder

    @Inject
    lateinit var getAppInfoUseCase: GetAppInfoAsyncUseCase

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    private var newPostFormatter: MeeraNewPostFormatter? = null
    private var settings: Settings? = null

    @Inject
    lateinit var fileManager: FileManager

    private val addPostViewModel by viewModels<AddPostViewModel> { App.component.getViewModelFactory() }
    private val musicViewModel by viewModels<MeeraMusicViewModel>()

    private var groupId: Int = 0
    private var isPersonalPost = false
    private var pathImage: String? = null
    private var pathVideo: String? = null
    private var isVideoWasCompressed: Boolean = false
    private var mediaAttachmentUri: Uri? = null
    private var media: MusicCellUIEntity? = null
    private var userPermissions: UserPermissions? = null

    private var _media: MusicCellUIEntity? = null
        get() = media

    private var isLockBackPressed = false

    private val disposables = CompositeDisposable()
    private lateinit var mediaAdapter: MediaListAdapter

    private var isPreviewPermitted = false
    private var roadType = RoadSelectionEnum.MAIN
    private var whoCanComment = WhoCanCommentPostEnum.EVERYONE
    private var mediaPickerJob: Job? = null
    private var mediaGalleryOpenPreviewJob: Job? = null
    private var currentDialog: AppCompatDialogFragment? = null

    private var commentingSettingsTooltipJob: Job? = null

    private var scrollViewHeightWithOpenedKeyBoard: Int? = null
    private var scrollViewHeightWithClosedKeyBoard: Int? = null

    private var mediaPreviewMaxHeight: Int = 0

    private var isTextOnBackgroundFullyVisible: Boolean = true
    private var isTextBackgroundActivated: Boolean = false

    private var mediaPicker: TedBottomSheetDialogFragment? = null
    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    private var apaiAttachment = WeakReference<View>(null)

    private val attachmentsAdapter: AttachmentPostAdapter by lazy(LazyThreadSafetyMode.NONE) {
        AttachmentPostAdapter(addPostViewModel)
    }

    private val commentingSettingsTooltip: PopupWindow? by lazy {
        createTooltip(
            context,
            R.layout.app_hint_post_commenting_policy
        )
    }

    private var postRoadSwitchTooltipJob: Job? = null
    private val postRoadSwitchTooltip: PopupWindow? by lazy {
        createTooltip(
            context,
            R.layout.app_hint_post_road_switch
        )
    }

    private var shareMusicTooltipJob: Job? = null
    private val shareMusicTooltip: PopupWindow? by lazy {
        createTooltip(
            context,
            R.layout.app_hint_add_post_music
        )
    }

    private var mediaPositioningTooltipJob: Job? = null
    private val mediaPositioningTooltip: PopupWindow? by lazy {
        createTooltip(
            context,
            R.layout.app_hint_post_media_positioning
        )
    }

    private val openFrom: OpenFrom?
        get() = arguments?.parcelable(OpenFrom.EXTRA_KEY)

    private var suggestedMenu: SuggestedTagListMenu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)

        settings = getAppInfoUseCase.executeBlocking()
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> MeeraFragmentAddPostBinding
        get() = MeeraFragmentAddPostBinding::inflate

    override fun onBackPressed(): Boolean {
        mediaPickerJob?.cancel()
        return binding?.etWrite?.suggestionMenu?.onBackPressed()
            ?: binding?.etAddPostTitle?.suggestionMenu?.onBackPressed()
            ?: binding?.vPostTextBackground?.getEditText()?.suggestionMenu?.onBackPressed()
            ?: isLockBackPressed
    }

    private var showMediaGallery: Boolean? = null
    private val disposable = CompositeDisposable()

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        showMediaGallery = arguments?.getBoolean(ARG_SHOW_MEDIA_GALLERY, false)
        apaiAttachment = WeakReference(binding!!.apaiAttachment)

        val eventParametersUiModel = arguments?.getParcelable<EventParametersUiModel>(KEY_MAP_EVENT)
        eventParametersUiModel?.let(addPostViewModel::setEventParameters)
        newSuggestionMenu()
        initTextChangedListener()
        checkArgumentsForEditPost()
        setupToolbar()
        setupStatusBar(view)
        setupInputs()
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

            binding?.addPostContentContainer?.setOnClickListener {
                openKeyboardOnPostTextInput()
            }
        }

        initAttachmentAdapter()
        initMusic()
        initTextBackgrounds()
        initObservers()
        setupKeyboardHeightProvider()
        initAddButtonsVisibility()
        logPostCreateOpen(openFrom, showMediaGallery)

        act.permissionListener.add(listener)
    }

    val listener: (requestCode: Int, permissions: Array<String>, grantResults: IntArray) -> Unit =
        { requestCode, permissions, grantResults ->
            if (requestCode == PERMISSION_MEDIA_CODE) {
                if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                    mediaPicker?.updateGalleryPermissionState(PermissionState.GRANTED)
                    initMediaGallery()
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

    private fun checkArgumentsForEditPost() {
        arguments?.let { bundle ->
            groupId = if (bundle.containsKey(IArgContainer.ARG_GROUP_ID)) {
                bundle.get(IArgContainer.ARG_GROUP_ID) as Int
            } else 0
            isPersonalPost = bundle.containsKey(ARG_TO_PERSONAL_ROAD)
            if (bundle.containsKey(IArgContainer.ARG_POST)) {
                val post = bundle.get(IArgContainer.ARG_POST) as PostUIEntity
                addPostViewModel.setPost(post)
                tryToRestoreUploadModel(post.toUploadPostBundle())
                post.postImage?.let { postImage ->
                    saveImage(postImage, { imageUri ->
                        showImageAttachment(imageUri)
                    }, true)
                }
                post.video?.let { video ->
                    post.videoPreview?.let { preview ->
                        saveImage(preview, { imageUri ->
                            initVideoPreview(imageUri, video)
                        }, true)
                    }
                }
            } else {
                tryToRestoreUploadModel()
            }
        }
    }

    override fun onEventRoadDialogPrivacyAllIsSet() {
        addPostViewModel.publishCurrentUploadBundle()
    }

    override fun onEventRoadDialogPrivacyCancelled() {
        resetSendButton()
    }

    fun createUploadPostBundle(): UploadPostBundle {
        val postTitle = binding?.etAddPostTitle?.text?.toString() ?: ""
        val postText = binding?.etWrite?.text?.toString()?.trim() ?: ""
        val event = addPostViewModel.getEventEntity()?.copy(title = postTitle)
        val isTextBackgroundShowing = binding?.vPostTextBackground?.isShowing() ?: false
        return UploadPostBundle(
            text = postText,
            postId = addPostViewModel.getPost()?.postId,
            groupId = groupId,
            imagePath = pathImage,
            videoPath = pathVideo,
            mediaAttachmentUriString = mediaAttachmentUri?.toString(),
            roadType = roadType.state,
            whoCanComment = whoCanComment,
            media = media?.mediaEntity,
            backgroundId = if (isTextBackgroundShowing) binding?.vPostTextBackground?.getPostBackground()?.id else null,
            backgroundUrl = if (isTextBackgroundShowing) binding?.vPostTextBackground?.getPostBackground()?.url else null,
            fontSize = if (isTextBackgroundShowing) binding?.vPostTextBackground?.getRelativeFontSize() else null,
            event = event,
            mediaPositioning = addPostViewModel.getMediaPositioningForUpload(
                y = if (apaiAttachment.get() is MeeraAddPostAttachmentItemViewNew) {
                    (apaiAttachment.get() as MeeraAddPostAttachmentItemViewNew).getImageYPosition()
                } else {
                    (apaiAttachment.get() as AddPostAttachmentItemView).getImageYPosition()
                }
            ),
            mediaList = null
        )
    }

    private fun logPostCreateOpen(openFrom: OpenFrom?, isClickToIcon: Boolean?) {
        if (openFrom == null) {
            return
        }

        val whichButton = if (isClickToIcon == true) {
            AmplitudeCreatePostWhichButton.ICON
        } else {
            AmplitudeCreatePostWhichButton.LINE
        }

        if (!addPostViewModel.isEditPost()) {
            addPostViewModel.getAmplitudeHelper().logCreatePostClick(
                openFrom.amplitudePropertyWhere,
                whichButton
            )
        }
    }

    override fun onResume() {
        super.onResume()

        subscribeViewEvent()
    }

    override fun onPause() {
        super.onPause()

        unSubscribeViewEvent()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupInputs() {
        if (addPostViewModel.isEventPost()) {
            binding?.etWrite?.hint = resources.getString(R.string.map_events_configuration_description_hint)
            binding?.etWrite?.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    binding?.etWrite?.suggestionMenu?.setEditText(binding?.etWrite)
                    if (binding?.etAddPostTitle?.text.isNullOrEmpty()) {
                        binding?.etAddPostTitle?.setHintTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.ui_purple
                            )
                        )
                    }
                }
            }
            binding?.etAddPostTitle?.imeOptions = EditorInfo.IME_ACTION_NEXT
            binding?.etAddPostTitle?.setRawInputType(InputType.TYPE_CLASS_TEXT)
            val newLinesFilter = InputFilter { source, start, end, dest, dstart, dend ->
                val replacingString = source?.subSequence(start, end)?.toString()
                if (replacingString?.contains("\n") == true) {
                    replacingString.replace("\n", " ")
                } else {
                    null
                }
            }
            binding?.etAddPostTitle?.filters = binding?.etAddPostTitle?.filters?.plus(newLinesFilter)
            binding?.etAddPostTitle?.addTextChangedListener {
                binding?.etAddPostTitle?.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.ui_gray))
                if (scrollViewHeightWithOpenedKeyBoard == null)
                    scrollViewHeightWithOpenedKeyBoard = binding?.nsvAddPost?.height
            }
            binding?.etAddPostTitle?.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    binding?.etAddPostTitle?.suggestionMenu?.setEditText(binding?.etAddPostTitle)
                }
            }
            view?.post { binding?.etAddPostTitle?.let(::doOpenKeyboard) }
        } else {
            newPostFormatter = MeeraNewPostFormatter.createNullable(requireContext(), binding, settings)
            binding?.etAddPostTitle?.gone()
            binding?.etWrite?.suggestionMenu?.setEditText(binding?.etWrite)
        }

        binding?.etWrite?.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                binding?.etWrite?.requestFocus()
                mediaPicker?.dismissAllowingStateLoss()
                openKeyboardOnPostTextInput()
            }
            return@setOnTouchListener false
        }
    }

    private fun subscribeViewEvent() {
        disposable.add(
            addPostViewModel.streamEvent.observeOn(AndroidSchedulers.mainThread())
                .subscribe { event ->
                    onViewEvent(event)
                })
    }

    private fun unSubscribeViewEvent() {
        disposable.clear()
    }

    private fun onViewEvent(event: AddPostViewEvent) {
        when (event) {
            is AddPostViewEvent.Empty -> {
                return
            }

            is AddPostViewEvent.UploadStarting -> {
                musicViewModel.handleUIAction(action = UserActionEvent.UnSubscribe)
                finishPublishing()
            }

            is AddPostViewEvent.NeedToShowRoadPrivacyDialog -> EventRoadPrivacyDialogFragment
                .getInstance(event.roadPrivacySetting)
                .show(childFragmentManager, EventRoadPrivacyDialogFragment::class.java.name)

            is AddPostViewEvent.NeedToShowModerationDialog -> showEventModerationDialog()
            is AddPostViewEvent.NeedToShowResetEditedMediaDialog -> showPostMediaResetDialog(
                event.uri,
                event.isAdding,
                event.openCamera
            )

            is AddPostViewEvent.SetAttachment -> getMediaByPicker(event.uri, event.afterEdit)
            is AddPostViewEvent.RemoveAttachment -> removeImageAttachment()
            is AddPostViewEvent.KeyboardHeightChanged -> changeMediaViewsHeight()
            is AddPostViewEvent.OpenCamera -> openCamera(event.afterResetMedia)
            is AddPostViewEvent.ShowMediaPicker -> showMediaPicker { imageUri -> handleMediaByPicker(imageUri) }
            is AddPostViewEvent.ShowAvailabilityError -> {
                showNotAvailableError(event.reason)
                resetSendButton()
            }

            is AddPostViewEvent.UploadVideo -> saveImage(event.videoUrl, onSuccess = {
                if (addPostViewModel.isPreviewAttachmentSat()) showVideoAttachment(it)
            }, toCache = true)
            else -> Unit
        }
        addPostViewModel.clearLastEvent()
    }

    private fun openCamera(afterResetMedia: Boolean) {
        activity?.getImageFromCamera(object : ImageCaptureUtils.Listener {
            override fun onResult(result: ImageCaptureResultModel) {
                addPostViewModel.setMediaWasCompressed(false)
                mediaAttachmentSelected(result.fileUri)
            }

            override fun onFailed() {
                addPostViewModel.actionCameraCaptureFailed(afterResetMedia)
            }
        })
    }

    private fun changeMediaViewsHeight() {
        calculateMediaPreviewHeight(needUpdate = true)
        if (apaiAttachment.get() is MeeraAddPostAttachmentItemViewNew) {
            (apaiAttachment.get() as MeeraAddPostAttachmentItemViewNew).updateMaxHeight(mediaPreviewMaxHeight)
        } else {
            (apaiAttachment.get() as AddPostAttachmentItemView).updateMaxHeight(mediaPreviewMaxHeight)
        }
        mediaPicker?.updatePreviewCollapsedHeight(getCollapsedMediaPickerHeight())
    }

    private fun tryToRestoreUploadModel(uploadModel: UploadPostBundle? = null) {
        var model = uploadModel
        if (model == null) {
            model = arguments?.getUploadBundle()?.let { uploadModelJson ->
                UploadBundleMapper.map(UploadType.Post, uploadModelJson)
            } as? UploadPostBundle ?: return

            model.postId?.let { postId ->
                addPostViewModel.setPost(
                    PostUIEntity(
                        postId = postId
                    )
                )
            }
        }

        binding?.etWrite?.setText(model.text)
        binding?.etWrite?.setSelection(model.text.length)
        groupId = model.groupId ?: 0
        isVideoWasCompressed = model.wasCompressed
        addPostViewModel.setMediaWasCompressed(model.wasCompressed)
        roadType = getRoadTypeForState(model.roadType)
        whoCanComment = model.whoCanComment ?: WhoCanCommentPostEnum.EVERYONE

        if (whoCanComment.state != WhoCanCommentPostEnum.EVERYONE.state) {
            binding?.vCommentSettingIndicator?.visible()
            binding?.coverCommentSettingIndicator?.visible()
        }

        model.media?.let {
            updateMusicContainerUI(MusicCellUIEntity(mediaEntity = it))
        }
        model.event?.let { eventEntity ->
            if (addPostViewModel.getEventEntity() == null) {
                addPostViewModel.setEventEntity(eventEntity)
            }
            binding?.etAddPostTitle?.setText(eventEntity.title)
            binding?.etAddPostTitle?.setSelection(eventEntity.title.length)
        }

        model.mediaAttachmentUriString?.let {
            val mediaAttachmentUri = Uri.parse(it)
            model.imagePath?.let { showImageAttachment(mediaAttachmentUri) }
            model.videoPath?.let { showVideoAttachment(mediaAttachmentUri) }
        }

        if (groupId == 0) {
            handleRoadType()
        }
    }

    private fun handleRoadType() {
        when (roadType) {
            RoadSelectionEnum.MAIN -> onMainRoad()
            RoadSelectionEnum.MY -> onMyRoadClicked()
        }
    }

    private fun initObservers() {
        musicViewModel.liveState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MusicViewModelState.AddMusic -> addMusic(state)
                is MusicViewModelState.ReplaceMusic -> replaceMusic(state)
            }
        }
        addPostViewModel.livePostViewEvents.observe(viewLifecycleOwner) { postViewEvent ->
            when (postViewEvent) {
                is PostViewEvent.PermissionsReady -> {
                    handlePermissions(postViewEvent.permissions)
                }

                is PostViewEvent.OnEditImageByClick -> {
                    Timber.e("openEditor 3")
                    openEditor(uri = Uri.parse(postViewEvent.path))
                }

                is PostViewEvent.OnAddStickerClick -> {
                    openEditor(uri = Uri.parse(postViewEvent.path), showStickers = true)
                }

                is PostViewEvent.OnOpenImage -> {
                    openPhoto(postViewEvent.path)
                }

                is PostViewEvent.OnVideoPlay -> {
                    musicViewModel.handleUIAction(action = UserActionEvent.UnSubscribe)
                    openVideo(postViewEvent.path)
                }

                is PostViewEvent.OnEditVideoByClick -> {
                    Timber.e("openEditor 2")
                    openEditor(uri = Uri.parse(postViewEvent.path))
                }

                is PostViewEvent.MediaAttachmentSelected -> {
                    mediaAttachmentSelected(postViewEvent.uri)
                }

                else -> {
                    throw RuntimeException("Unprocessed event - $postViewEvent")
                }
            }
        }
        addPostViewModel.liveEventLabelUiModel.observe(viewLifecycleOwner, this::handleEventPost)
        addPostViewModel.liveGalleryMedia.observe(viewLifecycleOwner, this::handleMediaUri)
    }

    private fun handleEventPost(eventLabelUiModel: EventLabelUiModel) {
        apaiAttachment = WeakReference(binding?.apaiAttachmentEvent)
        binding?.apply {
            elvAddPostEvenLabel.setModel(eventLabelUiModel)
            ivAddPostEventLabelEdit.setThrottledClickListener {
                ivAddPostEventLabelEdit.hideKeyboard()
                ivAddPostEventLabelEdit.postDelayed(EVENT_EDIT_NAVIGATION_DELAY_MS) {
                    (parentFragment as? EventConfigurator)?.onEditEvent()
                }
            }
            binding?.addPostContentContainer?.setMargins(16.dp, 0, 16.dp, 0)
            binding?.vgAddPostEventLabelLayout?.setMargins(16.dp, 60.dp, 16.dp, 0)

            val bgLabel: Int

            if (eventLabelUiModel.attachment != null) {
                closeMediaPreview()
                deletePostImage()
                pathVideo = null
                pathImage = eventLabelUiModel.attachment.attachmentResource
                checkUpload()
                if (addPostViewModel.isEventPost()) {
                    binding?.etAddPostTitle?.let(::doOpenKeyboard)
                }
                mediaAttachmentUri = Uri.parse(eventLabelUiModel.attachment.attachmentResource)
//                calculateMediaPreviewHeight(needUpdate = false)
//                binding?.apaiAttachmentEvent?.apply {
//                        val isNeedMediaPositioning =
//                            addPostViewModel.getFeatureTogglesContainer().postMediaPositioningFeatureToggle.isEnabled
//                        Timber.e("setAttachmentItem ")
//                        bind(
//                            actions = addPostViewModel,
//                            attachment = eventLabelUiModel.attachment,
//                            mediaPreviewMaxWidth = getScreenWidth(),
//                            mediaPreviewMaxHeight = mediaPreviewMaxHeight,
//                            isNeedMediaPositioning = isNeedMediaPositioning,
//                        )
//                    }
                addPostViewModel.onImageChosen(eventLabelUiModel.attachment.attachmentResource)
                apaiAttachmentEvent.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_bottomsheet_header)
//            vg_add_post_event_label_layout
                bgLabel = R.drawable.bg_event_label

            } else {
                bgLabel = R.drawable.bg_bottomsheet_header
            }
            binding?.vgAddPostEventLabelLayoutInner?.background = ContextCompat.getDrawable(requireContext(), bgLabel)


            binding?.abAddBost?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))
            binding?.llActionsContainer?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding?.vPostSelectBackground?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))

            vgAddPostEventLabelLayout.visible()
            vgAddPostEventLabelLayoutInner.visible()
            binding?.viewEventDevider?.visible()
        }
    }

    private fun handlePermissions(permissions: UserPermissions) {
        this.userPermissions = permissions
        if (!permissions.mainRoad.canCreatePostInMainRoad) {
            onMyRoadClicked()
        }
    }

    private fun initTextBackgrounds() {
        binding?.apply {
            val backgrounds = addPostViewModel.getPostBackgrounds()
            val initBackground = addPostViewModel.getPost()?.backgroundId
                ?.let { backgroundId -> backgrounds.firstOrNull { it.id == backgroundId } } ?: null

            vPostSelectBackground.bind(
                postBackgrounds = backgrounds,
                initBackground = initBackground,
                onBackgroundSelected = { background, inputSelection ->
                    if (vPostTextBackground.isShowing()) {
                        vPostTextBackground.setBackground(background)
                    } else {
                        vPostTextBackground.showAsEditable(
                            text = etWrite.text.toString(),
                            background = background,
                            inputSelection = inputSelection,
                            onTextChanged = { changedText ->
                                etWrite.setText(changedText)
                            },
                            showDefaultInput = {
                                val selection = vPostTextBackground.getEditText().selectionEnd
                                val text = vPostTextBackground.getEditText().text
                                changeVisibilityTextBackground(isVisible = false)
                                with(etWrite) {
                                    setText(text)
                                    setSelection(min(selection, length()))
                                    visible()
                                    requestFocus()
                                    highlightAllUniqueNamesAndHashTags()
                                }
                            }
                        )
                    }
                },
                onHideBackgrounds = {
                    isTextBackgroundActivated = false
                    changeVisibilityTextBackground(isVisible = false)
                }
            )


            vPostTextBackground.setupHeight()

            if (addPostViewModel.getPost()?.backgroundId != null) {
                initBackground?.let {
                    vPostTextBackground.postDelayed({
                        vPostTextBackground.showAsEditable(
                            text = etWrite.text.toString(),
                            background = initBackground,
                            inputSelection = etWrite.selectionEnd,
                            onTextChanged = { changedText ->
                                etWrite.setText(changedText)
                            },
                            showDefaultInput = {
                                val selection = etWrite.selectionEnd
                                val text = vPostTextBackground.getEditText().text
                                changeVisibilityTextBackground(isVisible = false)
                                with(etWrite) {
                                    setText(text)
                                    setSelection(min(selection, length()))
                                    visible()
                                    requestFocus()
                                    highlightAllUniqueNamesAndHashTags()
                                }
                            }
                        )
                        etWrite.setSelection(etWrite.text?.length ?: 0)
                        isTextBackgroundActivated = true
                        changeVisibilityTextBackground(isVisible = true)

                    }, DELAY_HINT_MS)
                }
            } else {
                etWrite.enableSuggestionMenu()
                vPostTextBackground.getEditText().enableSuggestionMenu()
            }

            ivAddBackground.setOnClickListener {
                isTextBackgroundActivated = true
                changeVisibilityTextBackground(isVisible = true)
                addPostViewModel.onTapPostBackgroundAnalytic()
            }

            ivAddBackground.isVisible =
                addPostViewModel.getFeatureTogglesContainer().postsWithBackgroundFeatureToggle.isEnabled
        }
    }

    private fun changeVisibilityTextBackground(isVisible: Boolean) {
        setEnableActionsButtons(isEnabled = false)
        binding?.apply {
            if (isVisible) {
                val selection = etWrite.selectionEnd
                etWrite.gone()
                llActionsContainer.goneAnimation(duration = 50)
                vPostSelectBackground.show(
                    inputSelection = selection,
                    onFinish = {
                        suggestedMenu?.setEditText(vPostTextBackground.getEditText())
                        with(vPostTextBackground.getEditText()) {
                            highlightAllUniqueNamesAndHashTags()
                            setSelection(min(selection, length()))
                            requestFocus()
                        }
                        setEnableActionsButtons(isEnabled = true)
                        closeMediaPreview()
                        etWrite.enableSuggestionMenu()
                        vPostTextBackground.getEditText().enableSuggestionMenu()
                    }
                )
            } else {
                val selection = vPostTextBackground.getEditText().selectionEnd
                val text = vPostTextBackground.getEditText().text
                llActionsContainer.visibleAnimation(duration = 50)
                vPostSelectBackground.hide(
                    onFinish = {
                        vPostTextBackground.hide()
                        etWrite.apply {
                            visible()
                            setText(text)
                            setSelection(min(selection, length()))
                            requestFocus()
                            suggestedMenu?.setEditText(this)
                            highlightAllUniqueNamesAndHashTags()
                        }
                        setEnableActionsButtons(isEnabled = true)
                        etWrite.enableSuggestionMenu()
                        vPostTextBackground.getEditText().enableSuggestionMenu()
                    }
                )
            }
        }
    }

    private fun setEnableActionsButtons(isEnabled: Boolean) {
        binding?.apply {
            listOf<View>(ivAttach, ivAddMusic, ivAddBackground).forEach {
                it.isEnabled = isEnabled
            }
        }
    }

    private fun disableViewForDelay(view: View?, delay: Long) {
        view?.apply {
            isEnabled = false
            postDelayed({
                isEnabled = true
            }, delay)
        }
    }

    private fun addMusic(state: MusicViewModelState.AddMusic) {
        updateMusicContainerUI(state.entity)
        newPostFormatter?.onPostChanged()
    }

    private fun replaceMusic(state: MusicViewModelState.ReplaceMusic) {
        updateMusicContainerUI(state.entity)
    }

    private fun initMusic() {
        binding?.mpcMedia?.setOnActionBtnClickListener {
            hideMusicContainer()
            media = null
            isPreviewPermitted
            checkUpload()
        }

        val audioEventListener =
            object : AudioEventListener {
                override fun onPlay(withListener: Boolean) {
                    binding?.mpcMedia?.startPlaying(withListener)
                }

                override fun onPause(isReset: Boolean) {
                    binding?.mpcMedia?.stopPlaying(false, isReset = isReset)
                }

                override fun onLoad(isDownload: Boolean) {
                    if (isDownload) binding?.mpcMedia?.startDownloading()
                    else binding?.mpcMedia?.stopDownloading()
                }

                override fun onProgress(percent: Int) {
                    binding?.mpcMedia?.setProgress(percent)
                }
            }

        binding?.mpcMedia?.initMediaController(
            object : MediaPlayerListener {
                override fun onPlay(withListener: Boolean) {
                    if (withListener)
                        _media?.let {
                            musicViewModel.handleUIAction(
                                action = UserActionEvent.PlayClicked(it, null),
                                audioEventListener = audioEventListener,
                                adapterPosition = 0
                            )
                        }
                }

                override fun onStop(withListener: Boolean, isReset: Boolean) {
                    if (withListener)
                        _media?.let {
                            musicViewModel.handleUIAction(UserActionEvent.StopClicked(it))
                        }
                }

                override fun clickShare() = Unit
            })
    }

    private fun cancelPostIfEmpty(cancelAction: () -> Unit) {
        binding?.etWrite?.suggestionMenu?.dismiss()
        binding?.etAddPostTitle?.suggestionMenu?.dismiss()
        binding?.vPostTextBackground?.getEditText()?.suggestionMenu?.dismiss()
        val postHasMediaAttachment = if (apaiAttachment.get() is MeeraAddPostAttachmentItemViewNew) {
            (apaiAttachment.get() as MeeraAddPostAttachmentItemViewNew).isVisible && (pathVideo != null || pathImage != null)
        } else {
            (apaiAttachment.get() as AddPostAttachmentItemView).isVisible && (pathVideo != null || pathImage != null)
        }
        val hasMusic = media != null
        val postHasText = binding?.etWrite?.text?.toString()?.isNotEmpty() ?: false

        if (addPostViewModel.isEditPost()) {
            if (PostEditValidator.isEdited(
                    requireNotNull(addPostViewModel.getPost()),
                    createUploadPostBundle()
                )
            ) {
                showExitAlert()
            } else {
                cancelAction()
            }
        } else {
            if (postHasMediaAttachment || postHasText || hasMusic) {
                showPostCancellationDialog()
            } else {
                cancelAction()
            }
        }
    }

    private fun initAttachmentAdapter() {
        addPostViewModel.attachmentsMedia.observe(viewLifecycleOwner) { attachments ->
            submitItems(attachments)
            setRecyclerVisibility(attachments)
            newPostFormatter?.onPostChanged()
            if (attachments.isEmpty()) isOpenedRv = false
            checkUpload()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setRecyclerVisibility(attachments: List<UIAttachmentPostModel>) {
        if (attachments.isNotEmpty()) {
            if (apaiAttachment.get() is MeeraAddPostAttachmentItemViewNew) {
                (apaiAttachment.get() as MeeraAddPostAttachmentItemViewNew).visible()
            } else {
                (apaiAttachment.get() as AddPostAttachmentItemView).visible()
            }
        } else {
            attachmentsAdapter.notifyDataSetChanged()
            deleteAttachment()
            if (apaiAttachment.get() is MeeraAddPostAttachmentItemViewNew) {
                (apaiAttachment.get() as MeeraAddPostAttachmentItemViewNew).gone()
            } else {
                (apaiAttachment.get() as AddPostAttachmentItemView).gone()
            }
        }
    }

    private fun submitItems(attachments: List<UIAttachmentPostModel>) {
        if (attachments.isNotEmpty()) {
            setAttachmentItem(attachment = attachments[0])
            openKeyboardOnEventTitleInput()
            if (addPostViewModel.isEventPost()) {
                binding?.vgAddPostEventLabelLayoutInner?.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_event_label)
            }
        } else if (addPostViewModel.isEventPost()) {
            binding?.vgAddPostEventLabelLayoutInner?.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_bottomsheet_header)
        }
        mediaPicker?.updateAlreadySelectedMedia(addPostViewModel.getSelectedEditedMediaUri())
    }

    private fun setAttachmentItem(attachment: UIAttachmentPostModel) {
        calculateMediaPreviewHeight(needUpdate = false)
        if (apaiAttachment.get() is MeeraAddPostAttachmentItemViewNew) {
            (apaiAttachment.get() as MeeraAddPostAttachmentItemViewNew).apply {
                val isNeedMediaPositioning =
                    addPostViewModel.getFeatureTogglesContainer().postMediaPositioningFeatureToggle.isEnabled
                Timber.e("setAttachmentItem ")
                bind(
                    actions = addPostViewModel,
                    attachment = attachment,
                    mediaPreviewMaxWidth = getScreenWidth(),
                    mediaPreviewMaxHeight = mediaPreviewMaxHeight,
                    isNeedMediaPositioning = isNeedMediaPositioning,
                )
                post { if (isNeedShowHint() && isNeedMediaPositioning) showMediaPositioningHint() }
            }
        } else {
            (apaiAttachment.get() as AddPostAttachmentItemView).apply {
                val isNeedMediaPositioning =
                    addPostViewModel.getFeatureTogglesContainer().postMediaPositioningFeatureToggle.isEnabled
                bind(
                    actions = addPostViewModel,
                    attachment = attachment,
                    mediaPreviewMaxWidth = getScreenWidth(),
                    mediaPreviewMaxHeight = mediaPreviewMaxHeight,
                    isNeedMediaPositioning = isNeedMediaPositioning,
                )
                post { if (isNeedShowHint() && isNeedMediaPositioning) showMediaPositioningHint() }
            }
        }
    }

    private fun showMediaPositioningHint() {
        val isPostMediaPositioningHintWasShown = addPostViewModel.isMediaPositioningHintShownTimes()
        if (isPostMediaPositioningHintWasShown && !addPostViewModel.isEventPost()) {

            mediaPositioningTooltipJob = lifecycleScope.launchWhenResumed {
                addPostViewModel.incMediaPositioningHintShown()
                delay(DELAY_HINT_MS)

                if (apaiAttachment.get() is MeeraAddPostAttachmentItemViewNew) {
                    (apaiAttachment.get() as MeeraAddPostAttachmentItemViewNew).let {
                        val apaiAttachmentRect = Rect()
                        it.getLocalVisibleRect(apaiAttachmentRect)
                        val attachmentPaddingTop = apaiAttachment.get()?.paddingTop ?: 0
                        val mediaPositioningTooltipWidth = 155.dp
                        val mediaPositioningTooltipHeight = 62.dp
                        mediaPositioningTooltip?.showAtLocation(
                            apaiAttachment.get(),
                            Gravity.NO_GRAVITY,
                            (getScreenWidth() - mediaPositioningTooltipWidth) / 2,
                            (apaiAttachmentRect.bottom - apaiAttachmentRect.top) / 2
                                + getToolbarHeight() + attachmentPaddingTop + mediaPositioningTooltipHeight.dp / 4
                        )

                        delay(DURATION_HINT_MS)
                        mediaPositioningTooltip?.dismiss()
                    }
                } else {
                    (apaiAttachment.get() as AddPostAttachmentItemView).let {
                        val apaiAttachmentRect = Rect()
                        it.getLocalVisibleRect(apaiAttachmentRect)
                        val attachmentPaddingTop = apaiAttachment.get()?.paddingTop ?: 0
                        val mediaPositioningTooltipWidth = 155.dp
                        val mediaPositioningTooltipHeight = 62.dp
                        mediaPositioningTooltip?.showAtLocation(
                            apaiAttachment.get(),
                            Gravity.NO_GRAVITY,
                            (getScreenWidth() - mediaPositioningTooltipWidth) / 2,
                            (apaiAttachmentRect.bottom - apaiAttachmentRect.top) / 2
                                + getToolbarHeight() + attachmentPaddingTop + mediaPositioningTooltipHeight.dp / 4
                        )

                        delay(DURATION_HINT_MS)
                        mediaPositioningTooltip?.dismiss()
                    }
                }
            }
        }
    }

    private fun newSuggestionMenu() {
        val localBinding = binding ?: return
        val bottomSheetBehavior: BottomSheetBehavior<View> = BottomSheetBehavior.from(localBinding.tagsList.root)
        addBottomSheetSlideListener(bottomSheetBehavior)
        addKeyBoardListener(bottomSheetBehavior)
        val suggestionsMenu = SuggestedTagListMenu(
            fragment = this,
            editText = null,
            recyclerView = localBinding.tagsList.recyclerTags,
            bottomSheetBehavior = bottomSheetBehavior,
            chatRoomId = null
        )
        localBinding.etWrite.disableSuggestionMenu()
        localBinding.vPostTextBackground.getEditText().disableSuggestionMenu()
        localBinding.etWrite.suggestionMenu = suggestionsMenu
        localBinding.etAddPostTitle.suggestionMenu = suggestionsMenu
        localBinding.vPostTextBackground.getEditText().suggestionMenu = suggestionsMenu
        initScrolHeightWithClosedKeyBoard()
        initScrollHeightWithOpenedKeyBoard()
        binding?.tagsList?.let {
            val bottomSheetBehavior: BottomSheetBehavior<View> = BottomSheetBehavior.from(it.root)
            suggestedMenu = SuggestedTagListMenu(
                fragment = this,
                editText = binding?.etWrite!!,
                recyclerView = it.recyclerTags,
                bottomSheetBehavior = bottomSheetBehavior,
                chatRoomId = null
            )
            addBottomSheetSlideListener(bottomSheetBehavior)
            addKeyBoardListener(bottomSheetBehavior)
            binding?.etWrite?.disableSuggestionMenu()
            binding?.vPostTextBackground?.getEditText()?.disableSuggestionMenu()
            binding?.etWrite?.suggestionMenu = suggestedMenu
            binding?.etAddPostTitle?.suggestionMenu = suggestedMenu
            binding?.vPostTextBackground?.getEditText()?.suggestionMenu = suggestedMenu
            initScrolHeightWithClosedKeyBoard()
        }
    }

    private fun addBottomSheetSlideListener(bottomSheetBehavior: BottomSheetBehavior<View>) {
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                //empty
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val lp = CoordinatorLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                lp.height = bottomSheet.y.toInt()
                binding?.nsvAddPost?.layoutParams = lp
                if (binding?.etWrite?.isFocused == true) {
                    binding?.nsvAddPost?.fullScroll(View.FOCUS_DOWN)
                }
            }
        })
    }

    private fun addKeyBoardListener(bottomSheetBehavior: BottomSheetBehavior<View>) {
        var isKeyboardVisible = false
        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { v, insets ->
            val newIsKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (isKeyboardVisible == newIsKeyboardVisible) return@setOnApplyWindowInsetsListener insets
            isKeyboardVisible = newIsKeyboardVisible

            //клава открывается и список виден
            if (isKeyboardVisible && bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                setScrollHeightForOpenedKeyBoardAndTags(bottomSheetBehavior)
            }
            //клава закрывается список виден
            else if (!isKeyboardVisible && bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                setScrollHeightForClosedKeyBoardAndOpenedTags(bottomSheetBehavior)
            } else {
                binding?.nsvAddPost?.layoutParams =
                    CoordinatorLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            }
            return@setOnApplyWindowInsetsListener insets
        }
    }

    private fun initScrolHeightWithClosedKeyBoard() {
        binding?.nsvAddPost?.post {
            if (scrollViewHeightWithClosedKeyBoard == null)
                scrollViewHeightWithClosedKeyBoard = binding?.nsvAddPost?.height
        }
    }

    private fun initTextChangedListener() {
        binding?.etWrite?.addTextChangedListener {
            initScrollHeightWithOpenedKeyBoard()
            controlVisibilityTextBackground(it.toString())
        }
    }

    private fun initAddButtonsVisibility() {
        binding?.ivAddMusic?.isVisible = addPostViewModel.isEventPost().not()
        binding?.ivAddBackground?.isVisible = isTextBackgroundAvailable()

        if (addPostViewModel.isEditPost()) {
            binding?.ivCommentsSetting?.isEnabled = false
            binding?.ivCommentsSetting?.alpha = ALPHA_DISABLED
        } else {
            binding?.ivCommentsSetting?.isEnabled = true
            binding?.ivCommentsSetting?.alpha = ALPHA_FULL
        }
    }

    private fun controlVisibilityTextBackground(text: String) {
        binding?.apply {
            if (vPostTextBackground.isShowing()) return

            vPostTextBackground.isInputFullyVisible(
                text = text,
                isVisible = {
                    this@MeeraAddPostFragmentNew.isTextOnBackgroundFullyVisible = it
                    if (isTextOnBackgroundFullyVisible && isTextBackgroundActivated) {
                        val selection = etWrite.selectionEnd
                        changeVisibilityTextBackground(isVisible = true)
                        vPostTextBackground.showAsEditable(text = etWrite.text.toString(), selection = selection)
                        etWrite.gone()
                    }

                    ivAddBackground.isVisible = isTextBackgroundAvailable()
                }
            )
        }
    }

    private fun initScrollHeightWithOpenedKeyBoard() {
        if (scrollViewHeightWithOpenedKeyBoard == null)
            scrollViewHeightWithOpenedKeyBoard = binding?.nsvAddPost?.height
    }

    private fun setScrollHeightForClosedKeyBoardAndOpenedTags(bottomSheetBehavior: BottomSheetBehavior<View>) {
        val lp = CoordinatorLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        scrollViewHeightWithClosedKeyBoard?.let { height ->
            lp.height = height - bottomSheetBehavior.peekHeight
        }
        binding?.nsvAddPost?.layoutParams = lp
        doDelayed(DELAY_FOR_SCROLLING_VIEW_TO_BOTTOM) {
            if (binding?.etWrite?.isFocused == true) {
                binding?.nsvAddPost?.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    private fun setScrollHeightForOpenedKeyBoardAndTags(bottomSheetBehavior: BottomSheetBehavior<View>) {
        val lp = CoordinatorLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        scrollViewHeightWithOpenedKeyBoard?.let { height ->
            lp.height = height - bottomSheetBehavior.peekHeight
        }
        binding?.nsvAddPost?.layoutParams = lp
        binding?.nsvAddPost?.post {
            if (binding?.etWrite?.isFocused == true) {
                binding?.nsvAddPost?.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    private fun setupToolbar() {
        when {
            addPostViewModel.isEventPost() -> {
                binding?.llSelectRoadContainer?.isEnabled = false
                binding?.ivArrowMenu?.gone()
                binding?.tvTextRoadType?.setText(R.string.map_events_configuration_title)
                binding?.ivCloseAddPost?.setImageResource(R.drawable.ic_event_post_back)
                binding?.vAddPostAppbarDivider?.gone()
            }

            groupId == null || groupId == 0 -> {
                binding?.llSelectRoadContainer?.setOnClickListener {
                    if (!addPostViewModel.isEditPost()) {
                        openRoadSelectionMenu()
                    }
                }
                if (isPersonalPost) {
                    onMyRoadClicked()
                }
            }

            else -> {
                binding?.ivArrowMenu?.gone()
                binding?.tvTextRoadType?.setText(R.string.to_group_txt)
            }
        }

        if (addPostViewModel.isEditPost()) {
            binding?.llSelectRoadContainer?.isEnabled = false
            binding?.llSelectRoadContainer?.alpha = ALPHA_DISABLED
        } else {
            binding?.llSelectRoadContainer?.isEnabled = true
            binding?.llSelectRoadContainer?.alpha = ALPHA_FULL
        }
    }

    override fun onStopFragment() {
        super.onStopFragment()

        musicViewModel.handleUIAction(action = UserActionEvent.UnSubscribe)
    }

    private fun setupRvGallery() {
        checkMediaPermissions(
            object : PermissionDelegate.Listener {

                override fun onGranted() {
                    initMediaGallery()
                    addPostViewModel.handleAutoOpenMediaGallery()
                }

                override fun needOpenSettings() {
                    binding?.rvGalleryMedia?.gone()
                    isPreviewPermitted = false
                    addPostViewModel.handleAutoOpenMediaGallery()
                }

                override fun onDenied() {
                    binding?.rvGalleryMedia?.gone()
                    isPreviewPermitted = false
                    addPostViewModel.handleAutoOpenMediaGallery()
                }

                override fun onError(error: Throwable?) {
                    binding?.rvGalleryMedia?.gone()
                    isPreviewPermitted = false
                    addPostViewModel.handleAutoOpenMediaGallery()
                }
            }
        )
    }

    private fun setupKeyboardHeightProvider() {
        binding?.root?.let { root ->
            keyboardHeightProvider = KeyboardHeightProvider(root)
            keyboardHeightProvider?.observer = { height ->
                if (height > 0) {
                    addPostViewModel.saveKeyboardHeight(height)
                    keyboardHeightProvider?.release()
                }
            }
        }
    }

    private fun initMediaGallery() {
        isPreviewPermitted = true
        initRvMedia()
        binding?.rvGalleryMedia?.post {
            val itemsCountInRow = MEDIA_GALLERY_ITEM_COUNT_IN_ROW
            val itemsPadding = MEDIA_GALLERY_ITEM_PADDING.dp
            binding?.rvGalleryMedia?.layoutParams?.height = (getScreenWidth() / itemsCountInRow) - itemsPadding
        }

        addPostViewModel.requestLatestMedia()
    }

    private fun handleMediaByPicker(path: Uri, afterEdit: Boolean = false) {
        if (apaiAttachment.get() is MeeraAddPostAttachmentItemViewNew) {
            (apaiAttachment.get() as MeeraAddPostAttachmentItemViewNew).showLoading()
        } else {
            (apaiAttachment.get() as AddPostAttachmentItemView).showLoading()
        }
        addPostViewModel.checkSelectedEditedMediaUri(path, afterEdit)
    }

    private fun getMediaByPicker(path: Uri, afterEdit: Boolean) {
        mediaAttachmentSelected(path, afterEdit)
    }

    private fun initRvMedia() {
        binding?.rvGalleryMedia?.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        binding?.rvGalleryMedia?.adapter = mediaAdapter
        if (!addPostViewModel.isEditPost()) {
            binding?.rvGalleryMedia?.visible()
        } else {
            binding?.rvGalleryMedia?.gone()
        }
        isOpenedRv = true

        mediaAdapter.onClickListener = { item ->
            when (item.type) {
                TYPE_MEDIA, TYPE_VIDEO -> {
                    item.mediaPreview?.let { uri ->
                        addPostViewModel.setMediaWasCompressed(false)
                        mediaAttachmentSelected(uri)
                    }
                }

                TYPE_CAMERA -> {
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        activity?.getImageFromCamera(object : ImageCaptureUtils.Listener {
                            override fun onResult(result: ImageCaptureResultModel) {
                                addPostViewModel.setMediaWasCompressed(false)
                                mediaAttachmentSelected(result.fileUri)
                            }
                        })
                    } else {
                        onCameraRequestPermissions(false)
                    }
                }

                TYPE_GALLERY -> {
                    showMediaPicker { imageUri -> mediaAttachmentSelected(imageUri) }
                }
            }
        }
    }

    private fun handleMediaUri(it: List<Uri>) {
        val collection = mutableListOf<MediaListAdapter.MediaItemGallery>()

        collection.add(MediaListAdapter.MediaItemGallery(type = TYPE_CAMERA))

        it.forEach {
            val isVideo = fileManager.getMediaType(it) == MEDIA_TYPE_VIDEO
            val type = if (isVideo) TYPE_VIDEO else TYPE_MEDIA
            val mediaItemGallery = MediaListAdapter.MediaItemGallery(it, type)

            collection.add(mediaItemGallery)
        }

        collection.add(MediaListAdapter.MediaItemGallery(type = TYPE_GALLERY))
        mediaAdapter.resetAndAddData(collection)
    }


    private fun showMusicContainer() {
        binding?.flMusicContainer?.visible()
        binding?.flMusicContainer?.measure(MATCH_PARENT, WRAP_CONTENT)
        binding?.flMusicContainer?.measuredHeight?.let {
            binding?.flMusicContainer?.animateHeight(it, DEFAULT_ANIMATION_DURATION)
        }
    }

    private fun hideMusicContainer() {
        binding?.flMusicContainer?.animateHeight(0, DEFAULT_ANIMATION_DURATION) {
            musicViewModel.handleUIAction(action = UserActionEvent.UnSubscribe)
            binding?.flMusicContainer?.gone()
            newPostFormatter?.onPostChanged()
        }
    }

    private fun updateMusicContainerUI(data: MusicCellUIEntity) {
        media = data
        binding?.mpcMedia?.setMediaInformation(
            media?.mediaEntity?.albumUrl,
            media?.mediaEntity?.artist,
            media?.mediaEntity?.track
        )
        showMusicContainer()
        checkUpload()
    }

    override fun onStart() {
        super.onStart()
        setupTextChangedObservable()
    }

    private fun calculateMediaPreviewHeight(needUpdate: Boolean) {
        if (mediaPreviewMaxHeight != 0 && !needUpdate) return

        val contentContainerHeight: Int =
            getScreenHeight() - addPostViewModel.getKeyboardHeightForPicker() - getToolbarHeight()

        val addPostTitleHeight: Int = binding?.etAddPostTitle?.height ?: 0
        val addPostTitleFinalHeight: Int =
            if (addPostTitleHeight != 0) addPostTitleHeight + EVENT_TITLE_MARGIN_BOTTOM.dp else 0
        val eventViewsHeight: Int = addPostTitleFinalHeight + (binding?.elvAddPostEvenLabel?.height ?: 0).toInt()
        val textPadding: Int = binding?.etWrite?.paint?.descent()?.toInt() ?: 0
        val postInputMargins =
            (binding?.etWrite?.marginTop ?: 0).toInt() + (binding?.etWrite?.marginBottom ?: 0).toInt()
        val postInputHeight: Int =
            POST_INPUT_TEXT_SIZE.sp * POST_INPUT_MIN_LINES_VISIBLE + postInputMargins + textPadding
        val offset = POST_INPUT_BOTTOM_OFFSET.dp - MEDIA_PREVIEW_MARGIN_TOP.dp
        val contentViewsHeight = getActionsContainerHeight() + eventViewsHeight + postInputHeight + offset

        mediaPreviewMaxHeight = contentContainerHeight - contentViewsHeight
    }

    private fun openKeyboardOnPostTextInput(onCompleted: () -> Unit = {}) {
        Timber.d("openKeyboard")
        doDelayed(OPEN_KEYBOARD_DELAY) {
            binding?.etWrite?.let(::doOpenKeyboard)
            onCompleted.invoke()
        }
    }

    private fun openKeyboardOnEventTitleInput() {
        Timber.d("openKeyboard")
        doDelayed(OPEN_KEYBOARD_DELAY) {
            if (addPostViewModel.isEventPost()) {
                binding?.etAddPostTitle?.let(::doOpenKeyboard)
            }
        }
    }


    private fun doOpenKeyboard(inputView: EditText) {
        if (view != null) {
            inputView.requestFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(inputView, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
        keyboardHeightProvider?.release()
    }

    private fun setupStatusBar(view: View) {
        val statusBar = view.findViewById<View>(R.id.status_bar_post)
        val params = statusBar.layoutParams as AppBarLayout.LayoutParams
        params.height = context.getStatusBarHeight()
        statusBar.layoutParams = params
    }

    private fun showPostCancellationDialog() {
        currentDialog = ConfirmDialogBuilder()
            .setHeader(getString(R.string.delete_attachment_dialog_header))
            .setDescription(getString(R.string.delete_attachment_dialog_description))
            .setLeftBtnText(getString(R.string.delete_attachment_dialog_cancel_button_text))
            .setRightBtnText(getString(R.string.delete_attachment_dialog_confirm_button_text))
            .setCancelable(false)
            .setRightClickListener {
                unlockScreen()
                deleteAttachment()
                context?.hideKeyboard(requireView())
                mediaPicker?.dismissAllowingStateLoss()
                act.onBackPressed()
            }
            .show(childFragmentManager)
    }

    private fun showExitAlert() {
        currentDialog = ConfirmDialogBuilder()
            .setHeader(getString(R.string.post_edit_exit_title))
            .setDescription(getString(R.string.post_edit_exit_description))
            .setLeftBtnText(getString(R.string.general_cancel))
            .setRightBtnText(getString(R.string.general_exit))
            .setCancelable(false)
            .setRightClickListener {
                unlockScreen()
                deleteAttachment()
                context?.hideKeyboard(requireView())
                mediaPicker?.dismissAllowingStateLoss()
                act.onBackPressed()
            }
            .show(childFragmentManager)
    }

    private fun showEventModerationDialog() {
        currentDialog = MeeraConfirmDialogBuilder()
            .setHeader(getString(R.string.map_events_moderation_dialog_title))
            .setDescription(getString(R.string.map_events_moderation_dialog_message))
            .setBottomBtnText(getString(R.string.map_events_moderation_dialog_edit).uppercase())
            .setTopBtnText(getString(R.string.map_events_moderation_dialog_confirm).uppercase())
            .setCancelable(false)
            .setTopClickListener {
                addPostViewModel.publishCurrentUploadBundle()
            }
            .setBottomClickListener {
                resetSendButton()
            }
            .setDialogCancelledListener {
                resetSendButton()
            }
            .show(childFragmentManager)
    }

    private fun showPostMediaResetDialog(uri: Uri?, isAdding: Boolean, openCamera: Boolean) {
        currentDialog = ConfirmDialogBuilder()
            .setHeader(getString(R.string.post_reset_media_dialog_title))
            .setDescription(getString(R.string.post_reset_media_dialog_description))
            .setBottomBtnText(getString(R.string.post_reset_media_dialog_cancel).uppercase())
            .setMiddleBtnText(getString(R.string.post_reset_media_dialog_action).uppercase())
            .setCancelable(false)
            .setVertical(true)
            .setNeedTopButton(false)
            .setMiddleClickListener {
                addPostViewModel.actionAfterApproveResetAttachment(uri, isAdding, openCamera)
            }
            .show(childFragmentManager)
    }

    private fun showNotAvailableError(reason: NotAvailableReasonUiEntity) {
        when (reason) {
            NotAvailableReasonUiEntity.POST_NOT_FOUND -> showError(getString(R.string.post_edit_error_not_found_message))
            NotAvailableReasonUiEntity.USER_NOT_CREATOR -> showError(getString(R.string.post_edit_error_not_creator_message))
            NotAvailableReasonUiEntity.POST_DELETED -> showError(getString(R.string.post_edit_error_deleted_message))
            NotAvailableReasonUiEntity.EVENT_POST_UNABLE_TO_UPDATE,
            NotAvailableReasonUiEntity.UPDATE_TIME_IS_OVER -> {
                showAlert(
                    title = getString(R.string.post_edit_error_expired_title),
                    message = getString(R.string.post_edit_error_expired_description),
                    onOkClick = {
                        context?.hideKeyboard(requireView())
                        mediaPicker?.dismissAllowingStateLoss()
                        act.onBackPressed()
                    }
                )
            }
        }
    }

    private fun setupClickActions() {
        binding?.ivCloseAddPost?.setOnClickListener {
            (parentFragment as? EventConfigurator)?.apply {
                context?.hideKeyboard(requireView())
                it.postDelayed(EVENT_EDIT_NAVIGATION_DELAY_MS) {
                    onEditEvent()
                }
            } ?: cancelPostIfEmpty {
                context?.hideKeyboard(requireView())
                mediaPicker?.dismissAllowingStateLoss()
                act.onBackPressed()
            }
        }

        binding?.ivAddMusic?.setThrottledClickListener {
            disableViewForDelay(binding?.ivAddBackground, DISABLE_VIEW_DELAY)
            musicViewModel.logAddMusic()
            musicViewModel.handleUIAction(action = UserActionEvent.UnSubscribe)
            AddMusicBottomFragment.showAddMusicBottomFragment(childFragmentManager, media == null)
        }

        binding?.tvSend?.setThrottledClickListener {
            handleSendPost()
        }

        // Image attach
        binding?.ivAttach?.setThrottledClickListener {
            disableViewForDelay(binding?.ivAddBackground, DISABLE_VIEW_DELAY)
            binding?.etWrite?.clearFocus()
            musicViewModel.handleUIAction(action = UserActionEvent.UnSubscribe)
            showMediaPicker { imageUri -> handleMediaByPicker(imageUri) }
        }

        //open comment menu
        binding?.ivCommentsSetting?.setThrottledClickListener {
            if (!addPostViewModel.isEditPost()) {
                musicViewModel.handleUIAction(action = UserActionEvent.UnSubscribe)
                val where = when {
                    roadType == RoadSelectionEnum.MY -> AmplitudePropertyWhere.SELF_FEED
                    groupId != 0 -> AmplitudePropertyWhere.COMMUNITY
                    else -> AmplitudePropertyWhere.OTHER
                }
                amplitudeHelper.logPostShareSettingsTap(where)
                openCommentsMenu()
            }
        }

        initMediaContentKeyboard()
    }

    private fun initMediaContentKeyboard() {
        val etWrite = binding?.etWrite ?: return
        val receiver = InputReceiveContentListener(
            act.applicationContext,
            lifecycleScope,
            contentListener = { uri, mimeType ->
                addPostViewModel.keyBoardMediaReceived(uri, mimeType)
            }
        )
        ViewCompat.setOnReceiveContentListener(
            etWrite,
            InputReceiveContentListener.SUPPORTED_MIME_TYPES,
            receiver
        )
    }

    override fun onShowHints() {
        super.onShowHints()
        act?.hideHints()

        val isCommentSettingsButtonVisible = binding?.ivCommentsSetting?.isVisible ?: false
        if (isCommentSettingsButtonVisible) {

            val isCommentPolicyHintShown = addPostViewModel.isCommentPolicyHintShownTimes()
            if (isCommentPolicyHintShown) {

                commentingSettingsTooltipJob = lifecycleScope.launchWhenResumed {

                    delay(650)
                    binding?.ivCommentsSetting?.let { imageView ->
                        addPostViewModel.incCommentPolicyHintShown()

                        commentingSettingsTooltip?.showAboveView(
                            this@MeeraAddPostFragmentNew,
                            imageView
                        )

                    }

                    delay(4000)

                    commentingSettingsTooltip?.dismiss()
                }
            }
        }

        val isPostRoadSwitchHintShown = addPostViewModel.isPostRoadSwitchHintShownTimes()
        val isNotGroup = groupId == 0
        if (isPostRoadSwitchHintShown && isNotGroup && !addPostViewModel.isEventPost()) {
            postRoadSwitchTooltipJob = lifecycleScope.launchWhenResumed {
                delay(650)
                binding?.toolbarContentContainer?.let {
                    addPostViewModel.incPostRoadSwitchHintShown()

                    postRoadSwitchTooltip?.showBelowView(
                        fragment = this@MeeraAddPostFragmentNew,
                        view = it,
                        gravityModifier = Gravity.CENTER_HORIZONTAL
                    )
                }

                delay(4000)

                postRoadSwitchTooltip?.dismiss()
            }
        }

        val isPostMusicShareHintWasShown = addPostViewModel.isMusicHintShownTimes()
        if (isPostMusicShareHintWasShown && !addPostViewModel.isEventPost()) {
            shareMusicTooltipJob = lifecycleScope.launchWhenResumed {
                addPostViewModel.incMusicHintShown()
                delay(DELAY_HINT_MS)
                binding?.ivAddMusic?.let {
                    shareMusicTooltip?.showAboveView(
                        fragment = this@MeeraAddPostFragmentNew,
                        view = it
                    )
                }
                delay(DURATION_HINT_MS)
                shareMusicTooltip?.dismiss()
            }
        }
    }

    override fun onHideHints() {
        super.onHideHints()

        act?.hideHints()

        commentingSettingsTooltip?.dismiss()
        commentingSettingsTooltipJob?.cancel()

        postRoadSwitchTooltip?.dismiss()
        postRoadSwitchTooltipJob?.cancel()
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

    private fun updateMediaAdapter() {
        mediaAdapter.notifyDataSetChanged()
    }

    private fun showCameraSettingsDialog() {
        currentDialog = ConfirmDialogBuilder()
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

    private fun openCommentsMenu() {
        val menu = SurveyBottomMenu()
        menu.isEvent = addPostViewModel.isEventPost()
        menu.isRoad = groupId == 0
        menu.isCommunityCommentingOptionMode = groupId != 0
        menu.state = whoCanComment
        menu.allClickedListener = {
            whoCanComment = WhoCanCommentPostEnum.EVERYONE
            binding?.vCommentSettingIndicator?.gone()
            binding?.coverCommentSettingIndicator?.gone()
        }

        menu.noOneClickedListener = {
            whoCanComment = WhoCanCommentPostEnum.NOBODY
            binding?.vCommentSettingIndicator?.visible()
            binding?.coverCommentSettingIndicator?.visible()
        }

        menu.friendsClickedListener = {
            whoCanComment = if (menu.isCommunityCommentingOptionMode) COMMUNITY_MEMBERS else FRIENDS
            binding?.vCommentSettingIndicator?.visible()
            binding?.coverCommentSettingIndicator?.visible()
        }

        menu.show(parentFragmentManager)
    }

    private fun openRoadSelectionMenu() {
        if (this.userPermissions?.mainRoad?.canCreatePostInMainRoad == false) {
            showDialogNoPermissionToCreatePostInMainRoad()
            return
        }
        binding?.ivArrowMenu?.animate()?.rotation(90f)?.setDuration(200)?.start()

        val roadSelectBottomSheet = RoadSelectBottomSheet()
        roadSelectBottomSheet.roadState = this.roadType

        roadSelectBottomSheet.mainRoadClickedListener = {
            onMainRoad()
        }

        roadSelectBottomSheet.myRoadClickedListener = {
            onMyRoadClicked()
        }

        roadSelectBottomSheet.onDismiss = {
            binding?.ivArrowMenu?.animate()?.rotation(-90f)?.setDuration(200)?.start()
        }

        roadSelectBottomSheet.show(parentFragmentManager)
    }

    private fun showDialogNoPermissionToCreatePostInMainRoad() {
        val dialog = BlockUserByAdminDialogFragment()
        dialog.blockReason = userPermissions?.userBlockInfo?.blockReasonText
        dialog.blockDateUnixtimeSec = userPermissions?.userBlockInfo?.blockedUntil
        dialog.closeDialogClickListener = { dialog.dismiss() }
        dialog.writeSupportClickListener = {
            // addPostViewModel.onWriteToTechSupportClicked()   // Отключил "Написать в чат техподдержке"
            act?.navigateToTechSupport()
        }
        dialog.headerDialogType = HeaderDialogType.MainRoadType
        dialog.show(childFragmentManager, "blocked_user_dialog")
    }

    private fun onMyRoadClicked() {
        this.roadType = RoadSelectionEnum.MY
        binding?.tvTextRoadType?.setText(R.string.to_my_road)
    }

    private fun onMainRoad() {
        this.roadType = RoadSelectionEnum.MAIN
        binding?.tvTextRoadType?.setText(R.string.to_main_road)
    }

    private fun deleteAttachment() {
        deletePostImage()
        deleteVideo()
        mediaAttachmentUri = null
        checkUpload()
    }

    var workState: WorkInfo.State? = null


    private fun handleSendPost() {
        if (addPostViewModel.isAlreadyUploading()) {
            ConfirmDialogBuilder()
                .setHeader(resources.getString(R.string.post_publish_already_uploading_header))
                .setDescription(resources.getString(R.string.post_publish_already_uploading_text))
                .setLeftBtnText(resources.getString(R.string.post_publish_already_uploading_ok_button))
                .setLeftClickListener {
                    (activity as? ActivityToolsProvider)?.getStatusToastViewController()
                        ?.restoreStatusToast()
                }
                .setCancelable(true)
                .show(childFragmentManager)
        } else {
            if (canPublish()) {
                context?.hideKeyboard(requireView())
                val uploadModel = createUploadPostBundle().apply {
                    this.wasCompressed = isVideoWasCompressed
                }
                binding?.etWrite?.suggestionMenu?.dismiss()
                binding?.etAddPostTitle?.suggestionMenu?.dismiss()
                binding?.vPostTextBackground?.getEditText()?.suggestionMenu?.dismiss()
                binding?.tvSend?.gone()
                binding?.pbSendPost?.visible()
                addPostViewModel.addPostV2(uploadModel)
            } else {
                binding?.tvSend?.visible()
                binding?.pbSendPost?.gone()
            }
        }
    }

    private fun resetSendButton() {
        binding?.tvSend?.visible()
        binding?.pbSendPost?.gone()
    }

    private fun getOpenPlace(): MediaControllerOpenPlace {
        return if (addPostViewModel.isEventPost()) MediaControllerOpenPlace.EventPost else MediaControllerOpenPlace.Post
    }

    private fun mediaAttachmentSelected(uri: Uri, afterEdit: Boolean = false) {
        addPostViewModel.handleSelectedEditedMediaUri(afterEdit = afterEdit, uri)
        lifecycleScope.launch {
            when (act.getMediaControllerFeature().needEditMedia(uri = uri, openPlace = getOpenPlace())) {
                is MediaControllerNeedEditResponse.VideoTooLong, MediaControllerNeedEditResponse.NeedToCrop -> {
                    view?.hideKeyboard()
                    hideAndResetAttachmentView()
                    Timber.e("openEditor 1")
                    openEditor(uri = uri, automaticOpen = true)
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
                    actionAfterAttachmentSelected()
                }
            }
        }
    }

    private fun actionAfterAttachmentSelected() {
        if (!addPostViewModel.isEventPost()) {
            mediaPicker?.updateAlreadySelectedMedia(addPostViewModel.getSelectedEditedMediaUri())
        } else {
            mediaPicker?.dismissAllowingStateLoss()
        }
    }

    private fun hideAndResetAttachmentView() {
        apaiAttachment.get()?.gone()
        if (apaiAttachment.get() is MeeraAddPostAttachmentItemViewNew) {
            (apaiAttachment.get() as MeeraAddPostAttachmentItemViewNew).resetView()
        } else {
            (apaiAttachment.get() as AddPostAttachmentItemView).resetView()
        }
    }


    private val photoEditorCallback = object : MediaControllerCallback {
        override fun onPhotoReady(resultUri: Uri, nmrAmplitude: NMRPhotoAmplitude?) {
            handleMediaByPicker(path = resultUri, afterEdit = true)
            nmrAmplitude?.let {
                addPostViewModel.logPhotoEdits(openFrom, it)
            }
        }

        override fun onVideoReady(resultUri: Uri, nmrAmplitude: NMRVideoAmplitude?) {
            if (resultUri.toString().isEmpty()) {
                Timber.e("video_editor: returns uri : $resultUri")

                return
            }
            onVideoAdded()
            handleMediaByPicker(path = resultUri, afterEdit = true)
            nmrAmplitude?.let {
                addPostViewModel.logVideoEdits(openFrom, it)
            }
        }

        override fun onCanceled() {
            mediaPicker?.updateAlreadySelectedMedia(addPostViewModel.getSelectedEditedMediaUri())
        }

        override fun onError() {
            showError(getString(R.string.error_while_working_with_image))
        }
    }

    private fun openEditor(
        uri: Uri,
        showStickers: Boolean = false,
        automaticOpen: Boolean = false
    ) {
        addPostViewModel.isEditorAutomaticOpen = automaticOpen
        musicViewModel.handleUIAction(action = UserActionEvent.UnSubscribe)
        addPostViewModel.logEditorOpen(uri, openFrom, automaticOpen)
        act.getMediaControllerFeature().open(
            uri = uri,
            callback = photoEditorCallback,
            openPlace = getOpenPlace(),
            openStickers = showStickers
        )
    }

    private fun onVideoAdded() {
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
            addPostViewModel.onImageChosen(uri.toString())
            checkUpload()
            if (addPostViewModel.isEventPost()) {
                binding?.etAddPostTitle?.let(::doOpenKeyboard)
            }
            mediaAttachmentUri = uri
        }
    }

    private fun initVideoPreview(uri: Uri, videoUrl: String) {
        uri.path?.let { pathPhoto ->
            closeMediaPreview()
            deletePostImage()
            pathVideo = videoUrl
            addPostViewModel.onPreviewImageForVideo(uri.toString(), videoUrl)
            checkUpload()
            if (addPostViewModel.isEventPost()) {
                binding?.etAddPostTitle?.let(::doOpenKeyboard)
            }
        }
    }

    private fun removeImageAttachment() {
        addPostViewModel.clearSelectedEditedMediaUri()
        //TODO при редизайне взять изменения с AddMultipleMediaPostFragment
//        mediaPicker?.updateAlreadySelectedMedia(MediaViewerEditedAttachmentInfo.empty())
        deletePostImage()
        pathVideo = null
        pathImage = null
        addPostViewModel.onMediaRemoved()
        checkUpload()
        mediaAttachmentUri = null
    }

    private fun showVideoAttachment(uri: Uri) {
        uri.path?.let { path ->
            closeMediaPreview()
            deleteVideo()
            pathImage = null
            pathVideo = path
            addPostViewModel.onVideoChosen(path)
            checkUpload()
            mediaAttachmentUri = uri
            binding?.mediaAttachmentProgress?.gone()
        }
    }

    private fun setupTextChangedObservable() {
        binding?.etAddPostTitle?.let { headerInputText ->
            disposables.add(
                RxTextView.textChanges(headerInputText)
                    .debounce(150, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { checkUpload() },
                        { Timber.e(it) }
                    )
            )
        }
        binding?.etWrite?.let { inputText ->

            disposables.add(
                RxTextView.textChanges(inputText)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ _ ->
                        newPostFormatter?.onPostChanged()
                    }, { Timber.e(it) })
            )

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
                            } ?: run {
                                checkUpload()
                            }
                        }
                    }, { Timber.e(it) })
            )
        }
    }

    private fun checkUpload() {
        if (canPublish().not()) {
            binding?.tvSend?.isEnabled = false
            binding?.tvSend?.setTint(R.color.ui_gray)
            mediaGalleryOpenPreviewJob?.cancel()
            if (isTextBackgroundActivated.not()) {
                mediaGalleryOpenPreviewJob = doDelayed(DELAY_FOR_SHOW_MEDIA_LIST, ::openMediaPreview)
            }
        } else {
            mediaGalleryOpenPreviewJob?.cancel()
            mediaGalleryOpenPreviewJob = null
            binding?.tvSend?.isEnabled = true
            binding?.tvSend?.setTint(R.color.ui_purple)
            closeMediaPreview()
        }

        binding?.ivAddBackground?.isVisible = isTextBackgroundAvailable()

        if (isTextBackgroundActivated) isTextBackgroundActivated = !hasMedia()
    }

    private fun canPublish(): Boolean {
        val hasContent = pathImage != null
            || pathVideo != null
            || binding?.etWrite?.text?.trim()?.isNotEmpty() == true
            || media != null
        val hasTitle = binding?.etAddPostTitle?.text?.trim()?.isNotEmpty() == true
        val downloadingVideo = addPostViewModel.isPreviewAttachmentSat()
        return hasContent
            && (addPostViewModel.isEventPost().not() || hasTitle && addPostViewModel.isEventPost())
            && !downloadingVideo
    }

    private fun hasMedia(): Boolean {
        return pathImage != null || pathVideo != null || media != null
    }

    private fun isTextBackgroundAvailable(): Boolean {
        val hasContent = pathImage != null
            || pathVideo != null
            || media != null
        return hasContent.not() && isTextOnBackgroundFullyVisible
            && addPostViewModel.isEventPost().not()
            && addPostViewModel.getFeatureTogglesContainer().postsWithBackgroundFeatureToggle.isEnabled
    }

    private var isOpenedRv = false

    private fun openMediaPreview() {
        if (isOpenedRv && isPreviewPermitted || addPostViewModel.isEventPost() && pathImage != null) return
        Timber.tag("GalleryLine").d("trying to open gallery line")
        binding?.rvGalleryMedia?.visible()
        binding?.rvGalleryMedia?.measure(MATCH_PARENT, 64.dp)
        binding?.rvGalleryMedia?.measuredHeight?.let {
            binding?.rvGalleryMedia?.animateHeight(it, DEFAULT_ANIMATION_DURATION)
        }
        isOpenedRv = true
    }

    private fun closeMediaPreview() {
        if (!isOpenedRv) return
        binding?.rvGalleryMedia?.visible()
        binding?.rvGalleryMedia?.animateHeight(0, DEFAULT_ANIMATION_DURATION) {
            binding?.rvGalleryMedia?.gone()
        }
        isOpenedRv = false
    }

    private fun finishPublishing() {
        unlockScreen()
        binding?.tvSend?.isEnabled = true
        binding?.etWrite?.isEnabled = true
        binding?.etWrite?.text = null
        binding?.ivAttach?.isEnabled = true
        currentDialog?.dismiss()
        mediaPicker?.dismissAllowingStateLoss()
        binding?.etWrite?.suggestionMenu?.dismiss()
        binding?.etAddPostTitle?.suggestionMenu?.dismiss()
        binding?.vPostTextBackground?.getEditText()?.suggestionMenu?.dismiss()
        if (addPostViewModel.isEventPost()) {
            (parentFragment as? EventConfigurator)?.onEventPostPublished()
        } else {
            act.navigateBack(1)
        }
    }

    private fun deletePostImage() {
        addPostViewModel.deleteImageExceptGif(pathImage)
        pathImage = null
    }

    private fun deleteVideo() {
        addPostViewModel.deleteVideoIfNeeded(pathVideo)
        pathVideo = null
    }

    private fun unlockScreen() {
        isLockBackPressed = false
        //iv_close_add_post.isEnabled = true
    }

    private val orientationScreenListener =
        object : OrientationScreenListener() {
            override fun onOrientationChanged(orientation: Int) {
                orientationChangedListener.invoke(orientation)
            }
        }

    //Configuration need to configure mediaviewerView
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        orientationScreenListener.onOrientationChanged(newConfig.orientation)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPicker?.dismissAllowingStateLoss()
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
            //Далее собираем билдер
            MediaViewer.with(context)
                .setImageList(data)
                .startPosition(0)
                .setOrientationChangedListener(orientationScreenListener)
                .onSaveImage { saveImage(it, false) }
                .shareAvailable(false)
                .copyAvailable(false)
                .setAct(act)
                .onDismissListener(OnDismissListener {})
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

    private fun getHalfExpandedMediaPickerHeight(): Int = (getScreenHeight() * MEDIA_PICKER_HALF_EXTENDED_RATIO).toInt()

    private fun getCollapsedMediaPickerHeight(): Int =
        addPostViewModel.getKeyboardHeightForPicker() + getActionsContainerHeight()

    private fun getToolbarHeight(): Int = binding?.toolbarContentContainer?.height ?: 0

    private fun getActionsContainerHeight(): Int = binding?.llActionsContainer?.height ?: 0

    private fun showMediaPickerWithPermissionState(
        permissionState: PermissionState,
        onPickImageUri: (imageUri: Uri) -> Unit
    ) {
        mediaPicker?.dismissAllowingStateLoss()
        mediaPicker = loadSingleImageUri(
            activity = act,
            viewLifecycleOwner = viewLifecycleOwner,
            type = MediaControllerOpenPlace.Common,
            needWithVideo = addPostViewModel.isEventPost().not(),
            showGifs = addPostViewModel.isEventPost().not(),
            suggestionsMenu = SuggestionsMenu(act.getCurrentFragment(), SuggestionsMenuType.ROAD),
            permissionState = permissionState,
            tedBottomSheetPermissionActionsListener = this,
            previewModeParams = MediaViewerPreviewModeParams(
                isPreviewModeEnabled = true,
                halfExtendedHeight = getHalfExpandedMediaPickerHeight(),
                collapsedHeight = getCollapsedMediaPickerHeight()
            ),
            videoMaxDuration = addPostViewModel.getVideoMaxDuration(),
            selectedEditedMedia = addPostViewModel.getSelectedEditedMediaUri(),
            loadImagesCommonCallback = CoreTedBottomPickerActDependencyProvider(
                act = act,
                onReadyImageUri = { imageUri ->
                    addPostViewModel.setMediaWasCompressed(false)
                    onPickImageUri(imageUri)
                },
                onImageRemoved = { removeImageAttachment() },
                onRequestMediaReset = { addPostViewModel.showDialogResetMediaBeforeOpenCamera() },
                onDismissPicker = {
                    showMediaGallery = false
                    updateMediaAdapter()
                    if (addPostViewModel.isEventPost()) {
                        binding?.etAddPostTitle?.let(::doOpenKeyboard)
                        selectInputEnd(binding?.etAddPostTitle)
                    } else {
                        openKeyboardOnPostTextInput()
                        selectInputEnd(binding?.etWrite)
                    }
                }
            ),
            cameraLensFacing = CameraCharacteristics.LENS_FACING_BACK,
        )
    }

    private fun selectInputEnd(et: EditText?) {
        et?.setSelection(et.length())
    }

    private fun saveImage(imageUrl: String, toCache: Boolean) {
        saveImage(imageUrl, {}, toCache)
    }

    private fun saveImage(imageUrl: String, onSuccess: (Uri) -> Unit, toCache: Boolean) {
        saveImageOrVideoFile(
            imageUrl = imageUrl,
            act = act,
            viewLifecycleOwner = viewLifecycleOwner,
            successListener = onSuccess,
            toCache
        )
    }

    private fun showAlert(
        title: String,
        message: String,
        onOkClick: () -> Unit
    ) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(title)
            setMessage(message)
            positiveButton { onOkClick() }
        }.show()
    }

    private fun showError(message: String) {
        NToast.with(view)
            .typeError()
            .text(message)
            .show()
    }

    @Parcelize
    enum class OpenFrom(val amplitudePropertyWhere: AmplitudePropertyWhere) : Parcelable {
        Profile(AmplitudePropertyWhere.PROFILE),
        Community(AmplitudePropertyWhere.COMMUNITY),
        SelfRoad(AmplitudePropertyWhere.SELF_FEED),
        MainRoad(AmplitudePropertyWhere.MAIN_FEED),
        Map(AmplitudePropertyWhere.MAP);

        companion object {
            const val EXTRA_KEY = "OPEN_FROM"
        }
    }

    companion object {
        private const val KEY_MAP_EVENT = "KEY_MAP_EVENT"

        private const val EVENT_EDIT_NAVIGATION_DELAY_MS = 150L

        fun getInstance(
            event: EventParametersUiModel? = null,
            uploadPostBundle: String? = null,
            showMediaGallery: Boolean = true,
            openFrom: OpenFrom
        ): MeeraAddPostFragmentNew {
            return MeeraAddPostFragmentNew().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_MAP_EVENT, event)
                    putBoolean(ARG_SHOW_MEDIA_GALLERY, showMediaGallery)
                    putString(UPLOAD_BUNDLE_KEY, uploadPostBundle)
                    putParcelable(OpenFrom.EXTRA_KEY, openFrom)
                }
            }
        }
    }
}
