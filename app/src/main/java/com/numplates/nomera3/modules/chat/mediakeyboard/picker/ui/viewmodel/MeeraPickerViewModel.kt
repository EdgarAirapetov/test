package com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.viewmodel

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.base.enums.PermissionState
import com.meera.core.extensions.combineWith
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_VIDEO
import com.numplates.nomera3.R
import com.numplates.nomera3.domain.interactornew.GetKeyboardHeightUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.data.entity.TemporaryMessageText
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.AddPhotoUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.AddSelectedMediaKeyboardUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.CheckButtonsStateUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.ClearMediaContentUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.GetEditedMessageMedia
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.GetMediaButtonsStateUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.GetMessageTextUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.GetPermissionViewStateUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.GetPermissionViewsVisibilityUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.GetPickerMessageMedia
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.GetUserNameUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.RemovePhotoUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.SetMessageTextUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.SetPermissionViewStateUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.ShowOrHidePermissionViewsUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.fragment.MAX_PICTURE_COUNT
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.fragment.MAX_VIDEO_COUNT
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.model.MeeraPickerUiEffect
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.model.TotalSelectedMediaUiModel
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.helper.GalleryTilesHelper
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.model.PickerTile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList
import javax.inject.Inject

private const val MAX_VIDEO_DURATION_MS = 5 * 60 * 1000
private const val NOT_EXISTED_ROOM = 0L

