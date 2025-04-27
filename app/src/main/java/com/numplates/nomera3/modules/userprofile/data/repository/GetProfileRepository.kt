package com.numplates.nomera3.modules.userprofile.data.repository

import io.reactivex.Single
import org.phoenixframework.Message

interface GetProfileRepository {

    fun getProfile(): Single<Message>
}
