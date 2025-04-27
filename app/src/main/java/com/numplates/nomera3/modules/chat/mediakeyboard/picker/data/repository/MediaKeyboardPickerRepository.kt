package com.numplates.nomera3.modules.chat.mediakeyboard.picker.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.meera.core.base.enums.PermissionState
import com.meera.core.di.scopes.AppScope
import com.meera.core.extensions.combineWith
import com.meera.core.extensions.empty
import com.numplates.nomera3.modules.chat.helpers.isNetworkPath
import com.numplates.nomera3.modules.chat.mediakeyboard.data.entity.TemporaryMessageText
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.data.entity.MediaKeyboardViewEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import kotlin.random.Random

private const val RANDOM_STRING_SPACE_COUNT = 30
private const val SPACE = " "


@AppScope
class MediaKeyboardPickerRepository @Inject constructor() {

    private val _chosenPhotosUriListFlow = MutableLiveData(setOf<String>())
    val chosenPhotosUriListFlow: LiveData<Set<String>> = _chosenPhotosUriListFlow

    private val _messageTextLiveData = MutableLiveData(TemporaryMessageText())
    val messageTextLiveData: LiveData<TemporaryMessageText> = _messageTextLiveData

    private val _userNameLiveData = MutableLiveData(TemporaryMessageText())
    val userNameLiveData: LiveData<TemporaryMessageText> = _userNameLiveData

    private val _mediaButtonsUiState = MutableLiveData(false)
    val mediaButtonsLiveData: LiveData<Boolean> = _mediaButtonsUiState

    private val _mediaKeyboardViewEvent = MutableSharedFlow<MediaKeyboardViewEvent>()
    val mediaKeyboardViewEvent: SharedFlow<MediaKeyboardViewEvent> = _mediaKeyboardViewEvent

    private val _permissionViewsUiVisibilityState = MutableLiveData(false)
    val permissionViewsVisibilityLiveData: LiveData<Boolean> = _permissionViewsUiVisibilityState

    private val _permissionViewUiState = MutableLiveData(PermissionState.GRANTED)
    val permissionViewStateLiveData: LiveData<PermissionState> = _permissionViewUiState

    private val _editedMessageMediaLive = MutableLiveData(setOf<String>())
    val editedMessageMediaLive: LiveData<Set<String>> = _editedMessageMediaLive

    private val _chosenPhotosUriLiveData = MutableLiveData(setOf<String>())
    val chosenPhotosUriLiveData: LiveData<Set<String>> = _chosenPhotosUriLiveData

    private val totalMediaSet = mutableSetOf<String>()


    val totalMediaListLive: LiveData<Set<String>> = _editedMessageMediaLive
        .combineWith(_chosenPhotosUriLiveData) { fromMessage, fromPicker ->
            totalMediaSet
        }

    fun addPhoto(uri: String) {
        val liveDataToUpdate = if (uri.isNetworkPath()) _editedMessageMediaLive else _chosenPhotosUriLiveData
        val newSet = liveDataToUpdate.value?.toMutableSet() ?: mutableSetOf()
        newSet.add(uri)
        totalMediaSet.add(uri)
        liveDataToUpdate.value = newSet
    }

    fun addPhotoFromClipboard(uri: String) {
        val liveDataToUpdate = _editedMessageMediaLive
        val newSet = liveDataToUpdate.value?.toMutableSet() ?: mutableSetOf()
        val uriWithRandomSpaces = "${generateRandomSpaces()}$uri"
        newSet.add(uriWithRandomSpaces)
        totalMediaSet.add(uriWithRandomSpaces)
        liveDataToUpdate.value = newSet
    }

    fun removePhoto(uri: String) {
        val liveDataToUpdate = if (uri.isNetworkPath()) _editedMessageMediaLive else _chosenPhotosUriLiveData
        val newSet = liveDataToUpdate.value?.toMutableSet() ?: mutableSetOf()
        newSet.remove(uri)
        totalMediaSet.remove(uri)
        liveDataToUpdate.value = newSet
    }

    fun removePhotoFromTotalMedia(uri: String) {
        val totalSet = totalMediaListLive.value?.toMutableSet() ?: mutableSetOf()
        totalSet.remove(uri)
        totalMediaSet.remove(uri)
        _editedMessageMediaLive.value = emptySet()
        _chosenPhotosUriLiveData.value = totalSet
    }

    fun setMessageText(roomId: Long, text: String) {
        _messageTextLiveData.value = TemporaryMessageText(roomId, text)
    }

    fun setUserName(userName: String) {
        _userNameLiveData.value = TemporaryMessageText(name = userName)
    }

    fun clear() {
        clearMediaContent()
        if (!messageTextLiveData.hasActiveObservers()) {
            _messageTextLiveData.value = TemporaryMessageText()
        }
    }

    fun clearMediaContent() {
        totalMediaSet.clear()
        _chosenPhotosUriLiveData.value = emptySet()
        _editedMessageMediaLive.value = emptySet()
    }

    suspend fun sendSelectedEvent() {
        _mediaKeyboardViewEvent.emit(MediaKeyboardViewEvent.SendSelectedViewEvent)
    }

    suspend fun replaceMediaContent(data: Set<String>) {
        _editedMessageMediaLive.value = data.filter { it.isNetworkPath() }.toSet()
        _chosenPhotosUriLiveData.value = data.filterNot { it.isNetworkPath() }.toSet()
    }

    suspend fun checkButtonsState() {
        _mediaKeyboardViewEvent.emit(MediaKeyboardViewEvent.CheckButtonsState)
    }

    fun mediaKeyboardStateChanged(isShow: Boolean) {
        _mediaButtonsUiState.value = isShow
    }

    fun permissionViewsVisibilityChanged(isShow: Boolean) {
        _permissionViewsUiVisibilityState.value = isShow
    }

    fun permissionViewStateChanged(permissionState: PermissionState) {
        _permissionViewUiState.value = permissionState
    }

    /**
     * !!! Hack
     * Добавляем рандомную строку пробелов в Uri
     * для обхода уникальности Set, затем на выходе trim()
     */
    private fun generateRandomSpaces(): String =
        (0..Random.nextInt(RANDOM_STRING_SPACE_COUNT)).joinToString(String.empty()) { SPACE }
}
