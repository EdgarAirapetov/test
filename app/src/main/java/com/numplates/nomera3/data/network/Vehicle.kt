package com.numplates.nomera3.data.network

import androidx.room.Ignore
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class Vehicle (

        @SerializedName("vehicle_id")
        var vehicleId: Int?,

        @SerializedName("type")
        var type: VehicleType?,                         //=  VehicleType(),

        @SerializedName("image")
        var picture: String?,

        @SerializedName("number")
        var number: String?,

        @SerializedName("is_main")
        var mainVehicle: Int,

        @Ignore
        @SerializedName("country")
        var country: Country? = Country(),

        @SerializedName("brand_name")
        var brandName: String?,

        @SerializedName("model_name")
        var modelName: String?,

//        @SerializedName("brand_name") var brandName: String? = null,
        // todo Add to Room
        @SerializedName("brand")
        var make: CarsMakes.Make?,      //= CarsMakes.Make(),

        @Ignore
        @SerializedName("model")
        var model: CarsModels.Model? = CarsModels.Model(),

        @SerializedName("description")
        var description: String?,

        @Ignore
        @SerializedName("photos")
        var photos: List<PhotoModel>? = null

) : Serializable {

        constructor() : this(0, null, "", "",
                0, null, "",
                "", null, null,
                "", null)
}

