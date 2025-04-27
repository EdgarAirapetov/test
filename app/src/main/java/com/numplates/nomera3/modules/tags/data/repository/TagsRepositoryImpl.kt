package com.numplates.nomera3.modules.tags.data.repository

import com.numplates.nomera3.modules.tags.data.api.TagApi
import com.numplates.nomera3.modules.tags.data.entity.HashtagTagListModel
import com.numplates.nomera3.modules.tags.data.entity.UniqueNameTagListResponse
import timber.log.Timber
import javax.inject.Inject

class TagsRepositoryImpl @Inject constructor(private val tagsApi: TagApi) : TagsRepository {

    override suspend fun getTagListByUniqueName(
        uniqueName: String,
        roomId: Int?,
        limit: Int?,
        offset: Int?,
        success: (UniqueNameTagListResponse) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val result = tagsApi.getTagListByUniqueName(uniqueName, roomId)
            if (result.data != null) {
                success(result.data)
            } else {
                fail(Exception("searchByUniqueNamePost result = null"))
            }
        } catch (e: Exception) {
            fail(e)
            Timber.e(e)
        }
    }

    override suspend fun getTagListByHashtag(
        hashtag: String,
        success: (HashtagTagListModel) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val hashTagWithoutPrefix = hashtag.takeIf { it.isNotBlank() && it.isNotEmpty() }?.removePrefix("#")
            Timber.e("getTagListByHashtag: hashTagWithoutPrefix == %s", hashTagWithoutPrefix ?: "")
            tagsApi.getTagListByHashtag(hashTagWithoutPrefix)
                .data
                ?.also(success)
                ?: fail(Exception("response data is null"))
        } catch (e: Exception) {
            Timber.e(e)
            fail(e)
        }
    }
}
