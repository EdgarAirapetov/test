package com.numplates.nomera3.modules.purchase.ui.mapper

import com.numplates.nomera3.modules.purchase.domain.model.SimplePurchaseModel
import com.numplates.nomera3.modules.purchase.ui.model.SimplePurchaseUiModel
import com.numplates.nomera3.modules.purchase.ui.model.UpgradeStatusUIState
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.numplates.nomera3.presentation.view.widgets.UserInfoUIEntity
import javax.inject.Inject

class PurchaseUiModelStateMapper @Inject constructor() {

    fun mapSimplePurchasesToSimplePurchaseUIModel(models: List<SimplePurchaseModel>): List<SimplePurchaseUiModel> {
        return models.map { model ->
            SimplePurchaseUiModel(
                marketId = model.marketId,
                description = model.description,
                price = model.price,
            )
        }
    }

    fun mapUserProfileToUpgradeStatusUIState(profile: UserProfileModel) : UpgradeStatusUIState {
        return UpgradeStatusUIState(
            name = profile.name ?: "",
            accountColor = profile.accountColor,
            accountType = profile.accountType,
            avatar = profile.avatarBig ?: "",
            birthday = profile.birthday,
            accountTypeExpiration = profile.accountTypeExpiration ?: 0,
            hideBirthday = profile.hideBirthday,
            cityName = profile.coordinates?.cityName,
            vehicles = profile.vehicles?.map { vehicle ->
                UserInfoUIEntity(
                    vehicle.brandLogo,
                    vehicle.typeId,
                    vehicle.hasNumber,
                    vehicle.number,
                    vehicle.countryId,
                    vehicle.brandName,
                    vehicle.modelName,
                    vehicle.isMain
                )
            } ?: emptyList()

        )
    }
}
