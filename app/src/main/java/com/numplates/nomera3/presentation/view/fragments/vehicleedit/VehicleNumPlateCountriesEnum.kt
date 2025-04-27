package com.numplates.nomera3.presentation.view.fragments.vehicleedit

import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.CountryFilterItem
import com.numplates.nomera3.presentation.view.widgets.numberplateview.NumberPlateEnum

enum class VehicleNumPlateCountriesEnum(
    val country: CountryFilterItem,
    val countryName: Int, val countryFlag: Int, val autoNumPlate: NumberPlateEnum, val motoNumPlate: NumberPlateEnum
) {
    RU(
        country = CountryFilterItem.RUSSIA,
        countryName = R.string.general_russia,
        countryFlag = R.drawable.country_ru,
        autoNumPlate = NumberPlateEnum.RU_AUTO,
        motoNumPlate = NumberPlateEnum.RU_MOTO
    ),
    AM(
        country = CountryFilterItem.ARMENIA,
        countryName = R.string.general_armenia,
        countryFlag = R.drawable.country_am,
        autoNumPlate = NumberPlateEnum.AM_AUTO,
        motoNumPlate = NumberPlateEnum.AM_MOTO
    ),
    BY(
        country = CountryFilterItem.BELARUS,
        countryName = R.string.general_belarus,
        countryFlag = R.drawable.country_by,
        autoNumPlate = NumberPlateEnum.BY_AUTO,
        motoNumPlate = NumberPlateEnum.BY_MOTO
    ),
    GE(
        country = CountryFilterItem.GEORGIA,
        countryName = R.string.general_georgia,
        countryFlag = R.drawable.country_ge,
        autoNumPlate = NumberPlateEnum.GE_AUTO,
        motoNumPlate = NumberPlateEnum.GE_MOTO
    ),
    KZ(
        country = CountryFilterItem.KAZAKHSTAN,
        countryName = R.string.general_kazakhstan,
        countryFlag = R.drawable.country_kz,
        autoNumPlate = NumberPlateEnum.KZ_AUTO,
        motoNumPlate = NumberPlateEnum.KZ_MOTO
    ),
    UA(
        country = CountryFilterItem.UKRAINE,
        countryName = R.string.general_ukraine,
        countryFlag = R.drawable.country_ua,
        autoNumPlate = NumberPlateEnum.UA_AUTO,
        motoNumPlate = NumberPlateEnum.UA_MOTO
    ),
}
