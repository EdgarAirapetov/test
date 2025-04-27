package com.numplates.nomera3.data.network

import com.numplates.nomera3.data.network.core.ResponseWrapper
import retrofit2.http.Body
import retrofit2.http.POST

interface SyncContactsApi {

    @POST("/v3/users/contacts/upload")
    suspend fun postContacts(
        @Body contacts: SyncContactsDto
    ) : ResponseWrapper<SyncContactsResultDto>
}
