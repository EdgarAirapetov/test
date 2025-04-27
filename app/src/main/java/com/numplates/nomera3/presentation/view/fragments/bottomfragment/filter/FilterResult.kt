package com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter

import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter.FilterBottomSheet.Companion.MAX_FILTER_AGE
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter.FilterBottomSheet.Companion.MIN_FILTER_AGE

val ALL_COUNTRY = RegistrationCountryModel("","")

data class FilterResult(
    var filterType: FilterBottomSheet.Companion.FilterType =
        FilterBottomSheet.Companion.FilterType.PEOPLE_SEARCH,
    var countries: MutableSet<RegistrationCountryModel> = mutableSetOf(ALL_COUNTRY),
    var cities: MutableSet<City> = mutableSetOf(),
    var gender: FilterGender = FilterGender.ANY,
    var age: FilterAgeRange = FilterAgeRange()
) {

    fun setByDefault(){
        filterType = FilterBottomSheet.Companion.FilterType.PEOPLE_SEARCH
        countries = mutableSetOf(ALL_COUNTRY)
        cities = mutableSetOf()
        gender = FilterGender.ANY
        age = FilterAgeRange()
    }

    fun copy(
        countries: MutableSet<RegistrationCountryModel> = this.countries,
        cities: MutableSet<City> = this.cities,
        gender: FilterGender = this.gender,
        age: FilterAgeRange = this.age
    ): FilterResult {
        return FilterResult(
            countries = mutableSetOf<RegistrationCountryModel>().apply { addAll(countries) },
            cities = mutableSetOf<City>().apply { addAll(cities) },
            gender = gender,
            age = age.copy()
        )
    }
}

enum class FilterGender {
    ANY, MALE, FEMALE
}

data class FilterAgeRange(
    var start: Int = MIN_FILTER_AGE,
    var end: Int = MAX_FILTER_AGE
)

fun FilterResult?.isSomethingChanged(result: FilterResult?): Boolean {
    return when {
        this == null && result == null -> false
        this?.countries?.toTypedArray().contentToString() !=
                result?.countries?.toTypedArray().contentToString() -> true
        this?.cities?.toTypedArray().contentToString() !=
                result?.cities?.toTypedArray().contentToString() -> true
        this?.gender != result?.gender -> true
        this?.age?.start != result?.age?.start -> true
        this?.age?.end != result?.age?.end -> true
        else -> false
     }
}
