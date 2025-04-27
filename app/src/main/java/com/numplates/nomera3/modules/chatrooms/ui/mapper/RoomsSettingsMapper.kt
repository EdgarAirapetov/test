package com.numplates.nomera3.modules.chatrooms.ui.mapper

import com.numplates.nomera3.modules.chatrooms.data.api.GetRoomsResponse
import com.numplates.nomera3.modules.chatrooms.data.entity.RoomsSettingsModel
import javax.inject.Inject

class RoomsSettingsMapper @Inject constructor() {

    fun mapRoomsSettings(roomsResponse: GetRoomsResponse?): RoomsSettingsModel {
        return RoomsSettingsModel(
            whoCanChat = roomsResponse?.whoCanChat,
            countBlackList = roomsResponse?.countBlackList,
            countWhiteList = roomsResponse?.countWhiteList,
            chatRequest = roomsResponse?.chatRequest
        )
    }

}
