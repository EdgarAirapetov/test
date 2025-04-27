package com.numplates.nomera3.modules.userprofile.ui.entity

import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.ui.Separable
import com.numplates.nomera3.modules.userprofile.ui.adapter.UserProfileAdapterType

class UserEntityGarageFloor(
    val listVehicles: List<VehicleUIModel>,
    val accountTypeEnum: AccountTypeEnum,
    val vehicleCount: Int = 0,
    val isMe: Boolean = true,
    val userColor: Int? = 0,
    override var isSeparable: Boolean = true
) : UserUIEntity, Separable {
    override val type: UserProfileAdapterType
        get() = UserProfileAdapterType.GARAGE_FLOOR
}

data class VehicleUIModel(
    val vehicleId: Long,
    val brandLogo: String?,
    val avatarSmall: String,
    val hasNumber: Boolean?,
    val brandName: String?,
    val modelName: String?,
    val number: String?,
    val typeId: Int?,
    val countryId: Long?,
    val hidden: Boolean
) {
    companion object {
        const val TYPE_AUTO = 1
        const val TYPE_MOTO = 2
        const val TYPE_BICYCLE = 3
        const val TYPE_KICKSCOOTER = 4
        const val TYPE_SKATEBOARD = 5
        const val TYPE_ROLLERSKATES = 6
        const val TYPE_SNOWMOBILE = 7
        const val TYPE_JETSKI = 8
        const val TYPE_PLANE = 9
        const val TYPE_BOAT = 10
    }
}
