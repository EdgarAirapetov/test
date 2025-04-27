package com.numplates.nomera3.modules.userprofile.domain.model.usermain

data class UserVehicleModel(
    val vehicleId: Long,
    val brandLogo: String?,
    val avatarSmall: String,
    val hasNumber: Boolean?,
    val brandName: String?,
    val modelName: String?,
    val number: String?,
    val typeId: Int?,
    val countryId: Long?,
    val isMain: Boolean?
)
