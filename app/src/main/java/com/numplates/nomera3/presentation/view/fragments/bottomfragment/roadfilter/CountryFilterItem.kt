package com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter

enum class CountryFilterItem {
    ALL,
    AZERBAIJAN,
    ARMENIA,
    BELARUS,
    GEORGIA,
    KAZAKHSTAN,
    KYRGYZSTAN,
    MOLDOVA,
    RUSSIA,
    TAJIKISTAN,
    TURKMENISTAN,
    UZBEKISTAN,
    UKRAINE,
    TURKEY,
    OAE,
    THAILAND
}

fun CountryFilterItem.getCountryId(): Long? {
    return when (this) {
        CountryFilterItem.RUSSIA -> 3159L
        CountryFilterItem.UKRAINE -> 9908L
        CountryFilterItem.BELARUS -> 248L
        CountryFilterItem.GEORGIA -> 1280L
        CountryFilterItem.KAZAKHSTAN -> 1894L
        CountryFilterItem.ARMENIA -> 245L
        CountryFilterItem.AZERBAIJAN -> 81L
        CountryFilterItem.KYRGYZSTAN -> 2303L
        CountryFilterItem.MOLDOVA -> 2788L
        CountryFilterItem.TAJIKISTAN -> 9575L
        CountryFilterItem.TURKMENISTAN -> 9638L
        CountryFilterItem.UZBEKISTAN -> 9787L
        CountryFilterItem.OAE -> 582051L
        CountryFilterItem.TURKEY -> 9705L
        CountryFilterItem.THAILAND -> 582050L
        else -> null
    }
}

fun getCountryFilterItem(countryId: Long?): CountryFilterItem {
    return when (countryId) {
        3159L -> CountryFilterItem.RUSSIA
        9908L -> CountryFilterItem.UKRAINE
        248L -> CountryFilterItem.BELARUS
        1280L -> CountryFilterItem.GEORGIA
        1894L -> CountryFilterItem.KAZAKHSTAN
        245L -> CountryFilterItem.ARMENIA
        81L -> CountryFilterItem.AZERBAIJAN
        2303L -> CountryFilterItem.KYRGYZSTAN
        2788L -> CountryFilterItem.MOLDOVA
        9575L -> CountryFilterItem.TAJIKISTAN
        9638L -> CountryFilterItem.TURKMENISTAN
        9787L -> CountryFilterItem.UZBEKISTAN
        582051L -> CountryFilterItem.OAE
        9705L -> CountryFilterItem.TURKEY
        582050L -> CountryFilterItem.THAILAND
        else -> CountryFilterItem.ALL
    }
}
