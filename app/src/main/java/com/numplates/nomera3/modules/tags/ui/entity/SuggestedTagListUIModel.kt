package com.numplates.nomera3.modules.tags.ui.entity

sealed class SuggestedTagListUIModel {

    data class HashtagUIModel(var id: Int, var name: String, var count: Long) : SuggestedTagListUIModel()

    data class UniqueNameUIModel(
        var id: Long,
        var imageURL: String,
        var uniqueName: String,
        var userName: String,
        var isUserVerified : Boolean
    ) : SuggestedTagListUIModel()
}
