package com.numplates.nomera3.modules.tags.domain.mappers

import com.meera.core.extensions.toBoolean
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.modules.tags.data.entity.HashtagTagListModel
import com.numplates.nomera3.modules.tags.ui.entity.SuggestedTagListUIModel

object TagModelMapper {

    fun createHashtagUIModelList(dataModel: HashtagTagListModel?): List<SuggestedTagListUIModel.HashtagUIModel> {
        return dataModel?.tagList
                ?.filterNotNull()
                ?.filter { it.id != null && it.text != null && it.count != null }
                ?.map { SuggestedTagListUIModel.HashtagUIModel(it.id, it.text, it.count) }
                ?: listOf()
    }

    fun createUniqueNameUIModel(userSimple: UserSimple?): SuggestedTagListUIModel.UniqueNameUIModel? {
        return userSimple?.let { user ->
            val userId = user.userId
            val imageURL = user.avatarSmall
            val uniqueName = user.uniqueName
            val name = user.name
            val isUserVerified = user.profileVerified.toBoolean()
            if (imageURL != null && uniqueName != null && name != null) {
                SuggestedTagListUIModel.UniqueNameUIModel(
                    userId,
                    imageURL,
                    uniqueName,
                    name,
                    isUserVerified)
            } else {
                null
            }
        }
    }

    fun createUniqueNameUIModelList(
        userSimpleList: List<UserSimple>?
    ): List<SuggestedTagListUIModel.UniqueNameUIModel> {
        return userSimpleList
                ?.mapNotNull { createUniqueNameUIModel(it) }
                ?: listOf()
    }
}
