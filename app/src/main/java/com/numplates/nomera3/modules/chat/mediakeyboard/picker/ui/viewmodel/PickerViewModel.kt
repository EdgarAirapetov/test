package com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.base.enums.PermissionState
import com.meera.core.extensions.combineWith
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
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.model.TotalSelectedMediaUiModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val NOT_EXISTED_ROOM = 0L

class PickerViewModel @Inject constructor(
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
) : ViewModel() {

    val messageTextLiveData: LiveData<TemporaryMessageText>
        get() = getMessageTextUseCase.invoke()

    val userNameLiveData: LiveData<TemporaryMessageText>
        get() = getUserNameUseCase.invoke()

    val chosenPhotosLiveData: LiveData<Set<String>>
        get() = getPickerMessageMedia.invoke()

    val editedPhotosLiveData: LiveData<Set<String>>
        get() = getEditedMessageMedia.invoke()

    val totalMediaListLiveData: LiveData<TotalSelectedMediaUiModel>
        get() = editedPhotosLiveData.combineWith(chosenPhotosLiveData) { edited, picked ->
            TotalSelectedMediaUiModel(
                mediaFromMessage = edited ?: emptySet(),
                mediaFromPicker = picked ?: emptySet()
            )
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

    fun setButtonStateByBehavior() {
        viewModelScope.launch {
            checkButtonsStateUseCase.invoke()
        }
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
}
