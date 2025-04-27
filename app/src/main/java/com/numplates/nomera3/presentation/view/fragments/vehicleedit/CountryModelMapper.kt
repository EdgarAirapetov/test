package com.numplates.nomera3.presentation.view.fragments.vehicleedit

import com.meera.core.dialogs.unlimiteditem.MeeraConfirmDialogUnlimitedNumberItemsData
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.getCountryId
import javax.inject.Inject

class CountryModelMapper @Inject constructor() {
    fun mapRegistrationCountryModelListToMeeraConfirmDialogUnlimitedNumberItemsDataList(
        items: List<RegistrationCountryModel>, isAuto: Boolean
    ): List<MeeraConfirmDialogUnlimitedNumberItemsData> {
        return VehicleNumPlateCountriesEnum.entries.map { numPlate ->
            val countryModel =
                items.firstOrNull { it.id?.toLong() == numPlate.country.getCountryId() } ?: return@map null
            mapRegistrationCountryModelToMeeraConfirmDialogUnlimitedNumberItemsData(
                country = numPlate, countryModel = countryModel, isAuto = isAuto
            )
        }.filterNotNull()
    }

    fun mapRegistrationCountryModelToMeeraConfirmDialogUnlimitedNumberItemsData(
        country: VehicleNumPlateCountriesEnum, countryModel: RegistrationCountryModel, isAuto: Boolean
    ): MeeraConfirmDialogUnlimitedNumberItemsData {
        return MeeraConfirmDialogUnlimitedNumberItemsData(
            name = country.countryName,
            icon = country.countryFlag,
            action = MeeraVehicleCountryItem(
                numPlateMask = if (isAuto) country.autoNumPlate else country.motoNumPlate, countryModel = countryModel
            ),
        )
    }
}
