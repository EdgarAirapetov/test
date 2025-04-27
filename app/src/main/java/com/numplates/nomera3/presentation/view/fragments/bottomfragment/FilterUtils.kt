package com.numplates.nomera3.presentation.view.fragments.bottomfragment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import com.meera.core.extensions.dp
import com.meera.core.extensions.setMargins
import com.meera.uikit.widgets.chips.UiKitCheckableChipView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.CountryFilterItem


fun Context.createMeeraCountryFilterItemView(filterCountryItem: RegistrationCountryModel): View {
    val filterItemView = LayoutInflater.from(this)
        .inflate(R.layout.meera_item_filter_country_chip, null)
        .apply {
            tag = filterCountryItem
            layoutParams = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setMargins(
                start = 0,
                top = 12.dp,
                end = 12.dp,
                bottom = 0
            )
        } as UiKitCheckableChipView
    if (filterCountryItem.id == null) {
        filterItemView.text = getString(R.string.everyone)
    } else {
        filterItemView.text = filterCountryItem.name
    }
    return filterItemView
}

fun Context.createCountryFilterItemView(filterCountryItem: RegistrationCountryModel): View {

    val filterItemView = LayoutInflater
        .from(this)
        .inflate(R.layout.bottom_sheet_road_filter_country_item, null)
        .apply {
            tag = filterCountryItem
            layoutParams = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                32.dp
            )
            setMargins(0, 11.dp, 6.dp, 0)
        } as TextView
    if (filterCountryItem.id == null) {
        filterItemView.text = getString(R.string.road_post_all_countries)
    } else {
        filterItemView.text = filterCountryItem.name
    }
    return filterItemView
}


fun Context.createCountryFilterItemView(filterCountryItem: CountryFilterItem): View {
    fun getStringById(id: Int): String {
        return resources.getString(id)
    }

    val filterItemView = LayoutInflater
        .from(this)
        .inflate(R.layout.bottom_sheet_road_filter_country_item, null)
        .apply {
            tag = filterCountryItem
            layoutParams = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                32.dp
            )
            setMargins(0, 11.dp, 6.dp, 0)
        }

    when (filterCountryItem) {
        CountryFilterItem.ALL -> {
            filterItemView.setPadding(
                18.dp,
                filterItemView.paddingTop,
                18.dp,
                filterItemView.paddingBottom
            )

            getStringById(R.string.road_post_all_countries)
        }

        CountryFilterItem.RUSSIA -> getStringById(R.string.general_russia)
        CountryFilterItem.KAZAKHSTAN -> getStringById(R.string.general_kazakhstan)
        CountryFilterItem.BELARUS -> getStringById(R.string.general_belarus_filter)
        CountryFilterItem.ARMENIA -> getStringById(R.string.general_armenia)
        CountryFilterItem.GEORGIA -> getStringById(R.string.general_georgia)
        CountryFilterItem.UKRAINE -> getStringById(R.string.general_ukraine)
        CountryFilterItem.AZERBAIJAN -> getStringById(R.string.general_azerbaijan)
        CountryFilterItem.KYRGYZSTAN -> getStringById(R.string.general_kyrgyzstan)
        CountryFilterItem.MOLDOVA -> getStringById(R.string.general_moldova)
        CountryFilterItem.TAJIKISTAN -> getStringById(R.string.general_tajikistan)
        CountryFilterItem.TURKMENISTAN -> getStringById(R.string.general_turkmenistan)
        CountryFilterItem.UZBEKISTAN -> getStringById(R.string.general_uzbekistan)
        CountryFilterItem.TURKEY -> getStringById(R.string.general_turkey)
        CountryFilterItem.OAE -> getStringById(R.string.general_oae)
        CountryFilterItem.THAILAND -> getStringById(R.string.general_thailand)
    }.let { countryName: String ->
        filterItemView as TextView
        filterItemView.text = countryName
    }

    return filterItemView
}

fun getCountryFlag(countryFilterItem: CountryFilterItem): Int? {

    return when (countryFilterItem) {
        CountryFilterItem.RUSSIA -> R.drawable.ic_flag_of_russia
        CountryFilterItem.KAZAKHSTAN -> R.drawable.ic_flag_of_kazakhstan
        CountryFilterItem.BELARUS -> R.drawable.ic_flag_of_belarus
        CountryFilterItem.ARMENIA -> R.drawable.ic_flag_of_armenia
        CountryFilterItem.GEORGIA -> R.drawable.ic_flag_of_georgia
        CountryFilterItem.UKRAINE -> R.drawable.ic_flag_of_ukraine
        else -> null
    }
}

fun Context.getCountryString(countryFilterItem: CountryFilterItem): String {
    fun getStringById(id: Int): String {
        return resources.getString(id)
    }

    return when (countryFilterItem) {
        CountryFilterItem.ALL -> getStringById(R.string.road_post_all_countries)
        CountryFilterItem.RUSSIA -> getStringById(R.string.general_russia)
        CountryFilterItem.KAZAKHSTAN -> getStringById(R.string.general_kazakhstan)
        CountryFilterItem.BELARUS -> getStringById(R.string.general_belarus_filter)
        CountryFilterItem.ARMENIA -> getStringById(R.string.general_armenia)
        CountryFilterItem.GEORGIA -> getStringById(R.string.general_georgia)
        CountryFilterItem.UKRAINE -> getStringById(R.string.general_ukraine)
        CountryFilterItem.AZERBAIJAN -> getStringById(R.string.general_azerbaijan)
        CountryFilterItem.KYRGYZSTAN -> getStringById(R.string.general_kyrgyzstan)
        CountryFilterItem.MOLDOVA -> getStringById(R.string.general_moldova)
        CountryFilterItem.TAJIKISTAN -> getStringById(R.string.general_tajikistan)
        CountryFilterItem.TURKMENISTAN -> getStringById(R.string.general_turkmenistan)
        CountryFilterItem.UZBEKISTAN -> getStringById(R.string.general_uzbekistan)
        CountryFilterItem.TURKEY -> getStringById(R.string.general_turkey)
        CountryFilterItem.OAE -> getStringById(R.string.general_oae)
        CountryFilterItem.THAILAND -> getStringById(R.string.general_thailand)
    }
}
