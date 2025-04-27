package com.numplates.nomera3.modules.complains.ui.details

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.meera.application_api.media.MediaFileMetaDataDelegate
import com.meera.application_api.media.domain.GetCropInfoUseCase
import com.meera.core.extensions.getScreenHeight
import com.meera.core.utils.files.FileUtilsImpl
import com.meera.core.utils.tedbottompicker.models.MediaUriModel
import com.meera.core.utils.tedbottompicker.models.MediaViewerEditedAttachmentInfo
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.noomeera.nmrmediatools.NMRVideoAmplitude
import com.numplates.nomera3.App
import com.numplates.nomera3.domain.interactornew.GetKeyboardHeightUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.IsKeyboardHeightSavedUseCase
import com.numplates.nomera3.domain.interactornew.SetKeyboardHeightUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.complaints.AmplitudeComplaints
import com.numplates.nomera3.modules.baseCore.helper.amplitude.editor.AmplitudeEditor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.feed.AmplitudeFeedAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.feed.AmplitudeVideoAlertActionType
import com.numplates.nomera3.modules.complains.domain.usecase.ComplainOnChatUseCase
import com.numplates.nomera3.modules.complains.domain.usecase.ComplainOnMomentUseCase
import com.numplates.nomera3.modules.complains.domain.usecase.ComplainOnUserUseCase
import com.numplates.nomera3.modules.complains.domain.worker.UploadComplaintMediaWorker
import com.numplates.nomera3.modules.complains.ui.details.UserComplaintDetailsEvent.FinishComplaintFlow
import com.numplates.nomera3.modules.complains.ui.reason.ComplainType
import com.numplates.nomera3.modules.upload.domain.usecase.post.CompressImageForUploadUseCase
import com.numplates.nomera3.modules.upload.domain.usecase.post.CompressVideoUseCase
import com.numplates.nomera3.modules.upload.domain.usecase.post.GetVideoLengthUseCase
import com.numplates.nomera3.modules.upload.domain.usecase.post.PostImageDeleteExceptGifUseCase
import com.numplates.nomera3.modules.upload.domain.usecase.post.PostVideoDeleteUseCase
import com.numplates.nomera3.modules.uploadpost.ui.AttachmentPostActions
import com.numplates.nomera3.modules.uploadpost.ui.data.AttachmentPostType
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentPostModel
import com.numplates.nomera3.modules.uploadpost.ui.mapper.UIAttachmentsMapper
import com.numplates.nomera3.modules.user.ui.entity.ComplainReasonId
import com.numplates.nomera3.presentation.view.utils.mediaprovider.MediaProviderContract
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val MAX_VIDEO_DURATION_LENGTH = 60L
private const val DEFAULT_COMPLAIN_ID = -1
private const val DEFAULT_KEYBOARD_HEIGHT_RATIO = 0.3

