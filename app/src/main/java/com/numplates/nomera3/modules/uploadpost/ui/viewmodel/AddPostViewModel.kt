package com.numplates.nomera3.modules.uploadpost.ui.viewmodel

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.analytics.add_post.AddPostAnalytics
import com.meera.core.extensions.empty
import com.meera.core.extensions.getScreenHeight
import com.meera.core.preferences.AppSettings
import com.meera.core.preferences.AppSettings.Companion.APP_HINT_COMMENT_POLICY_TIMES
import com.meera.core.preferences.AppSettings.Companion.APP_HINT_POST_ROAD_SWITCH_TIMES
import com.meera.core.utils.tedbottompicker.models.MediaUriModel
import com.meera.core.utils.tedbottompicker.models.MediaViewerEditedAttachmentInfo
import com.meera.db.models.UploadType
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.noomeera.nmrmediatools.NMRVideoAmplitude
import com.numplates.nomera3.data.network.MediaPositioningDto
import com.numplates.nomera3.di.CACHE_DIR
import com.numplates.nomera3.domain.interactornew.CheckPostNowUploadingUseCase
import com.numplates.nomera3.domain.interactornew.GetKeyboardHeightUseCase
import com.numplates.nomera3.domain.interactornew.GetPostBackgroundsUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.IsKeyboardHeightSavedUseCase
import com.numplates.nomera3.domain.interactornew.SetKeyboardHeightUseCase
import com.numplates.nomera3.domain.interactornew.UploadPostByTypeUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetAppInfoAsyncUseCase
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetRoadVideoMaxDurationUseCase
import com.numplates.nomera3.modules.appInfo.ui.entity.PostBackgroundItemUiModel
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.editor.AmplitudeEditor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.editor.AmplitudeEditorParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.feed.AmplitudeFeedAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.feed.AmplitudeVideoAlertActionType
import com.numplates.nomera3.modules.chat.domain.usecases.GetImageFileForKeyboardContentUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.SuccessGif
import com.numplates.nomera3.modules.chat.domain.usecases.SuccessImage
import com.numplates.nomera3.modules.chat.domain.usecases.UnknownFail
import com.numplates.nomera3.modules.uploadpost.ui.entity.NotAvailableReasonUiEntity.EVENT_POST_UNABLE_TO_UPDATE
import com.numplates.nomera3.modules.uploadpost.ui.entity.toUiEntity
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.domain.usecase.CheckPostPostParams
import com.numplates.nomera3.modules.feed.domain.usecase.CheckPostUpdateAvailability
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.domain.usecase.NeedToShowModerationDialogUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.SetModerationDialogShownUseCase
import com.numplates.nomera3.modules.maps.ui.events.mapper.EventLabelUiMapper
import com.numplates.nomera3.modules.maps.ui.events.mapper.MapEventsUiMapperImpl
import com.numplates.nomera3.modules.maps.ui.events.model.EventLabelUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventParametersUiModel
import com.numplates.nomera3.modules.newroads.data.entities.EventEntity
import com.numplates.nomera3.modules.redesign.fragments.main.map.MeeraAddPostFragmentNew
import com.numplates.nomera3.modules.upload.data.post.UploadPostBundle
import com.numplates.nomera3.modules.upload.domain.usecase.post.GetVideoLengthUseCase
import com.numplates.nomera3.modules.upload.domain.usecase.post.PostImageDeleteExceptGifUseCase
import com.numplates.nomera3.modules.upload.domain.usecase.post.PostVideoDeleteUseCase
import com.numplates.nomera3.modules.uploadpost.ui.AddMultipleMediaPostFragment
import com.numplates.nomera3.modules.uploadpost.ui.AttachmentPostActions
import com.numplates.nomera3.modules.uploadpost.ui.PostEditValidator
import com.numplates.nomera3.modules.uploadpost.ui.data.AttachmentPostType
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentPostModel
import com.numplates.nomera3.modules.uploadpost.ui.mapper.UIAttachmentsMapper
import com.numplates.nomera3.modules.user.domain.usecase.UserPermissionParams
import com.numplates.nomera3.modules.user.domain.usecase.UserPermissionsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetLocalSettingsUseCase
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.utils.getTrueTextWithProfanity
import com.numplates.nomera3.presentation.utils.isEditorTempFile
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import com.numplates.nomera3.presentation.view.utils.mediaprovider.MediaProviderContract
import com.numplates.nomera3.presentation.viewmodel.viewevents.AddPostViewEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.PostViewEvent
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Named

