package com.numplates.nomera3.modules.complains.data.repository

import com.numplates.nomera3.modules.complains.data.api.ComplaintApi
import com.numplates.nomera3.modules.complains.data.model.ChatComplaintParams
import com.numplates.nomera3.modules.complains.data.model.MomentComplaintParams
import com.numplates.nomera3.modules.complains.data.model.UserComplaintParams
import com.numplates.nomera3.modules.complains.domain.repository.ComplaintRepository
import javax.inject.Inject

class ComplaintRepositoryImpl @Inject constructor(
    private val complaintApi: ComplaintApi
) : ComplaintRepository {

    override suspend fun complainOnUser(params: UserComplaintParams): Int {
        return complaintApi.complainOnUser(params).data.complainId
    }

    override suspend fun complainOnChat(params: ChatComplaintParams): Int {
        return complaintApi.complainOnChat(params).data.complainId
    }

    override suspend fun complainOnMoment(params: MomentComplaintParams): Int {
        return complaintApi.complainOnMoment(params).data.complainId
    }
}
