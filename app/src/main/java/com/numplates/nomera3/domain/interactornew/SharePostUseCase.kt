package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiMain

class SharePostUseCase(private val endpoints: ApiMain) {

    suspend fun getGroupsAllowedToRepost(startIndex: Int, limit: Int) =
            endpoints.getGroupsAllowedToRepost(startIndex, limit)

}
