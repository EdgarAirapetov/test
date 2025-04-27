package com.numplates.nomera3.modules.maps.ui.model

import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity

sealed interface MainMapOpenPayload {
    data class EventPayload(val eventPost: PostUIEntity) : MainMapOpenPayload
}
