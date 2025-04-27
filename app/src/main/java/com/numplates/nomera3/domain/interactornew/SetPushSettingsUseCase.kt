package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiHiWay
import com.numplates.nomera3.data.network.PushSettingsResponse
import com.numplates.nomera3.data.network.core.ResponseWrapper
import io.reactivex.Flowable

class SetPushSettingsUseCase(private val repository: ApiHiWay) {

    fun setPushSettings(
            userId: Long,
            settings: PushSettingsResponse
    ): Flowable<ResponseWrapper<PushSettingsResponse>> = repository.updatePushSettings(settings, userId)

    fun getPushSettings(
            userId: Long
    ): Flowable<ResponseWrapper<PushSettingsResponse>> = repository.getPushSettings(userId)
}