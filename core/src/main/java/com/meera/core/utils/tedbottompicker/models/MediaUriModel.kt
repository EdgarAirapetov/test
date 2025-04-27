package com.meera.core.utils.tedbottompicker.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaUriModel(
    val initialUri: Uri,
    var editedUri: Uri? = null,
    val networkId: String? = null
) : Parcelable {
    companion object {
        fun initial(uri: Uri) = MediaUriModel(initialUri = uri)

        fun edited(initialUri:Uri, editedUri:Uri) = MediaUriModel(initialUri = initialUri,editedUri = editedUri)
    }

    fun clearEditedUri() {
        editedUri = null
    }

    fun getActualUri(): Uri = editedUri ?: initialUri

    fun isEdited(): Boolean = editedUri != null
}
