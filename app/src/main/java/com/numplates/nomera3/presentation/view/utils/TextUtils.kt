package com.numplates.nomera3.presentation.view.utils

import com.meera.core.utils.text.ageCityFormattedText
import com.meera.db.models.userprofile.UserSimple

// !!! Метод жестко привязан к модели UserSimple
// returns string
// "$age, $city"
// "$city"
// "$age"
// or empty
fun getAgeCityFormattedText(user: UserSimple): String {
    val age: Long? = user.birthday
    val city: String? = user.city?.name
    return ageCityFormattedText(age, city)
}
