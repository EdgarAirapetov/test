package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.Serializable


data class VehicleRequest (
        @SerializedName("country_id") var countryId: Int? = null,
        @SerializedName("brand_id") var makeId: Int? = null,
        @SerializedName("model_id") var model: Int? = null,
        @SerializedName("brand_name") var brand_name: String? = null,
        @SerializedName("model_name") var model_name: String? = null,
        @SerializedName("number")var number: String? = null,
        @SerializedName("description") var description: String? = null,
        @SerializedName("type") var type: String? = null
) : Serializable {
    constructor(v :Vehicle) : this(v.country?.countryId, v.make?.makeId,
        v.model?.modelId, v.make?.brandName, v.model?.modelName, v.number, v.description, v.type?.typeId)

    fun toRequestMap() : HashMap<String, RequestBody>{
        val requestMap : HashMap<String, RequestBody> = HashMap()
        requestMap["country_id"] = countryId.toString().toRequestBody(MultipartBody.FORM)
        requestMap["brand_id"] = makeId.toString().toRequestBody(MultipartBody.FORM)
        requestMap["model_id"] = model.toString().toRequestBody(MultipartBody.FORM)
        requestMap["brand_name"] = brand_name.orEmpty().toRequestBody(MultipartBody.FORM)
        requestMap["model_name"] = model_name.orEmpty().toRequestBody(MultipartBody.FORM)
        requestMap["number"] = number.orEmpty().toRequestBody(MultipartBody.FORM)
        requestMap["description"] = description.orEmpty().toRequestBody(MultipartBody.FORM)
        requestMap["type"] = type.toString().toRequestBody(MultipartBody.FORM)

        return requestMap
    }
}
