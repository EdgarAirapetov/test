package com.numplates.nomera3.modules.contentsharing.domain.usecase

import com.numplates.nomera3.modules.contentsharing.domain.repository.ContentSharingRepository
import com.numplates.nomera3.modules.share.data.entity.ResponseShareItem
import javax.inject.Inject

class GetShareItemsUseCase @Inject constructor(
    private val repository: ContentSharingRepository
) {

    suspend fun invoke(query: String?, lastContactId: String?, selectedUserId: Long?): List<ResponseShareItem> {
        return repository.getShareItems(query, lastContactId, selectedUserId)
    }

}
