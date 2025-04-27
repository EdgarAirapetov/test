package com.numplates.nomera3.modules.tags.data.repository

import com.numplates.nomera3.modules.tags.data.entity.HashtagTagListModel
import com.numplates.nomera3.modules.tags.data.entity.UniqueNameTagListResponse

interface TagsRepository {

    suspend fun getTagListByUniqueName(
            uniqueName: String,
            roomId: Int? = null, // optional value
            limit: Int? = null, // optional value default: 0
            offset: Int? = null, //optional value default: 20
            success: (UniqueNameTagListResponse) -> Unit,
            fail: (Exception) -> Unit
    )

    suspend fun getTagListByHashtag(
            hashtag: String,
            success: (HashtagTagListModel) -> Unit,
            fail: (Exception) -> Unit
    )
}