private const val MAX_VIDEO_DURATION_LENGTH = 60L
private const val DEFAULT_KEYBOARD_HEIGHT_RATIO = 0.3
private const val FILE_PATH_DELIMITER = "/"
const val EDIT_POST_AVAILABLE = 1
typealias MeeraOpenFrom = MeeraAddPostFragmentNew.OpenFrom


class AddPostViewModel @Inject constructor(
    private val mediaProvider: MediaProviderContract,
    private val appSettings: AppSettings,
    private val analyticsInteractor: AnalyticsInteractor,
    private val userPermissions: UserPermissionsUseCase,
    private val getKeyboardHeightUseCase: GetKeyboardHeightUseCase,
    private val setKeyboardHeightUseCase: SetKeyboardHeightUseCase,
    private val isKeyboardHeightSavedUseCase: IsKeyboardHeightSavedUseCase,
    private val uploadPostByTypeUseCase: UploadPostByTypeUseCase,
    private val checkPostNowUploadingUseCase: CheckPostNowUploadingUseCase,
    private val deleteImageExceptGifUseCase: PostImageDeleteExceptGifUseCase,
    private val getPostBackgroundsUseCase: GetPostBackgroundsUseCase,
    private val getAppInfoAsyncUseCase: GetAppInfoAsyncUseCase,
    private val deleteVideoUseCase: PostVideoDeleteUseCase,
    private val mapper: UIAttachmentsMapper,
    private val eventMapper: MapEventsUiMapperImpl,
    private val eventLabelMapper: EventLabelUiMapper,
    private val getLocalSettingsUseCase: GetLocalSettingsUseCase,
    private val imageForKeyboardUseCase: GetImageFileForKeyboardContentUseCase,
    private val getVideoLengthUseCase: GetVideoLengthUseCase,
    private val getRoadVideoMaxDurationUseCase: GetRoadVideoMaxDurationUseCase,
    private val needToShowModerationDialogUseCase: NeedToShowModerationDialogUseCase,
    private val setModerationDialogUseCase: SetModerationDialogShownUseCase,
    private val checkPostUpdateAvailability: CheckPostUpdateAvailability,
    private val featureTogglesContainer: FeatureTogglesContainer,
    private val getUserUidUseCase: GetUserUidUseCase,
    @Named(CACHE_DIR) private val cacheDir: File,
    private val analyticsFeed: AmplitudeFeedAnalytics,
    private val addPostAnalytics: AddPostAnalytics,
    private val editorAnalytics: AmplitudeEditor
) : ViewModel(), AttachmentPostActions {

    val attachmentsMedia = MutableLiveData<List<UIAttachmentPostModel>>()

    val livePostViewEvents = MutableLiveData<PostViewEvent>()

    val liveGalleryMedia = MutableLiveData<List<Uri>>()

    val streamEvent = BehaviorSubject.create<AddPostViewEvent>()

    val liveEventLabelUiModel = MutableLiveData<EventLabelUiModel>()

    var isEditorAutomaticOpen = false

    private var uploadPostBundle: UploadPostBundle? = null
    private var eventEntity: EventEntity? = null
    private var isEventPost = false

    private var settings: Settings? = null

    private var initPost: PostUIEntity? = null

    private var videoMaxDuration: Int? = null

    private var selectedEditedAttachmentUri: MediaViewerEditedAttachmentInfo = MediaViewerEditedAttachmentInfo()

    private val _mediaWasCompressed = MutableStateFlow(false)

    init {
        settings = getAppInfoAsyncUseCase.executeBlocking()
        requestPostCreatePermissions()
    }

    fun getEventEntity() = eventEntity

    fun isEventPost() = isEventPost

    fun getPost() = initPost

    fun isEditPost() = initPost != null

    fun getAmplitudeHelper() : AnalyticsInteractor {
        return analyticsInteractor
    }

    fun getPostBackgrounds(): List<PostBackgroundItemUiModel> {
        return getPostBackgroundsUseCase.invoke(settings)
    }

    fun getFeatureTogglesContainer(): FeatureTogglesContainer = featureTogglesContainer

    fun getMediaPositioningForUpload(y: Double?): MediaPositioningDto {
        return MediaPositioningDto(y = y ?: 0.0)
    }

    fun setMediaWasCompressed(wasCompressed: Boolean) {
        _mediaWasCompressed.value = wasCompressed
    }

    fun setPost(post: PostUIEntity?) {
        initPost = post
    }

    private fun requestPostCreatePermissions() {
        viewModelScope.launch(Dispatchers.IO) {
            userPermissions.execute(
                UserPermissionParams(),
                success = { permissions ->
                    livePostViewEvents.postValue(
                        PostViewEvent.PermissionsReady(permissions)
                    )
                },
                fail = {
                    Timber.e(it)
                }
            )
        }
    }

    fun isPreviewAttachmentSat(): Boolean {
       return attachmentsMedia.value?.firstOrNull()?.type == AttachmentPostType.ATTACHMENT_PREVIEW
    }

    fun clearLastEvent() {
        streamEvent.onNext(AddPostViewEvent.Empty)
    }

    fun deleteImageExceptGif(path: String?) {
        if (path.isEditorTempFile(cacheDir)) {
            viewModelScope.launch {
                deleteImageExceptGifUseCase.execute(path)
            }
        }
    }

    fun deleteVideoIfNeeded(path: String?) {
        if (path.isEditorTempFile(cacheDir)) {
            viewModelScope.launch {
                deleteVideoUseCase.execute(path)
            }
        }
    }

    fun requestLatestMedia() {
        viewModelScope.launch {
            try {
                val recentMedia = if (isEventPost()) {
                    mediaProvider.getRecentImages(maxCount = RECENT_MEDIA_MAX_COUNT, includeGifs = false)
                } else {
                    mediaProvider.getRecentVideosImagesCombined(RECENT_MEDIA_MAX_COUNT)
                }
                liveGalleryMedia.postValue(recentMedia)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun isAlreadyUploading(): Boolean {
        return checkPostNowUploadingUseCase.invoke()
    }

    fun logEditorOpen(
        uri: Uri,
        openFrom: OpenFrom?,
        automaticOpen: Boolean = false
    ) = viewModelScope.launch {
        editorAnalytics.editorOpenAction(
            where = getEditorWhereProperty(openFrom),
            automaticOpen = automaticOpen,
            type = editorAnalytics.getEditorType(uri)
        )
    }

    fun logEditorOpen(
        uri: Uri,
        openFrom: MeeraOpenFrom?,
        automaticOpen: Boolean = false
    ) = viewModelScope.launch {
        editorAnalytics.editorOpenAction(
            where = getEditorWhereProperty(openFrom),
            automaticOpen = automaticOpen,
            type = editorAnalytics.getEditorType(uri)
        )
    }

    fun logPhotoEdits(
        openFrom: OpenFrom?,
        nmrAmplitude: NMRPhotoAmplitude,
    ) = viewModelScope.launch {
            editorAnalytics.photoEditorAction(
                editorParams = AmplitudeEditorParams(
                    where = getEditorWhereProperty(openFrom),
                    automaticOpen = isEditorAutomaticOpen
                ),
                nmrAmplitude = nmrAmplitude
            )
        }

    fun logPhotoEdits(
        openFrom: MeeraOpenFrom?,
        nmrAmplitude: NMRPhotoAmplitude,
    ) = viewModelScope.launch {
        editorAnalytics.photoEditorAction(
            editorParams = AmplitudeEditorParams(
                where = getEditorWhereProperty(openFrom),
                automaticOpen = isEditorAutomaticOpen
            ),
            nmrAmplitude = nmrAmplitude
        )
    }

    fun logVideoEdits(openFrom: OpenFrom?, nmrAmplitude: NMRVideoAmplitude) =
        viewModelScope.launch {
            editorAnalytics.videoEditorAction(
                editorParams = AmplitudeEditorParams(
                    where = getEditorWhereProperty(openFrom),
                    automaticOpen = isEditorAutomaticOpen
                ),
                nmrAmplitude = nmrAmplitude
            )
        }

    fun logVideoEdits(openFrom: MeeraOpenFrom?, nmrAmplitude: NMRVideoAmplitude) =
        viewModelScope.launch {
            editorAnalytics.videoEditorAction(
                editorParams = AmplitudeEditorParams(
                    where = getEditorWhereProperty(openFrom),
                    automaticOpen = isEditorAutomaticOpen
                ),
                nmrAmplitude = nmrAmplitude
            )
        }

    private fun getEditorWhereProperty(openFrom: OpenFrom?) =
        when (openFrom) {
            AddMultipleMediaPostFragment.OpenFrom.SelfRoad,
            AddMultipleMediaPostFragment.OpenFrom.MainRoad -> {
                AmplitudePropertyWhere.FEED
            }

            else -> openFrom?.amplitudePropertyWhere
        }

    private fun getEditorWhereProperty(openFrom: MeeraOpenFrom?) =
        when (openFrom) {
            MeeraAddPostFragmentNew.OpenFrom.SelfRoad,
            MeeraAddPostFragmentNew.OpenFrom.MainRoad -> {
                AmplitudePropertyWhere.FEED
            }

            else -> openFrom?.amplitudePropertyWhere
        }

    fun addPostV2(uploadBundle: UploadPostBundle) {
        uploadBundle.wasCompressed = _mediaWasCompressed.value
        if (isEventPost) {
            viewModelScope.launch {
                runCatching {
                    val privacySettings = getLocalSettingsUseCase.invoke()
                    val roadPrivacySetting = privacySettings
                        .firstOrNull { it.key == SettingsKeyEnum.SHOW_PERSONAL_ROAD.key }
                    val roadPrivacySettingValue = SettingsUserTypeEnum.values()
                        .firstOrNull { it.key == roadPrivacySetting?.value }
                        ?: SettingsUserTypeEnum.ALL
                    when {
                        IS_EVENT_ROAD_PRIVACY_DIALOG_ENABLED && roadPrivacySettingValue != SettingsUserTypeEnum.ALL -> {
                            uploadPostBundle = uploadBundle
                            streamEvent.onNext(AddPostViewEvent.NeedToShowRoadPrivacyDialog(roadPrivacySettingValue))
                        }
                        needToShowModerationDialogUseCase.invoke() -> {
                            uploadPostBundle = uploadBundle
                            streamEvent.onNext(AddPostViewEvent.NeedToShowModerationDialog)
                            setModerationDialogUseCase.invoke()
                        }
                        else -> {
                            uploadPostByTypeUseCase.invoke(UploadType.EventPost, uploadBundle)
                            streamEvent.onNext(AddPostViewEvent.UploadStarting)
                        }
                    }
                }.onFailure(Timber::e)
            }
        } else {
            if (initPost == null) {
                uploadPostByTypeUseCase.invoke(UploadType.Post, uploadBundle)
                streamEvent.onNext(AddPostViewEvent.UploadStarting)
            } else {
                uploadPost(uploadBundle)
            }
        }
    }

    private fun uploadPost(uploadBundle: UploadPostBundle) {
        viewModelScope.launch {
            if (isSamePostContent(initPost, uploadBundle)) {
                streamEvent.onNext(AddPostViewEvent.UploadStarting)
                return@launch
            }

            val bundleForUpload = if(isMediaContentChanged(initPost, uploadBundle)) {
                uploadBundle.copy(mediaChanged = true)
            } else {
                uploadBundle
            }

            val postId = initPost?.postId ?: return@launch
            checkPostUpdateAvailability.execute(
                params = CheckPostPostParams(postId = postId),
                success = { response ->
                    if (response.isAvailable == EDIT_POST_AVAILABLE && response.notAvailableReason == null) {
                        uploadPostByTypeUseCase.invoke(UploadType.EditPost, bundleForUpload)
                        streamEvent.onNext(AddPostViewEvent.UploadStarting)
                        logEditPost(bundleForUpload)
                    } else {
                        response.notAvailableReason?.let { reason ->
                            streamEvent.onNext(AddPostViewEvent.ShowAvailabilityError(reason.toUiEntity()))
                        } ?: streamEvent.onNext(AddPostViewEvent.ShowAvailabilityError(EVENT_POST_UNABLE_TO_UPDATE))
                    }
                },
                fail = { error ->
                    Timber.e(error)
                    streamEvent.onNext(AddPostViewEvent.ShowAvailabilityError(EVENT_POST_UNABLE_TO_UPDATE))
                }
            )
        }
    }

    private fun isSamePostContent(
        initPost: PostUIEntity?,
        postBundle: UploadPostBundle
    ): Boolean {
        val imageInitPost = getFileName(initPost?.getImageUrl())
        val imageUploadBundle = getFileName(postBundle.imagePath)
        val videoInitPost = getFileName(initPost?.getVideoUrl())
        val videoUploadBundle = getFileName(postBundle.videoPath)
        return (
            initPost?.postId == postBundle.postId
                && (initPost?.tagSpan?.getTrueTextWithProfanity() ?: initPost?.postText) == postBundle.text
                && initPost?.backgroundId == postBundle.backgroundId
                && initPost?.backgroundUrl == postBundle.backgroundUrl
                && imageInitPost == imageUploadBundle
                && videoInitPost == videoUploadBundle
                && initPost?.media?.trackUrl == postBundle.media?.trackUrl
            )
    }

    private fun isMediaContentChanged(
        initPost: PostUIEntity?,
        postBundle: UploadPostBundle
    ): Boolean {
        val imageInitPost = getFileName(initPost?.getImageUrl())
        val imageUploadBundle = getFileName(postBundle.imagePath)
        val videoInitPost = getFileName(initPost?.getVideoUrl())
        val videoUploadBundle = getFileName(postBundle.videoPath)
        return imageInitPost != imageUploadBundle
            || videoInitPost != videoUploadBundle
    }

    private fun getFileName(fullPath: String?): String =
        fullPath?.substringAfterLast(FILE_PATH_DELIMITER) ?: String.empty()

    fun publishCurrentUploadBundle() {
        uploadPostBundle?.let(::addPostV2)
    }

    fun isPostRoadSwitchHintShownTimes() =
        appSettings.isPostRoadSwitchHintShownTimes < TooltipDuration.DEFAULT_TIMES
            && appSettings.isShownTooltipSession(APP_HINT_POST_ROAD_SWITCH_TIMES)

    // увеличиваем на 1 кол-во показов до тех пор пока кол-во не достигнет 3х
    fun incPostRoadSwitchHintShown() {
        val shownTimes = appSettings.isPostRoadSwitchHintShownTimes
        if (shownTimes > TooltipDuration.DEFAULT_TIMES) return
        appSettings.isPostRoadSwitchHintShownTimes = shownTimes + 1
        appSettings.markTooltipAsShownSession(APP_HINT_POST_ROAD_SWITCH_TIMES)
    }

    fun isCommentPolicyHintShownTimes() =
        appSettings.isCommentPolicyHintShownTimes < TooltipDuration.DEFAULT_TIMES
            && appSettings.isShownTooltipSession(APP_HINT_COMMENT_POLICY_TIMES)

    // увеличиваем на 1 кол-во показов до тех пор пока кол-во не достигнет 3х
    fun incCommentPolicyHintShown() {
        val shownTimes = appSettings.isCommentPolicyHintShownTimes
        if (shownTimes > TooltipDuration.DEFAULT_TIMES) return
        appSettings.isCommentPolicyHintShownTimes = shownTimes + 1
        appSettings.markTooltipAsShownSession(APP_HINT_COMMENT_POLICY_TIMES)
    }

    fun isMusicHintShownTimes() =
        appSettings.isMusicHintShownTimes < TooltipDuration.DEFAULT_TIMES
            && appSettings.isShownTooltipSession(APP_HINT_COMMENT_POLICY_TIMES)

    fun incMusicHintShown() {
        val shownTimes = appSettings.isMusicHintShownTimes
        if (shownTimes > TooltipDuration.DEFAULT_TIMES) return
        appSettings.isMusicHintShownTimes = shownTimes + 1
        appSettings.markTooltipAsShownSession(APP_HINT_COMMENT_POLICY_TIMES)
    }

    fun isMediaPositioningHintShownTimes() =
        appSettings.isMediaPositioningShownTimes < TooltipDuration.MEDIA_POSITIONING_TIMES

    fun incMediaPositioningHintShown() {
        val shownTimes = appSettings.isMediaPositioningShownTimes
        if (shownTimes > TooltipDuration.MEDIA_POSITIONING_TIMES) return
        appSettings.isMediaPositioningShownTimes = shownTimes + 1
    }

    fun logVideoAlertAction(
        actionType: AmplitudeVideoAlertActionType,
        videoUri: Uri,
    ) {
        analyticsFeed.logVideoAlertAction(
            actionType = actionType,
            originalDuration = getVideoLengthUseCase.execute(videoUri),
            maxDuration = MAX_VIDEO_DURATION_LENGTH,
        )
    }

    private fun getKeyboardHeight(): Int = getKeyboardHeightUseCase.invoke()

    fun getKeyboardHeightForPicker(): Int {
        val isKeyboardHeightAlreadySaved = isKeyboardHeightSavedUseCase.invoke()
        return if (isKeyboardHeightAlreadySaved) {
            getKeyboardHeight()
        } else {
            (getScreenHeight() * DEFAULT_KEYBOARD_HEIGHT_RATIO).toInt()
        }
    }

    fun saveKeyboardHeight(height: Int) {
        val oldKeyboardHeight = getKeyboardHeightUseCase.invoke()
        if (oldKeyboardHeight == height) return

        setKeyboardHeightUseCase.invoke(height)
        streamEvent.onNext(AddPostViewEvent.KeyboardHeightChanged)
    }

    fun getVideoMaxDuration(): Int {
        videoMaxDuration?.let {
            return it
        } ?: run {
            val videoMaxDurationValue = getRoadVideoMaxDurationUseCase.invoke()
            videoMaxDuration = videoMaxDurationValue
            return videoMaxDurationValue
        }
    }

    fun getSelectedEditedMediaUri(): List<MediaUriModel> {
        val originalUri = selectedEditedAttachmentUri.original ?: return listOf()
        val editedUri = selectedEditedAttachmentUri.edited
        return listOf(MediaUriModel(initialUri = originalUri, editedUri = editedUri))
    }


    fun handleSelectedEditedMediaUri(afterEdit: Boolean, uri: Uri) {
        selectedEditedAttachmentUri = selectedEditedAttachmentUri.copy(
            original = if (afterEdit) selectedEditedAttachmentUri.original else uri,
            edited = if (afterEdit) uri else null
        )
    }

    fun checkSelectedEditedMediaUri(uri: Uri, afterEdit: Boolean) {
        setMediaWasCompressed(afterEdit)
        if (selectedEditedAttachmentUri.edited != null && !afterEdit) {
            streamEvent.onNext(
                AddPostViewEvent.NeedToShowResetEditedMediaDialog(
                    uri,
                    selectedEditedAttachmentUri.edited != uri,
                    false
                )
            )
        } else {
            streamEvent.onNext(AddPostViewEvent.SetAttachment(uri, afterEdit))
        }
    }

    fun showDialogResetMediaBeforeOpenCamera() {
        streamEvent.onNext(AddPostViewEvent.NeedToShowResetEditedMediaDialog(openCamera = true))
    }

    fun actionAfterApproveResetAttachment(uri: Uri?, isAdding: Boolean, openCamera: Boolean) {
        if (openCamera) {
            streamEvent.onNext(AddPostViewEvent.OpenCamera(true))
        } else {
            uri?.let {
                streamEvent.onNext(
                    if (isAdding)
                        AddPostViewEvent.SetAttachment(uri, false)
                    else
                        AddPostViewEvent.RemoveAttachment
                )
            }
        }
    }

    fun clearSelectedEditedMediaUri() {
        setMediaWasCompressed(false)
        selectedEditedAttachmentUri = selectedEditedAttachmentUri.copy(original = null, edited = null)
    }

    fun actionCameraCaptureFailed(afterResetMedia: Boolean) {
        if (afterResetMedia) removeMedia()
    }

    fun removeMedia() {
        clearSelectedEditedMediaUri()
        attachmentsMedia.value = listOf()
    }

    fun handleAutoOpenMediaGallery() {
        if (!isEventPost() && initPost == null) {
            streamEvent.onNext(AddPostViewEvent.ShowMediaPicker)
        }
    }

    fun onTapPostBackgroundAnalytic() {
        val userId = getUserUidUseCase.invoke()
        addPostAnalytics.onTapPostBackground(userId)
    }

    override fun onItemClicked(model: UIAttachmentPostModel) {
        when (model.type) {
            AttachmentPostType.ATTACHMENT_GIF,
            AttachmentPostType.ATTACHMENT_PHOTO -> {
                livePostViewEvents.value = PostViewEvent.OnOpenImage(model.attachmentResource)
            }
            AttachmentPostType.ATTACHMENT_VIDEO -> {
                livePostViewEvents.value = PostViewEvent.OnVideoPlay(model.attachmentResource)
            }
            else -> Unit
        }
    }

    override fun onItemEditClick(model: UIAttachmentPostModel) {
        when (model.type) {
            AttachmentPostType.ATTACHMENT_PHOTO -> {
                livePostViewEvents.value = PostViewEvent.OnEditImageByClick(model.attachmentResource)
            }
            AttachmentPostType.ATTACHMENT_VIDEO -> {
                livePostViewEvents.value = PostViewEvent.OnEditVideoByClick(model.attachmentResource)
            }
            else -> throw RuntimeException("unsupported attachment post type for edit")
        }
    }

    override fun onAddStickerClick(model: UIAttachmentPostModel) {
        livePostViewEvents.value = PostViewEvent.OnAddStickerClick(model.attachmentResource)
    }

    override fun onItemCloseClick(model: UIAttachmentPostModel) {
        viewModelScope.launch {
            if (selectedEditedAttachmentUri.edited == null) {
                clearSelectedEditedMediaUri()
                attachmentsMedia.value = attachmentsMedia.value?.toMutableList()?.apply {
                    remove(model)
                }
            } else {
                streamEvent.onNext(
                    AddPostViewEvent.NeedToShowResetEditedMediaDialog(
                        model.attachmentResource.toUri(),
                        false,
                        false
                    )
                )
            }
        }
    }

    fun onImageChosen(pathPhoto: String) {
        viewModelScope.launch {
            mapper.mapImageToAttachment(pathPhoto)?.also {
                attachmentsMedia.value = listOf(it)
            }
        }
    }

    fun onPreviewImageForVideo(pathPhoto: String, videoUrl: String) {
        viewModelScope.launch {
            mapper.mapImageToAttachment(pathPhoto, isPreviewImage = true)?.also {
                attachmentsMedia.value = listOf(it)
                streamEvent.onNext(AddPostViewEvent.UploadVideo(videoUrl = videoUrl))
            }
        }
    }

    fun onVideoChosen(pathVideo: String) {
        viewModelScope.launch {
            mapper.mapVideoToAttachment(pathVideo)?.also {
                attachmentsMedia.value = listOf(it)
            }
        }
    }

    fun onMediaRemoved() {
        viewModelScope.launch {
            attachmentsMedia.value = listOf()
        }
    }

    fun setEventEntity(eventEntity: EventEntity) {
        isEventPost = true
        this.eventEntity = eventEntity
        val eventLabelUiModel = eventLabelMapper.mapEventLabelUiModel(
            eventEntity = eventEntity,
            isVip = false
        )
        liveEventLabelUiModel.postValue(eventLabelUiModel)
    }

    fun setEventParameters(eventParametersUiModel: EventParametersUiModel) {
        isEventPost = true
        eventEntity = eventMapper.mapEventEntity(eventParametersUiModel)
        val eventLabelUiModel = eventLabelMapper.mapEventLabelUiModel(
            eventParametersUiModel = eventParametersUiModel,
            isVip = false
        )
        liveEventLabelUiModel.postValue(eventLabelUiModel)
    }

    fun keyBoardMediaReceived(media: Uri, label: String) {
        viewModelScope.launch {
            when (val res = imageForKeyboardUseCase.invoke(media, label)) {
                is SuccessGif -> {
                    if (isEventPost().not()) {
                        sendMediaAttachmentSelectedEvent(Uri.parse(res.gifPath))
                    }
                }
                is SuccessImage -> sendMediaAttachmentSelectedEvent(Uri.parse(res.imagePath))
                is UnknownFail -> Timber.d("Unknown error while processing image from keyboard")
                else -> Unit
            }
        }
    }

    private fun sendMediaAttachmentSelectedEvent(uri: Uri) =
        livePostViewEvents.postValue(PostViewEvent.MediaAttachmentSelected(uri))

    private fun logEditPost(postBundle: UploadPostBundle) {
        analyticsInteractor.logPostEdited(
            postId = postBundle.postId ?: return,
            authorId = appSettings.readUID(),
            where = mapRoadTypeToFeedName(postBundle.roadType, postBundle.groupId),
            textChange = PostEditValidator.isTextEdited(requireNotNull(initPost), postBundle),
            picChange = PostEditValidator.isImageEdited(requireNotNull(initPost), postBundle),
            videoChange = PostEditValidator.isVideoEdited(requireNotNull(initPost), postBundle),
            musicChange = PostEditValidator.isMediaEdited(requireNotNull(initPost), postBundle),
            backgroundChange = PostEditValidator.isBackgroundEdited(requireNotNull(initPost), postBundle),
        )
    }

    private fun mapRoadTypeToFeedName(roadType: Int?, groupId: Int?): AmplitudePropertyWhere {
        return if (groupId == 0) {
            if (roadType == 0) {
                AmplitudePropertyWhere.MAIN_FEED
            } else {
                AmplitudePropertyWhere.SELF_FEED
            }
        } else {
            AmplitudePropertyWhere.COMMUNITY_FEED
        }
    }

    companion object {
        private const val RECENT_MEDIA_MAX_COUNT = 20

        //TODO https://nomera.atlassian.net/browse/BR-24540
        private const val IS_EVENT_ROAD_PRIVACY_DIALOG_ENABLED = false
    }
}
