package com.numplates.nomera3.modules.contentsharing.ui.infrastructure

import android.net.Uri
import com.numplates.nomera3.modules.share.ui.entity.UIShareItem

object SharingDataCache {

    private val urisToUpload: MutableList<Uri> = mutableListOf()
    private val usersToSend: MutableList<UIShareItem> = mutableListOf()

    var contentLink: String? = null
    var messageComment: String? = null

    fun cacheUris(uris: List<Uri>) {
        urisToUpload.clear()
        urisToUpload.addAll(uris)
    }

    fun getUris(): List<Uri> {
        return urisToUpload
    }

    fun cacheUsers(users: List<UIShareItem>) {
        usersToSend.clear()
        usersToSend.addAll(users)
    }

    fun getUsers(): List<UIShareItem> {
        return usersToSend
    }

    fun getUsersIds(): List<Long> {
        return usersToSend
            .filter { it.isGroupChat.not() && it.isChecked && it.userId != null }
            .map { it.userId!! }
    }

    fun getRoomsIds(): List<Long> {
        return usersToSend
            .filter { it.isGroupChat && it.isChecked && it.roomId != null }
            .map { it.roomId!! }
    }

    fun clearData() {
        urisToUpload.clear()
        usersToSend.clear()
        contentLink = null
        messageComment = null
    }
}
