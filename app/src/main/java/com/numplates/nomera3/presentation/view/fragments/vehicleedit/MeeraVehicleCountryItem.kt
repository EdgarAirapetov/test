package com.numplates.nomera3.presentation.view.fragments.vehicleedit

import com.meera.core.dialogs.unlimiteditem.MeeraConfirmDialogUnlimitedNumberItemsAction
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.presentation.view.widgets.numberplateview.NumberPlateEnum

data class MeeraVehicleCountryItem(val numPlateMask: NumberPlateEnum, val countryModel: RegistrationCountryModel) :
    MeeraConfirmDialogUnlimitedNumberItemsAction
