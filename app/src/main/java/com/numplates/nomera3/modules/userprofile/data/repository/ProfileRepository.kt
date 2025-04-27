package com.numplates.nomera3.modules.userprofile.data.repository

import com.meera.db.models.userprofile.UserProfileNew
import com.numplates.nomera3.data.network.EmptyModel
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.userprofile.domain.model.AvatarModel
import com.numplates.nomera3.modules.userprofile.domain.model.ProfileSuggestionModel
import com.numplates.nomera3.modules.userprofile.domain.model.UserAvatarsModel
import com.numplates.nomera3.modules.userprofile.domain.model.UserGalleryModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun requestProfileDbModel(userId: Long, gpsX: Float?, gpsY: Float?, withoutSideEffects: Boolean): UserProfileNew

    suspend fun requestProfile(userId: Long, withoutSideEffects: Boolean): UserProfileModel

    suspend fun setProfileViewed(userId: Long) : ResponseWrapper<EmptyModel>

    suspend fun requestOwnProfileSynch() : UserProfileNew

    suspend fun updateOwnProfileDb(profile: UserProfileNew) : Long

    fun getOwnProfileModelFlow(): Flow<UserProfileModel>

    suspend fun getOwnLocalProfile(): UserProfileModel?

    suspend fun setAvatarAsMain(photoId: Long): AvatarModel

    suspend fun getAvatars(userId: Long, limit: Int, offset: Int): UserAvatarsModel

    suspend fun getGallery(userId: Long, limit: Int, offset: Int): UserGalleryModel

    suspend fun getPost(postId: Long): PostUIEntity

    suspend fun getProfileSuggestions(userId: Long): List<ProfileSuggestionModel>

    suspend fun makeCallUnvailables(userId: Long): Boolean
}
