package com.numplates.nomera3.modules.newroads.data.entities

import com.numplates.nomera3.data.network.ApiMain
import timber.log.Timber
import javax.inject.Inject

class MarkSubscriptionPostReadUseCase @Inject constructor(private val api: ApiMain) {
    suspend fun execute(): Boolean {
        return try {
            api.markSubscriptionPostRead().data
        } catch (exception: Exception) {
            Timber.e("Mark post read failed ${exception.message}")
            false
        }
    }
}
