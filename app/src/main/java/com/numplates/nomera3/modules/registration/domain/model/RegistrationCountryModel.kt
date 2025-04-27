package com.numplates.nomera3.modules.registration.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RegistrationCountryModel(
    val name: String,
    val flag: String,
    val code: String? = null,
    val mask: String? = null,
    val id: Int? = null
) : Parcelable