@Suppress("CanBeParameter")
class MeeraPickerViewModel @Inject constructor(
    private val addPhotoUseCase: AddPhotoUseCase,
    private val removePhotoUseCase: RemovePhotoUseCase,
    private val getMessageTextUseCase: GetMessageTextUseCase,
    private val setMessageTextUseCase: SetMessageTextUseCase,
    private val getUserNameUseCase: GetUserNameUseCase,
    private val addSelectedMediaKeyboardUseCase: AddSelectedMediaKeyboardUseCase,
    private val getMediaButtonsStateUseCase: GetMediaButtonsStateUseCase,
    private val getPermissionViewsVisibilityUseCase: GetPermissionViewsVisibilityUseCase,
    private val getPermissionViewStateUseCase: GetPermissionViewStateUseCase,
    private val setPermissionViewStateUseCase: SetPermissionViewStateUseCase,
    private val showOrHidePermissionViewsUseCase: ShowOrHidePermissionViewsUseCase,
    private val checkButtonsStateUseCase: CheckButtonsStateUseCase,
    private val clearMediaContentUseCase: ClearMediaContentUseCase,
    private val getEditedMessageMedia: GetEditedMessageMedia,
    private val getPickerMessageMedia: GetPickerMessageMedia,
    private val getKeyboardHeightUseCase: GetKeyboardHeightUseCase,
    private val galleryTilesHelper: GalleryTilesHelper,
    private val filesManager: FileManager,
) : ViewModel() {

    val messageTextLiveData: LiveData<TemporaryMessageText>
        get() = getMessageTextUseCase.invoke()

    val userNameLiveData: LiveData<TemporaryMessageText>
        get() = getUserNameUseCase.invoke()

    val chosenPhotosLiveData: LiveData<Set<String>>
        get() = getPickerMessageMedia.invoke()

    val editedPhotosLiveData: LiveData<Set<String>>
        get() = getEditedMessageMedia.invoke()

    private val counters: LinkedList<Uri> = LinkedList()

    private val _meeraPickerEffectsFlow = MutableSharedFlow<MeeraPickerUiEffect>()
    val meeraPickerEffectsFlow: SharedFlow<MeeraPickerUiEffect> = _meeraPickerEffectsFlow

    private val totalMediaListLiveData: LiveData<TotalSelectedMediaUiModel> =
        editedPhotosLiveData.combineWith(chosenPhotosLiveData) { edited, picked ->
            updateCounters(picked.orEmpty().toList())
            TotalSelectedMediaUiModel(
                mediaFromMessage = edited ?: emptySet(),
                mediaFromPicker = picked ?: emptySet()
            )
        }

    private val _pickerTilesLiveData = MutableLiveData<List<PickerTile>>()
    val pickerTilesLiveData: LiveData<List<PickerTile>> =
        _pickerTilesLiveData.combineWith(totalMediaListLiveData) { pickerTiles, _ ->
            pickerTiles.orEmpty().map { pickerTile ->
                pickerTile.clone().apply {
                    isSelected = counters.contains(pickerTile.imageUri)
                    counter = counters.indexOf(pickerTile.imageUri).inc()
                }
            }
        }

    val mediaButtonsLiveData: LiveData<Boolean> = getMediaButtonsStateUseCase.invoke()
    val permissionViewsVisibilityLiveData: LiveData<Boolean> = getPermissionViewsVisibilityUseCase.invoke()
    val permissionViewStateLiveData: LiveData<PermissionState> = getPermissionViewStateUseCase.invoke()

    fun messageChanged(message: String) {
        setMessageTextUseCase.invoke(NOT_EXISTED_ROOM, message)
    }

    fun addPhotoClicked(photoUri: String) {
        addPhotoUseCase.invoke(photoUri)
    }

    fun removePhotoClicked(photoUri: String) {
        removePhotoUseCase.invoke(photoUri)
    }

    fun removeAllPhotos() {
        clearMediaContentUseCase.invoke()
    }

    fun sendSelectedEvent() {
        viewModelScope.launch {
            addSelectedMediaKeyboardUseCase.invoke()
        }
    }

    fun showOrHidePermissionViews(isNeedShow: Boolean) {
        viewModelScope.launch {
            showOrHidePermissionViewsUseCase.invoke(isNeedShow)
        }
    }

    fun setPermissionViewState(permissionState: PermissionState) {
        viewModelScope.launch {
            setPermissionViewStateUseCase.invoke(permissionState)
        }
    }

    fun getKeyboardHeight() = getKeyboardHeightUseCase.invoke()

    fun loadPickerTiles(builder: TedBottomSheetDialogFragment.BaseBuilder<*>) {
        viewModelScope.launch {
            val tiles = withContext(Dispatchers.IO) {
                galleryTilesHelper.loadPickerTiles(builder)
            }
            _pickerTilesLiveData.postValue(tiles)
        }
    }

    fun addImageFromCamera(uri: Uri) {
        val selectedUrisSize = totalMediaListLiveData.value?.totalMedias()?.map(Uri::parse).orEmpty().size
        if (selectedUrisSize == MAX_PICTURE_COUNT) {
            emitEffect(MeeraPickerUiEffect.ShowMediaAlert(R.string.maximum_photo_files_selected))
        } else {
            addPhotoClicked(uri.toString())
            emitEffect(MeeraPickerUiEffect.UpdateListTiles)
        }
    }

    fun toggleSelection(uri: Uri) {
        if (filesManager.getMediaType(uri) == MEDIA_TYPE_VIDEO) {
            val videoDuration = filesManager.getVideoDurationMils(uri)
            if (videoDuration > MAX_VIDEO_DURATION_MS) {
                emitEffect(MeeraPickerUiEffect.ShowTooLongVideoAlert(uri))
                return
            }
        }
        val targetTile = pickerTilesLiveData.value.orEmpty().firstOrNull { item -> item.imageUri == uri } ?: return
        if (targetTile.isSelected) {
            removePhotoClicked(uri.toString())
        } else {
            val selectedUris = totalMediaListLiveData.value?.totalMedias()?.map(Uri::parse).orEmpty()
            val mimeTypes = selectedUris.map {
                val extension = MimeTypeMap.getFileExtensionFromUrl(it.toString())
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }
            val containsVideo = mimeTypes.any { it?.startsWith("video") == true }
            if (containsVideo ||
                selectedUris.size == MAX_PICTURE_COUNT ||
                (targetTile.isVideoTile && selectedUris.isNotEmpty())
            ) {
                val alertMessageRes = when {
                    selectedUris.size == MAX_VIDEO_COUNT && containsVideo && targetTile.isVideoTile -> R.string.maximum_video_files_selected
                    selectedUris.size == MAX_PICTURE_COUNT -> R.string.maximum_photo_files_selected
                    else -> R.string.cannot_select_image_and_video
                }
                emitEffect(MeeraPickerUiEffect.ShowMediaAlert(alertMessageRes))
            } else {
                addPhotoClicked(uri.toString())
            }
        }
        setButtonStateByBehavior()
    }

    private fun updateCounters(links: List<String>) {
        val uris = links.map(Uri::parse)
        if (uris.size < counters.size) {
            counters.filter { uri -> !uris.contains(uri) }
                .forEach { uri -> counters.remove(uri) }
        } else {
            uris.filter { uri -> !counters.contains(uri) }
                .forEach { uri -> counters.addLast(uri) }
        }
    }

    private fun setButtonStateByBehavior() {
        viewModelScope.launch {
            checkButtonsStateUseCase.invoke()
        }
    }

    private fun emitEffect(effect: MeeraPickerUiEffect) {
        viewModelScope.launch {
            _meeraPickerEffectsFlow.emit(effect)
        }
    }
}
