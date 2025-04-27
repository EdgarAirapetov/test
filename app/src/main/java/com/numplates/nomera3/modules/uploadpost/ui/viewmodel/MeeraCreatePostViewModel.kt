package com.numplates.nomera3.modules.uploadpost.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.analytics.add_post.AddPostAnalytics
import com.meera.core.extensions.getScreenHeight
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import com.meera.core.preferences.AppSettings.Companion.APP_HINT_COMMENT_POLICY_TIMES
import com.meera.core.preferences.AppSettings.Companion.APP_HINT_POST_ROAD_SWITCH_TIMES
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.FileUtilsImpl
import com.meera.core.utils.tedbottompicker.models.MediaUriModel
import com.meera.db.models.UploadType
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.noomeera.nmrmediatools.NMRVideoAmplitude
import com.numplates.nomera3.MEDIA_VIDEO
import com.numplates.nomera3.data.network.MediaPositioningDto
import com.numplates.nomera3.di.CACHE_DIR
import com.numplates.nomera3.domain.interactornew.CheckPostNowUploadingUseCase
import com.numplates.nomera3.domain.interactornew.DownloadFileUseCase
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
import com.numplates.nomera3.modules.calls.domain.CallSignal
import com.numplates.nomera3.modules.calls.domain.GetCallStatesUsecase
import com.numplates.nomera3.modules.calls.domain.GetCallStatusUsecase
import com.numplates.nomera3.modules.chat.domain.usecases.GetImageFileForKeyboardContentUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.SuccessGif
import com.numplates.nomera3.modules.chat.domain.usecases.SuccessImage
import com.numplates.nomera3.modules.chat.domain.usecases.UnknownFail
import com.numplates.nomera3.modules.uploadpost.ui.entity.NotAvailableReasonUiEntity.EVENT_POST_UNABLE_TO_UPDATE
import com.numplates.nomera3.modules.uploadpost.ui.entity.toUiEntity
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.domain.usecase.CheckPostPostParams
import com.numplates.nomera3.modules.feed.domain.usecase.CheckPostUpdateAvailability
import com.numplates.nomera3.modules.feed.domain.usecase.UpdatePostStateUseCase
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.MediaPositioning
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.toMediaPositioning
import com.numplates.nomera3.modules.maps.domain.usecase.NeedToShowModerationDialogUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.SetModerationDialogShownUseCase
import com.numplates.nomera3.modules.maps.ui.events.mapper.EventLabelUiMapper
import com.numplates.nomera3.modules.maps.ui.events.mapper.MapEventsUiMapperImpl
import com.numplates.nomera3.modules.maps.ui.events.model.EventLabelUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventParametersUiModel
import com.numplates.nomera3.modules.newroads.data.entities.EventEntity
import com.numplates.nomera3.modules.posts.domain.model.PostActionModel
import com.numplates.nomera3.modules.upload.data.post.UploadMediaModel
import com.numplates.nomera3.modules.upload.data.post.UploadPostBundle
import com.numplates.nomera3.modules.upload.domain.usecase.post.PostImageDeleteExceptGifUseCase
import com.numplates.nomera3.modules.upload.domain.usecase.post.PostVideoDeleteUseCase
import com.numplates.nomera3.modules.uploadpost.ui.AddMultipleMediaPostFragment
import com.numplates.nomera3.modules.uploadpost.ui.AttachmentMediaActions
import com.numplates.nomera3.modules.uploadpost.ui.PostEditValidator
import com.numplates.nomera3.modules.uploadpost.ui.data.AttachmentPostType
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentMediaModel
import com.numplates.nomera3.modules.uploadpost.ui.data.isSameMedia
import com.numplates.nomera3.modules.uploadpost.ui.data.parseTypeForAdapter
import com.numplates.nomera3.modules.uploadpost.ui.data.toMediaUriModel
import com.numplates.nomera3.modules.uploadpost.ui.mapper.UIAttachmentsMapper
import com.numplates.nomera3.modules.user.domain.usecase.UserPermissionParams
import com.numplates.nomera3.modules.user.domain.usecase.UserPermissionsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetLocalSettingsUseCase
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.utils.getTrueTextWithProfanity
import com.numplates.nomera3.presentation.utils.isEditorTempFile
import com.numplates.nomera3.presentation.view.ui.mediaViewer.ImageViewerData
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import com.numplates.nomera3.presentation.viewmodel.viewevents.AddPostViewEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.PostViewEvent
import dagger.Lazy
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.ArrayList
import javax.inject.Inject
import javax.inject.Named

