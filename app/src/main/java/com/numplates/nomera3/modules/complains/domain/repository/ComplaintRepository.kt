package com.numplates.nomera3.modules.complains.domain.repository

import com.numplates.nomera3.modules.complains.data.model.ChatComplaintParams
import com.numplates.nomera3.modules.complains.data.model.MomentComplaintParams
import com.numplates.nomera3.modules.complains.data.model.UserComplaintParams

interface ComplaintRepository {

    /**
     * Send complaint on user and get complaint's id to upload files if required
     *
     * @return complaint id
     */
    suspend fun complainOnUser(params: UserComplaintParams): Int

    suspend fun complainOnChat(params: ChatComplaintParams): Int

    suspend fun complainOnMoment(params: MomentComplaintParams): Int
}
