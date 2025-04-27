package com.numplates.nomera3.modules.maps.ui.snippet.model

sealed interface UserSnippetItem

object LoaderItem : UserSnippetItem

data class UserPreviewItem(
    val uid: Long,
    val payload: Any,
) : UserSnippetItem
