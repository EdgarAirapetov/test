package com.numplates.nomera3.presentation.view.fragments.vehicleedit.usecase

import com.numplates.nomera3.data.network.ApiHiWayKt
import com.numplates.nomera3.data.network.Vehicle
import com.numplates.nomera3.data.network.VehicleRequest
import com.numplates.nomera3.data.network.core.ResponseWrapper
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class UpdateVehicleUseCase @Inject constructor(
    private val api: ApiHiWayKt
) {
    suspend fun invoke(vehicle: Vehicle, image: String?): ResponseWrapper<Vehicle?> {
        val part: MultipartBody.Part? = if (image.isNullOrEmpty().not()) {
            val file = File(image)
            MultipartBody.Part.createFormData(
                "image", file.name, file.asRequestBody("image/*".toMediaTypeOrNull())
            )
        } else null
        return part?.let {
            api.updateVehicle(vehicle.vehicleId, VehicleRequest(vehicle).toRequestMap(), part)
        } ?: run {
            api.updateVehicle(vehicle.vehicleId, VehicleRequest(vehicle))
        }
    }
}
