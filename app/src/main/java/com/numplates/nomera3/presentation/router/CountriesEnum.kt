package com.numplates.nomera3.presentation.router

import com.numplates.nomera3.R


enum class CountriesEnum(
        val code: String,
        val hint: String,
        val id: Int,
        val flag: Int,
        val title: Int) {


    RU(
            code = "+7",
            hint = "__________",
            id = 3159,
            flag = R.drawable.country_ru,
            title = R.string.general_russia),

    UA(
            code = "+380",
            hint = "_________",
            id = 9908,
            flag = R.drawable.country_ua,
            title = R.string.general_ukraine),


    BY(
            code = "+375",
            hint = "_________",
            id = 248,
            flag = R.drawable.country_by,
            title = R.string.general_belarus),


    GE(
            code = "+995",
            hint = "_________",
            id = 1280,
            flag = R.drawable.country_ge,
            title = R.string.general_georgia),


    KZ(
            code = "+7",
            hint = "__________",
            id = 1894,
            flag = R.drawable.country_kz,
            title = R.string.general_kazakhstan),


    AM(
            code = "+374",
            hint = "________",
            id = 245,
            flag = R.drawable.country_am,
            title = R.string.general_armenia);

}
