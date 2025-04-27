package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiHiWay
import com.numplates.nomera3.data.network.Settings
import com.numplates.nomera3.data.network.core.ResponseWrapper
import io.reactivex.Flowable

@Deprecated("Use appinfo module")
class GetInfoApplicationUseCase(private val apiHiWay: ApiHiWay?) {

    fun getApplicationInfo(): Flowable<ResponseWrapper<Settings>>? {
        return apiHiWay?.applicationInfo
    }

}
