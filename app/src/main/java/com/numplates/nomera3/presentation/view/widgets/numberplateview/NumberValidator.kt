package com.numplates.nomera3.presentation.view.widgets.numberplateview

import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.CountryFilterItem
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.getCountryId

private const val CAR = 1
private const val MOTO = 2

fun getNumberPlateEnum(vehicleId: Int, countryId: Long): NumberPlateEnum? {
    return when {
        vehicleId == CAR
                && countryId == CountryFilterItem.RUSSIA.getCountryId() -> {
            NumberPlateEnum.RU_AUTO
        }
        vehicleId == MOTO
                && countryId == CountryFilterItem.RUSSIA.getCountryId() -> {
            NumberPlateEnum.RU_MOTO
        }

        vehicleId == CAR
                && countryId == CountryFilterItem.KAZAKHSTAN.getCountryId() -> {
            NumberPlateEnum.KZ_AUTO
        }
        vehicleId == MOTO
                && countryId == CountryFilterItem.KAZAKHSTAN.getCountryId() -> {
            NumberPlateEnum.KZ_MOTO
        }

        vehicleId == CAR
                && countryId == CountryFilterItem.BELARUS.getCountryId() -> {
            NumberPlateEnum.BY_AUTO
        }
        vehicleId == MOTO
                && countryId == CountryFilterItem.BELARUS.getCountryId() -> {
            NumberPlateEnum.BY_MOTO
        }

        vehicleId == CAR
                && countryId == CountryFilterItem.ARMENIA.getCountryId() -> {
            NumberPlateEnum.AM_AUTO
        }
        vehicleId == MOTO
                && countryId == CountryFilterItem.ARMENIA.getCountryId() -> {
            NumberPlateEnum.AM_MOTO
        }

        vehicleId == CAR
                && countryId == CountryFilterItem.GEORGIA.getCountryId() -> {
            NumberPlateEnum.GE_AUTO
        }
        vehicleId == MOTO
                && countryId == CountryFilterItem.GEORGIA.getCountryId() -> {
            NumberPlateEnum.GE_MOTO
        }

        vehicleId == CAR
                && countryId == CountryFilterItem.UKRAINE.getCountryId() -> {
            NumberPlateEnum.UA_AUTO
        }
        vehicleId == MOTO
                && countryId == CountryFilterItem.UKRAINE.getCountryId() -> {
            NumberPlateEnum.UA_AUTO
        }
        else -> return null
    }
}

fun validateNumberForSearch(
    vehicleId: Int,
    number: String,
    countryId: Long
): Boolean {
    val plate = getNumberPlateEnum(vehicleId, countryId) ?: return false
    var patternLen = 0

    patternLen += plate.numPattern.length
    if (plate.prefixPattern != null) {
        patternLen += plate.prefixPattern?.length ?: 0
    }
    if (plate.suffixPattern != null) {
        patternLen += plate.suffixPattern?.length ?: 0
    }
    if (plate.regionPattern != null) {
        patternLen += plate.regionPattern?.length ?: 0
    }
    if (plate == NumberPlateEnum.RU_AUTO) {
        return number.length >= patternLen - 1
    }
    return number.length >= patternLen
}