private const val DEFAULT_KEYBOARD_HEIGHT_RATIO = 0.3
private const val MAX_SELECTED_MEDIA_COUNT = 10
typealias OpenFrom = AddMultipleMediaPostFragment.OpenFrom

class MeeraCreatePostViewModel @Inject constructor(
    private val appSettings: AppSettings,
    private val analyticsInteractor: AnalyticsInteractor,
    private val userPermissions: UserPermissionsUseCase,
    private val getKeyboardHeightUseCase: GetKeyboardHeightUseCase,
    private val setKeyboardHeightUseCase: SetKeyboardHeightUseCase,
    private val isKeyboardHeightSavedUseCase: IsKeyboardHeightSavedUseCase,
    private val uploadPostByTypeUseCase: UploadPostByTypeUseCase,
    private val downloadFileUseCase: DownloadFileUseCase,
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
    private val getRoadVideoMaxDurationUseCase: GetRoadVideoMaxDurationUseCase,
    private val needToShowModerationDialogUseCase: NeedToShowModerationDialogUseCase,
    private val setModerationDialogUseCase: SetModerationDialogShownUseCase,
    private val checkPostUpdateAvailability: CheckPostUpdateAvailability,
    private val featureTogglesContainer: FeatureTogglesContainer,
    private val getUserUidUseCase: GetUserUidUseCase,
    @Named(CACHE_DIR) private val cacheDir: File,
    private val addPostAnalytics: AddPostAnalytics,
    private val fileManager: FileManager,
    private val editorAnalytics: AmplitudeEditor,
    private val updatePostUseCase: UpdatePostStateUseCase,
    private val getCallStatesUsecase: GetCallStatesUsecase,
    private val getCallStatusUsecase: GetCallStatusUsecase,
    private val websocketChannel: Lazy<WebSocketMainChannel>
) : ViewModel(), AttachmentMediaActions, WebSocketMainChannel.WebSocketConnectionListener {

    private val _attachmentsMedia = MutableLiveData<List<UIAttachmentMediaModel>>()
    val attachmentsMedia: LiveData<List<UIAttachmentMediaModel>> = _attachmentsMedia

    val livePostViewEvents = MutableLiveData<PostViewEvent>()

    val streamEvent = BehaviorSubject.create<AddPostViewEvent>()

    val liveEventLabelUiModel = MutableLiveData<EventLabelUiModel>()

    var isEditorAutomaticOpen = false


    private val _showMediapickerDialog = MutableSharedFlow<Boolean>()
    val showMediaPicker = _showMediapickerDialog.asSharedFlow()

    private val _hideMediapickerDialog = MutableSharedFlow<Boolean>()
    val hideMediaPicker = _hideMediapickerDialog.asSharedFlow()

    private var mediaPickerDismissedWithCall: Boolean = false

    private var callSignalsJob: Job? = null
    private var callStatusJob: Job? = null
    private var uploadPostBundle: UploadPostBundle? = null
    private var eventEntity: EventEntity? = null
    private var isEventPost = false

    private var settings: Settings? = null

    private var initPost: PostUIEntity? = null

    private var videoMaxDuration: Int? = null

    private val _mediaWasCompressed = MutableStateFlow(false)

    private var mediaToEdit: UIAttachmentMediaModel? = null
    private var eventPostUriToEdit: Uri? = null

    init {
        settings = getAppInfoAsyncUseCase.executeBlocking()
        requestPostCreatePermissions()

        websocketChannel.get().addWebSocketConnectionListener(this)
        if (websocketChannel.get().isConnected()) {
            startObservingCallSignals()
        }
    }

    fun getEventEntity() = eventEntity

    fun isEventPost() = isEventPost

    fun getPost() = initPost

    fun isEditPost() = initPost != null

    fun getAmplitudeHelper(): AnalyticsInteractor {
        return analyticsInteractor
    }

    fun getPostBackgrounds(): List<PostBackgroundItemUiModel> {
        return getPostBackgroundsUseCase.invoke(settings)
    }

    fun getFeatureTogglesContainer(): FeatureTogglesContainer = featureTogglesContainer

    fun setMediaWasCompressed(wasCompressed: Boolean) {
        _mediaWasCompressed.value = wasCompressed
    }

    fun setPost(post: PostUIEntity?) {
        initPost = post
    }

    fun hasPreviewAttachments(): Boolean {
        return attachmentsMedia.value?.find { it.type == AttachmentPostType.ATTACHMENT_PREVIEW } != null
    }

    fun clearLastEvent() {
        streamEvent.onNext(AddPostViewEvent.Empty)
    }

    fun clearAttachmentCache() {
        val attachmentList = _attachmentsMedia.value ?: return
        attachmentList.forEach { media ->
            when (media.type) {
                AttachmentPostType.ATTACHMENT_PHOTO,
                AttachmentPostType.ATTACHMENT_GIF,
                AttachmentPostType.ATTACHMENT_PREVIEW -> {
                    deleteImageExceptGif(media.initialUri.path)
                    media.editedUri?.let {
                        deleteImageExceptGif(it.path)
                    }
                }

                AttachmentPostType.ATTACHMENT_VIDEO -> {
                    deleteVideoIfNeeded(media.initialUri.path)
                    media.editedUri?.let {
                        deleteVideoIfNeeded(it.path)
                    }
                }
            }
        }
    }

    fun hasMediaAttachments(): Boolean = !attachmentsMedia.value.isNullOrEmpty()

    fun parseAttachments(): List<UploadMediaModel> {
        return _attachmentsMedia.value?.map {
            val positioning = MediaPositioningDto(it.mediaPositioning.x, it.mediaPositioning.y)
            UploadMediaModel(
                mediaType = it.type,
                mediaUriPath = it.getActualResource(),
                dtoPositioning = positioning,
                mediaWasCompressed = it.isAttachmentCompressed(),
                initialStringUri = it.initialUri.toString(),
                editedStringUri = it.editedUri?.toString(),
                uploadMediaId = it.networkId
            )
        } ?: listOf()
    }

    fun parseAttachments(list: List<UIAttachmentMediaModel>): List<UploadMediaModel> {
        return list.map {
            val positioning = MediaPositioningDto(it.mediaPositioning.x, it.mediaPositioning.y)
            UploadMediaModel(
                mediaType = it.type,
                mediaUriPath = it.getActualResource(),
                dtoPositioning = positioning,
                mediaWasCompressed = it.isAttachmentCompressed(),
                initialStringUri = it.initialUri.toString(),
                editedStringUri = it.editedUri?.toString(),
                uploadMediaId = it.networkId
            )
        } ?: listOf()
    }

    fun handleMediaForRepeatPostCreation(list: List<UploadMediaModel>) {
        viewModelScope.launch {
            val newAttachmentsList = arrayListOf<UIAttachmentMediaModel>()
            list.forEach {
                mapUploadMediaModelToUiAttachment(it)?.also { uiModel ->
                    newAttachmentsList.add(uiModel)
                }
            }
            _attachmentsMedia.value = newAttachmentsList
        }
    }

    fun downloadImagesAndPreviewsFromAsset(assetsList: List<MediaAssetEntity>) {
        viewModelScope.launch {
            runCatching {
                val newAttachmentsList = arrayListOf<UIAttachmentMediaModel>()
                val videoToDownload = arrayListOf<MediaAssetEntity>()
                assetsList.forEach { asset ->
                    var isPreview = false
                    val downloadLink = when (asset.type) {
                        MEDIA_VIDEO -> {
                            videoToDownload.add(asset)
                            isPreview = true
                            asset.videoPreview.toString()
                        }

                        else -> asset.image.toString()
                    }

                    val cachedMediaUri = downloadFileUseCase.downloadFileAndSaveToCache(downloadLink) ?: return@launch
                    mapUiAttachment(
                        mediaModel = MediaUriModel.initial(cachedMediaUri),
                        isPreview = isPreview
                    )?.also {
                        newAttachmentsList.add(
                            it.copy(
                                mediaPositioning = asset.mediaPositioning ?: MediaPositioning(),
                                isCompressed = true,
                                networkId = asset.id
                            )
                        )
                    }
                }
                _attachmentsMedia.value = newAttachmentsList
                downloadVideosFromAssets(videoToDownload)
            }.onFailure(Timber::e)
        }
    }

    fun isAlreadyUploading(): Boolean {
        return checkPostNowUploadingUseCase.invoke()
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

    private fun getEditorWhereProperty(openFrom: MeeraOpenFrom?) =
        when (openFrom) {
            MeeraOpenFrom.SelfRoad,
            MeeraOpenFrom.MainRoad -> {
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
                uploadEditedPost(uploadBundle)
            }
        }
    }

    fun publishCurrentUploadBundle() {
        uploadPostBundle?.let(::addPostV2)
    }

    fun isPostRoadSwitchHintShownTimes() =
        appSettings.isPostRoadSwitchHintShownTimes < TooltipDuration.DEFAULT_TIMES
            && appSettings.isShownTooltipSession(APP_HINT_POST_ROAD_SWITCH_TIMES)

    fun incPostRoadSwitchHintShown() {
        val shownTimes = appSettings.isPostRoadSwitchHintShownTimes
        if (shownTimes > TooltipDuration.DEFAULT_TIMES) return
        appSettings.isPostRoadSwitchHintShownTimes = shownTimes + 1
        appSettings.markTooltipAsShownSession(APP_HINT_POST_ROAD_SWITCH_TIMES)
    }

    fun isCommentPolicyHintShownTimes() =
        appSettings.isCommentPolicyHintShownTimes < TooltipDuration.DEFAULT_TIMES
            && appSettings.isShownTooltipSession(APP_HINT_COMMENT_POLICY_TIMES)

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

    fun getSelectedEditedMediaUri() = _attachmentsMedia.value ?: listOf()

    fun setMediaList(mediaList: List<MediaUriModel>) {
        viewModelScope.launch {
            val newAttachmentsList = arrayListOf<UIAttachmentMediaModel>()
            mediaList.forEach { mediaModel ->
                attachmentsMedia.value?.find { it.isSameMedia(mediaModel) }?.let { uiAttachmentModel ->
                    newAttachmentsList.add(uiAttachmentModel.copy())
                } ?: kotlin.run {
                    val uiAttachment = mapUiAttachment(mediaModel)
                    uiAttachment?.let {
                        newAttachmentsList.add(it)
                    }
                }
            }
            _attachmentsMedia.value = newAttachmentsList
        }
    }

    fun confirmAndSetEventAttachmentMedia(eventPostImageUri: Uri) {
        viewModelScope.launch {
            val mediaModel = MediaUriModel.initial(eventPostImageUri)
            setMediaList(listOf(mediaModel))
            hideMediaPickerEvent()
        }
    }

    fun showDialogResetMediaBeforeOpenCamera() {
        val mediaForRemove = _attachmentsMedia.value?.firstOrNull() ?: return
        streamEvent.onNext(
            AddPostViewEvent.ToShowResetEditedMediaDialog(
                mediaModel = mediaForRemove,
                openCamera = true
            )
        )
    }

    fun confirmRemoveAttachment(mediaModel: UIAttachmentMediaModel, openCamera: Boolean = false) {
        viewModelScope.launch {
            val updatedAttachmentList = arrayListOf<UIAttachmentMediaModel>()
            _attachmentsMedia.value?.let { attachmentsList ->
                for (media in attachmentsList) {
                    if (media.initialUri != mediaModel.initialUri) {
                        updatedAttachmentList.add(media)
                    }
                }
            }
            _attachmentsMedia.value = updatedAttachmentList
            updateMediaChangesEvent()

            if (openCamera) {
                streamEvent.onNext(AddPostViewEvent.OpenCamera(true))
            }
        }
    }

    fun onTapPostBackgroundAnalytic() {
        val userId = getUserUidUseCase.invoke()
        addPostAnalytics.onTapPostBackground(userId)
    }

    fun handleEditedMedia(editResultUri: Uri) {
        mediaToEdit?.let { editedMedia ->
            viewModelScope.launch {
                val updatedAttachmentList = arrayListOf<UIAttachmentMediaModel>()
                _attachmentsMedia.value?.let { attachmentsList ->
                    for (media in attachmentsList) {
                        if (media.initialUri == editedMedia.initialUri) {
                            val mediaUri = MediaUriModel(
                                initialUri = media.initialUri,
                                editedUri = editResultUri
                            )
                            mapUiAttachment(mediaUri)?.also {
                                updatedAttachmentList.add(it.copy(mediaPositioning = media.mediaPositioning))
                            }
                        } else {
                            updatedAttachmentList.add(media)
                        }
                    }
                }
                _attachmentsMedia.value = updatedAttachmentList
                updateMediaChangesEvent()
            }
        }
    }

    fun handleEditedMediaForEventPost(editResultUri: Uri) {
        viewModelScope.launch {
            eventPostUriToEdit?.let { uriToEdit ->
                mapUiAttachment(uriToEdit, editResultUri)?.also {
                    _attachmentsMedia.value = listOf(it)
                }
            }
            clearMediaToEdit()
            hideMediaPickerEvent()
        }
    }

    fun setEventPostUriToEdit(uriToEdit: Uri) {
        eventPostUriToEdit = uriToEdit
    }

    fun clearMediaToEdit() {
        eventPostUriToEdit = null
        mediaToEdit = null
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
        if ((attachmentsMedia.value ?: listOf()).size == MAX_SELECTED_MEDIA_COUNT) {
            overCountMediaViewEvent()
        } else {
            processMediaFromKeyboard(media, label)
        }
    }

    override fun onItemClicked(uiMediaModel: UIAttachmentMediaModel) {
        if (hasPreviewAttachments()) return
        attachmentsMedia.value?.let { attachmentsList ->
            var position = 0
            val mediaList = mutableListOf<ImageViewerData>()
            attachmentsList.forEachIndexed { index, mediaModel ->
                mediaList.add(
                    ImageViewerData(
                        viewType = mediaModel.parseTypeForAdapter(),
                        imageUrl = mediaModel.getActualResource()
                    )
                )
                if (uiMediaModel.isSameMedia(mediaModel.toMediaUriModel())) {
                    position = index
                }
            }

            livePostViewEvents.value = PostViewEvent.OnOpenMedia(position, mediaList)
        }
    }

    override fun onItemEditClick(uiMediaPosition: Int) {
        val attachmentUiModel = attachmentsMedia.value?.get(uiMediaPosition) ?: return
        if (isEventPost) {
            eventPostUriToEdit = attachmentUiModel.initialUri
        } else {
            mediaToEdit = attachmentUiModel
        }
        when (attachmentUiModel.type) {
            AttachmentPostType.ATTACHMENT_PHOTO -> {
                livePostViewEvents.value = PostViewEvent.OnEditMediaImageByClick(attachmentUiModel)
            }

            AttachmentPostType.ATTACHMENT_VIDEO -> {
                livePostViewEvents.value = PostViewEvent.OnEditMediaVideoByClick(attachmentUiModel)
            }

            else -> throw RuntimeException("unsupported attachment post type for edit")
        }
    }

    override fun onAddStickerClick(uiMediaPosition: Int) {
        val attachmentUiModel = attachmentsMedia.value?.get(uiMediaPosition) ?: return

        if (isEventPost) {
            eventPostUriToEdit = attachmentUiModel.initialUri
        } else {
            mediaToEdit = attachmentUiModel
        }
        livePostViewEvents.value = PostViewEvent.OnAddStickerClick(attachmentUiModel.getActualResource())
    }

    override fun onItemCloseClick(uiMediaPosition: Int) {
        val model = attachmentsMedia.value?.get(uiMediaPosition) ?: return
        viewModelScope.launch {
            if (model.isEdited()) {
                streamEvent.onNext(
                    AddPostViewEvent.ToShowResetEditedMediaDialog(model)
                )
            } else {
                confirmRemoveAttachment(model)
            }
        }
    }

    override fun onItemPositionChange(uiMediaModel: UIAttachmentMediaModel, x: Double, y: Double) {
        val updatedAttachmentList = arrayListOf<UIAttachmentMediaModel>()
        _attachmentsMedia.value?.let { attachmentsList ->
            for (media in attachmentsList) {
                if (media.initialUri == uiMediaModel.initialUri) {
                    val mediaPositioning = MediaPositioning(x, y)
                    updatedAttachmentList.add(
                        media.copy(
                            mediaPositioning =
                                mediaPositioning
                        )
                    )
                } else {
                    updatedAttachmentList.add(media)
                }
            }
        }
        _attachmentsMedia.value = updatedAttachmentList
    }

    private fun uploadEditedPost(uploadBundle: UploadPostBundle) {
        viewModelScope.launch {
            if (isSamePostContent(initPost, uploadBundle)) {
                streamEvent.onNext(AddPostViewEvent.UploadStarting)
                return@launch
            }

            val postId = initPost?.postId ?: return@launch
            checkPostUpdateAvailability.execute(
                params = CheckPostPostParams(postId = postId),
                success = { response ->
                    if (response.isAvailable == EDIT_POST_AVAILABLE && response.notAvailableReason == null) {
                        uploadPostByTypeUseCase.invoke(UploadType.EditPost, uploadBundle)
                        streamEvent.onNext(AddPostViewEvent.UploadStarting)
                        sendUpdatePostStartEvent(uploadBundle)
                        logEditPost(uploadBundle)
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

    private fun sendUpdatePostStartEvent(uploadBundle: UploadPostBundle) {
        uploadBundle.postId ?: return
        val isPostContainsMediaContent = !uploadBundle.mediaList.isNullOrEmpty()

        val postEditingStart = PostActionModel.PostEditingStartModel(
            postId = uploadBundle.postId,
            postText = uploadBundle.text,
            isContainsMedia = isPostContainsMediaContent
        )
        viewModelScope.launch {
            updatePostUseCase.invoke(postEditingStart)
        }
    }

    private fun isSamePostContent(
        initPost: PostUIEntity?,
        postBundle: UploadPostBundle
    ): Boolean {
        val isSameMedia = isSameMediaAssets(initPost?.assets, postBundle.mediaList)
        return (
            initPost?.postId == postBundle.postId
                && (initPost?.tagSpan?.getTrueTextWithProfanity() ?: initPost?.postText) == postBundle.text
                && initPost?.backgroundId == postBundle.backgroundId
                && initPost?.backgroundUrl == postBundle.backgroundUrl
                && initPost?.media?.trackUrl == postBundle.media?.trackUrl
                && isSameMedia
            )
    }

    private fun isSameMediaAssets(assets: List<MediaAssetEntity>?, mediaList: List<UploadMediaModel>?): Boolean {
        if (assets.isNullOrEmpty() && mediaList.isNullOrEmpty()) return true
        if (assets.isNullOrEmpty() || mediaList.isNullOrEmpty()) return false
        if (assets.size != mediaList.size) return false

        for (i in mediaList.indices) {
            val asset = assets[i]
            val uploadMedia = mediaList[i]
            val uploadMediaPositioning = uploadMedia.dtoPositioning.toMediaPositioning()
            if (asset.mediaPositioning != uploadMediaPositioning) {
                return false
            }
            if (uploadMedia.uploadMediaId == null) {
                return false
            }
        }

        return true
    }

    private fun getKeyboardHeight(): Int = getKeyboardHeightUseCase.invoke()

    private suspend fun mapUploadMediaModelToUiAttachment(uploadModel: UploadMediaModel): UIAttachmentMediaModel? {
        var editedUri: Uri? = null
        uploadModel.editedStringUri?.let { editedUri = Uri.parse(it) }
        return mapUiAttachment(
            MediaUriModel(
                initialUri = Uri.parse(uploadModel.initialStringUri),
                editedUri = editedUri
            )
        )?.copy(
            networkId = uploadModel.uploadMediaId,
            mediaPositioning = uploadModel.dtoPositioning.toMediaPositioning(),
            isCompressed = true
        )
    }

    private suspend fun mapUiAttachment(initialUri: Uri, editedUri: Uri) =
        mapUiAttachment(MediaUriModel(initialUri = initialUri, editedUri = editedUri))

    private suspend fun mapUiAttachment(
        mediaModel: MediaUriModel,
        isPreview: Boolean = false
    ): UIAttachmentMediaModel? {
        when (fileManager.getMediaType(mediaModel.getActualUri())) {

            FileUtilsImpl.MEDIA_TYPE_VIDEO -> {
                return mapper.mapVideoMediaToAttachment(mediaModel)
            }

            FileUtilsImpl.MEDIA_TYPE_IMAGE, FileUtilsImpl.MEDIA_TYPE_IMAGE_GIF -> {
                return mapper.mapImageMediaToAttachment(mediaModel, isPreview)
            }
        }
        return null
    }

    private fun updateMediaChangesEvent() {
        val mediaUriList = (attachmentsMedia.value ?: listOf()).map { it.toMediaUriModel() }
        streamEvent.onNext(
            AddPostViewEvent.MediaPagerChanges(mediaUriList)
        )
    }

    private fun overCountMediaViewEvent() {
        streamEvent.onNext(AddPostViewEvent.ShowMaxCountReachedWarning)
    }

    private fun processMediaFromKeyboard(media: Uri, label: String) {
        viewModelScope.launch {
            when (val res = imageForKeyboardUseCase.invoke(media, label)) {
                is SuccessGif -> {
                    if (isEventPost().not()) {
                        mapper.mapImageMediaToAttachment(MediaUriModel(initialUri = Uri.parse(res.gifPath)))
                            ?.also { attachmentModel ->
                                _attachmentsMedia.value = (attachmentsMedia.value ?: listOf()) + attachmentModel
                            }
                    }
                }

                is SuccessImage -> {
                    mapper.mapImageMediaToAttachment(MediaUriModel(initialUri = Uri.parse(res.imagePath)))
                        ?.also { attachmentModel ->
                            _attachmentsMedia.value = (attachmentsMedia.value ?: listOf()) + attachmentModel
                        }
                }

                is UnknownFail -> Timber.d("Unknown error while processing image from keyboard")
                else -> Unit
            }
        }
    }

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

    private fun requestPostCreatePermissions() {
        viewModelScope.launch(Dispatchers.IO) {
            userPermissions.execute(
                UserPermissionParams(),
                success = { permissions -> livePostViewEvents.postValue(PostViewEvent.PermissionsReady(permissions)) },
                fail = { Timber.e(it) }
            )
        }
    }

    private suspend fun downloadVideosFromAssets(videoToDownload: ArrayList<MediaAssetEntity>) {
        kotlin.runCatching {
            videoToDownload.forEach { videoAsset ->
                videoAsset.video?.let { videoUrl ->
                    val cachedVideoUri = downloadFileUseCase.downloadFileAndSaveToCache(videoUrl) ?: return
                    setCachedVideoToMediaList(cachedVideoUri, videoAsset)
                }
            }
        }.onFailure(Timber::e)
    }

    private suspend fun setCachedVideoToMediaList(cachedVideoUri: Uri, videoAsset: MediaAssetEntity) {
        attachmentsMedia.value?.let { attachmentsList ->
            val videoPositionInMediaList = attachmentsList.indexOfFirst { it.networkId == videoAsset.id }
            if (videoPositionInMediaList < 0) return
            val updatedAttachmentList = attachmentsList.toMutableList()
            mapUiAttachment(MediaUriModel.initial(cachedVideoUri))?.also {
                updatedAttachmentList[videoPositionInMediaList] = it.copy(
                    mediaPositioning = videoAsset.mediaPositioning ?: MediaPositioning(),
                    isCompressed = true,
                    networkId = videoAsset.id
                )
            }
            _attachmentsMedia.value = updatedAttachmentList.toList()
        }
    }

    private fun deleteImageExceptGif(path: String?) {
        if (path.isEditorTempFile(cacheDir)) {
            viewModelScope.launch {
                deleteImageExceptGifUseCase.execute(path)
            }
        }
    }

    private fun deleteVideoIfNeeded(path: String?) {
        if (path.isEditorTempFile(cacheDir)) {
            viewModelScope.launch {
                deleteVideoUseCase.execute(path)
            }
        }
    }

    private fun hideMediaPickerEvent() {
        streamEvent.onNext(AddPostViewEvent.HideMediaPicker)
    }

    override fun connectionStatus(isConnected: Boolean) {
        if (isConnected) {
            startObservingCallSignals()
        }
    }

    private suspend fun handleCallSignal(callSignal: CallSignal) {
        when (callSignal) {
            CallSignal.INITIATE_CALL, CallSignal.ANSWER, CallSignal.OFFER, CallSignal.ACCEPT_CALL -> {
                mediaPickerDismissedWithCall = true
                _hideMediapickerDialog.emit(true)
            }

            else -> Unit
        }
    }

    private fun startObservingCallSignals() {
        callSignalsJob = getCallStatesUsecase.invoke()
            .flowOn(Dispatchers.IO)
            .onEach(::handleCallSignal)
            .launchIn(viewModelScope)

        callStatusJob = getCallStatusUsecase.invoke()
            .flowOn(Dispatchers.IO)
            .onEach {
                Timber.d("MAP SC status $it ${this.toString()}")
                if (mediaPickerDismissedWithCall) {
                    mediaPickerDismissedWithCall = false
                    _showMediapickerDialog.emit(true)
                }

            }
            .flowOn(Dispatchers.Main.immediate)
            .launchIn(viewModelScope)
    }

    fun clearCallJobs() {
        callSignalsJob?.cancel()
        callStatusJob?.cancel()
        websocketChannel.get().removeWebSocketConnectionListener(this)
    }

    override fun onCleared() {
        clearCallJobs()
        super.onCleared()

    }

    companion object {
        //TODO https://nomera.atlassian.net/browse/BR-24540
        private const val IS_EVENT_ROAD_PRIVACY_DIALOG_ENABLED = false
    }
}

