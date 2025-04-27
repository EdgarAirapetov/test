package com.numplates.nomera3.modules.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.helpers.allMediaAttachments
import com.numplates.nomera3.modules.chat.helpers.editmessage.models.EditMessageModel
import com.numplates.nomera3.modules.chat.helpers.isRepost
import com.numplates.nomera3.modules.chat.messages.domain.mapper.EditMessageMapper
import timber.log.Timber

class ChatMessageEditor constructor(
    private val onSendEditedMessage: suspend (EditMessageModel) -> Unit,
    private val editingEvents: (EditingEvents) -> Unit,
    private val editMessageMapper: EditMessageMapper,
) {

    private val _liveState: MutableLiveData<EditingState> = MutableLiveData(EditingState())
    val liveState: LiveData<EditingState> = _liveState.distinctUntilChanged()

    private var oldMessage: MessageEntity? = null
    private var editedText: String? = null
    private var editedMediaUriSet: Set<String>? = null

    fun getEditedMediaUris(): Set<String>? = editedMediaUriSet

    fun getOriginalMessage(): MessageEntity? = oldMessage?.copy()

    fun isEditInProgress(): Boolean = _liveState.value?.isEditing ?: false

    fun startEditingMessage(messageToEdit: MessageEntity) {
        oldMessage = messageToEdit
        _liveState.value = EditingState(
            isEditing = true,
            isRepostMessage = messageToEdit.isRepost(),
        )
        editingEvents.invoke(EditingEvents.EditingStarted)
    }

    fun finishEditingMessage() {
        if (_liveState.value?.isEditing == true) {
            _liveState.value = _liveState.value?.copy(isEditing = false)
            clearEditingState()
            editingEvents.invoke(EditingEvents.EditingFinished)
            Timber.d("finish message editing.")
        }
    }

    fun updateText(newText: String?) {
        Timber.d("message editing updated text: $newText")
        editedText = newText
        _liveState.value = _liveState.value?.copy(
            hasChanges = hasMessageBeenChanged(),
            isEmptyMessage = isEmptyEditedMessage(),
        )
    }

    fun updateMedias(newMediaUriSet: Set<String>?) {
        Timber.d("message editing updated medias: $newMediaUriSet")
        val oldMediaUriSet = oldMessage?.allMediaAttachments()?.toSet()
        if (oldMediaUriSet != newMediaUriSet) {
            editedMediaUriSet = newMediaUriSet
            _liveState.value = _liveState.value?.copy(
                hasChanges = hasMessageBeenChanged(),
                isEmptyMessage = isEmptyEditedMessage(),
            )
        }
    }

    suspend fun sendEditedMessage() {
        if (editedText == null && editedMediaUriSet == null) return
        if (!hasMessageBeenChanged()) {
            clearEditingState()
            editingEvents.invoke(EditingEvents.EditingFinished)
        } else {
            val oldMessage = oldMessage ?: error("Not supposed to be null here")
            onSendEditedMessage.invoke(editMessageMapper.map(
                oldMessage = oldMessage,
                editedText = editedText,
                editedMediaUriSet = editedMediaUriSet,
            ))
        }
    }

    private fun isEmptyEditedMessage(): Boolean {
        return editedText.isNullOrBlank() && editedMediaUriSet.isNullOrEmpty()
    }

    private fun hasMessageBeenChanged(): Boolean {
        val textChanged = !editedText.equals((oldMessage?.tagSpan?.text ?: oldMessage?.content))
        val mediaChanged = editedMediaUriSet.orEmpty() != oldMessage?.allMediaAttachments()?.toSet()
        return textChanged || mediaChanged
    }

    private fun clearEditingState() {
        oldMessage = null
        editedText = null
        editedMediaUriSet = null
        _liveState.value = EditingState()
    }
}

/**
 * Support object to define editing state
 *
 * @param isEditing true when user started editing process
 * @param hasChanges true if original message not equal to the editing message
 * @param isEmptyMessage true if new message does not have text and media files
 * @param isRepostMessage true if original message is a repost one
 */
data class EditingState(
    val isEditing: Boolean = false,
    val hasChanges: Boolean = false,
    val isEmptyMessage: Boolean = false,
    val isRepostMessage: Boolean = false,
)

sealed interface EditingEvents {
    data object EditingStarted : EditingEvents
    data object EditingFinished : EditingEvents
}