class UserComplaintDetailsViewModel @Inject constructor(
    private val complaintOnUserUseCase: ComplainOnUserUseCase,
    private val complainOnChatUseCase: ComplainOnChatUseCase,
    private val complainOnMomentUseCase: ComplainOnMomentUseCase,
    private val isKeyboardHeightSavedUseCase: IsKeyboardHeightSavedUseCase,
    private val getKeyboardHeightUseCase: GetKeyboardHeightUseCase,
    private val setKeyboardHeightUseCase: SetKeyboardHeightUseCase,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val getVideoLengthUseCase: GetVideoLengthUseCase,
    private val application: App,
    private val deleteImageExceptGifUseCase: PostImageDeleteExceptGifUseCase,
    private val deleteVideoUseCase: PostVideoDeleteUseCase,
    private val getCropInfoUseCase: GetCropInfoUseCase,
    private val compressImageForUploadUseCase: CompressImageForUploadUseCase,
    private val compressVideoUseCase: CompressVideoUseCase,
    private val metaDataDelegate: MediaFileMetaDataDelegate,
    private val mediaProvider: MediaProviderContract,
    private val mapper: UIAttachmentsMapper,
    private val analyticsComplaints: AmplitudeComplaints,
    private val analyticsFeed: AmplitudeFeedAnalytics,
    private val amplitudeEditor: AmplitudeEditor
) : ViewModel(), AttachmentPostActions {

    private val _complaintEvents = MutableSharedFlow<UserComplaintDetailsEvent>()
    val complaintEvents = _complaintEvents.asSharedFlow()

    private val _liveGalleryMedia = MutableLiveData<List<Uri>>()
    val liveGalleryMedia = _liveGalleryMedia.distinctUntilChanged()

    private val _attachmentsMedia = MutableLiveData<List<UIAttachmentPostModel>>()
    val attachmentsMedia = _attachmentsMedia.distinctUntilChanged()

    private val _screenState = MutableLiveData(UserComplaintScreenState())
    val screenState = _screenState.distinctUntilChanged()

    val openPlace = MediaControllerOpenPlace.Common

    private var selectedEditedAttachmentUri: MediaViewerEditedAttachmentInfo = MediaViewerEditedAttachmentInfo()

    override fun onItemClicked(model: UIAttachmentPostModel) {
        viewModelScope.launch {
            val command = when (model.type) {
                AttachmentPostType.ATTACHMENT_GIF,
                AttachmentPostType.ATTACHMENT_PHOTO -> {
                    UserComplaintDetailsEvent.OnOpenImage(model.attachmentResource)
                }
                AttachmentPostType.ATTACHMENT_VIDEO -> {
                    UserComplaintDetailsEvent.OnVideoPlay(model.attachmentResource)
                }
                else -> return@launch
            }
            _complaintEvents.emit(command)
        }
    }

    override fun onItemEditClick(model: UIAttachmentPostModel) {
        viewModelScope.launch {
            val command = when (model.type) {
                AttachmentPostType.ATTACHMENT_PHOTO -> {
                    UserComplaintDetailsEvent.OnEditImage(model.attachmentResource)
                }
                AttachmentPostType.ATTACHMENT_VIDEO -> {
                    UserComplaintDetailsEvent.OnEditVideo(model.attachmentResource)
                }
                else -> throw RuntimeException("unsupported attachment post type for edit")
            }
            _complaintEvents.emit(command)
        }
    }

    override fun onItemCloseClick(model: UIAttachmentPostModel) {
        viewModelScope.launch {
            _attachmentsMedia.value = _attachmentsMedia.value?.toMutableList()?.apply {
                remove(model)
            }
        }
    }

    fun deleteImageExceptGif(path: String?) {
        viewModelScope.launch {
            deleteImageExceptGifUseCase.execute(path)
        }
    }

    fun deleteVideo(path: String?) {
        viewModelScope.launch {
            deleteVideoUseCase.execute(path)
        }
    }

    fun onImageChosen(pathPhoto: String) {
        viewModelScope.launch {
            mapper.mapImageToAttachment(pathPhoto)?.also {
                _attachmentsMedia.value = listOf(it)
            }
        }
    }

    fun onVideoChosen(pathVideo: String) {
        viewModelScope.launch {
            mapper.mapVideoToAttachment(pathVideo)?.also {
                _attachmentsMedia.value = listOf(it)
            }
        }
    }

    fun requestLatestMedia() {
        viewModelScope.launch {
            try {
                val recentVideosImagesCombined = mediaProvider.getRecentVideosImagesCombined(20)
                _liveGalleryMedia.postValue(recentVideosImagesCombined)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun logEditorOpen(uri: Uri) = viewModelScope.launch {
        amplitudeEditor.editorOpenAction(
            type = amplitudeEditor.getEditorType(uri)
        )
    }

    fun logPhotoEdits(
        nmrAmplitude: NMRPhotoAmplitude
    ) = viewModelScope.launch {
        amplitudeEditor.photoEditorAction(nmrAmplitude)
    }

    fun logVideoEdits(
        nmrAmplitude: NMRVideoAmplitude
    ) = viewModelScope.launch {
        amplitudeEditor.videoEditorAction(nmrAmplitude)
    }

    fun sendComplaint(
        userId: Long,
        reason: ComplainReasonId,
        detailsParams: UserComplaintDetailsParams,
        where: AmplitudePropertyWhere,
        momentId: Long?,
        roomId: Long?,
        complainType: Int
    ) = viewModelScope.launch {
        _screenState.postValue(_screenState.value?.copy(isLockedSendButton = true))
        runCatching {
            val complaintId = when(complainType) {
                ComplainType.USER.key -> complaintOnUserUseCase.invoke(
                    userId = userId.toInt(),
                    reasonId = reason.key,
                    withFile = detailsParams.hasMedia(),
                    comment = detailsParams.comment,
                    momentId = momentId,
                    roomId = roomId
                )
                ComplainType.CHAT.key -> complainOnChatUseCase.invoke(
                    roomId = roomId,
                    reasonId = reason.key,
                    withFile = detailsParams.hasMedia(),
                    comment = detailsParams.comment
                )
                ComplainType.MOMENT.key -> sendMomentComplain(momentId)
                else -> DEFAULT_COMPLAIN_ID
            }
            Timber.d("Complaint ID: ${complaintId};")
            if (complaintId > 0) {
                if (detailsParams.hasMedia()) {
                    val imagePath = detailsParams.imagePath?.let {
                        compressImage(it)
                    }
                    val videoPath = detailsParams.videoPath?.let {
                        compressVideo(it)
                    }
                    sendAttachmentAsynchronous(
                        complaintId = complaintId,
                        imagePath = imagePath,
                        videoPath = videoPath,
                    )
                }
                analyticsComplaints.profileReportFinish(
                    reason = reason,
                    idFrom = getUserUidUseCase.invoke(),
                    idTo = userId,
                    haveText = detailsParams.hasText(),
                    charCount = detailsParams.charCount(),
                    haveMedia = detailsParams.hasMedia(),
                    videoCount = detailsParams.videoCount(),
                    imgCount = detailsParams.imageCount(),
                    where = where,
                )
            }
            _screenState.postValue(_screenState.value?.copy(isLockedSendButton = false))
            _complaintEvents.emit(FinishComplaintFlow(isSuccess = true))
        }.onFailure { error ->
            Timber.e(error)
            _screenState.postValue(_screenState.value?.copy(isLockedSendButton = false))
            _complaintEvents.emit(FinishComplaintFlow(isSuccess = false))
        }
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

    fun getSelectedEditedMediaUri(): List<MediaUriModel> {
        selectedEditedAttachmentUri.apply {
            original?.let {
                val model = MediaUriModel(
                    initialUri = it,
                    editedUri = edited
                )
                return listOf(model)
            }
        }
        return listOf()
    }

    fun handleSelectedEditedMediaUri(afterEdit: Boolean, uri: Uri) {
        selectedEditedAttachmentUri = selectedEditedAttachmentUri.copy(
            original = if (afterEdit) selectedEditedAttachmentUri.original else uri,
            edited = if (afterEdit) uri else null
        )
    }

    private suspend fun sendMomentComplain(momentId: Long?): Int {
        if (momentId == null) error("При жалобе на момент должен быть указан momentId")

        complainOnMomentUseCase.invoke(momentId = momentId)

        return DEFAULT_COMPLAIN_ID
    }

    private fun sendAttachmentAsynchronous(complaintId: Int, imagePath: String?, videoPath: String?) {
        val inputData = UploadComplaintMediaWorker.obtainInputData(
            complaintId = complaintId,
            imagePath = imagePath,
            videoPath = videoPath,
        )
        val request = OneTimeWorkRequestBuilder<UploadComplaintMediaWorker>()
            .setInputData(inputData)
            .build()
        WorkManager.getInstance(application).enqueue(request)
    }

    private suspend fun compressImage(path: String): String =
        getCropInfoUseCase.invoke(
            fileType = FileUtilsImpl.MEDIA_TYPE_IMAGE,
            mediaPlace = MediaControllerOpenPlace.Common
        )?.let {
            compressImageForUploadUseCase(
                imagePath = path,
                cropInfo = it
            )
        } ?: path

    private suspend fun compressVideo(path: String): String {
        val metadata = metaDataDelegate.getVideoMetadata(
            Uri.parse(path)
        ) ?: return path
        val cropInfo = getCropInfoUseCase.invoke(
            fileType = FileUtilsImpl.MEDIA_TYPE_VIDEO,
            mediaPlace = MediaControllerOpenPlace.Chat
        ) ?: return path
        val needCompress = cropInfo.needCompressMedia(
            currentWidth = metadata.width,
            currentBitrate = metadata.bitrate
        )
        return if (needCompress) {
            compressVideoUseCase.execute(path, cropInfo)
        } else {
            path
        }
    }
}
