package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiHiWay

class DeletePhotoUseCase(private val api: ApiHiWay) {
    fun deletePhoto(photoId: Long) = api.removePhoto(photoId)
}