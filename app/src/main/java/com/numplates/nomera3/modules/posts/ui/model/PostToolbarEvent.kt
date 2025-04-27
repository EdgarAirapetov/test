package com.numplates.nomera3.modules.posts.ui.model

sealed interface PostToolbarEvent {
    object OptionsClicked : PostToolbarEvent
    object BackClicked : PostToolbarEvent
}
