package com.numplates.nomera3.modules.maps.ui.snippet.model

import com.numplates.nomera3.modules.maps.domain.model.UserSnippetModel

sealed class SnippetEvent {

    object ShowOnboarding : SnippetEvent()

    data class DispatchUserSelected(val userSnippetModel: UserSnippetModel) : SnippetEvent()

    data class DispatchSnippetSlide(val slideOffset: Float) : SnippetEvent()

    data class DispatchNewSnippetState(val snippetState: SnippetState) : SnippetEvent()
}
