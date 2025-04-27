package com.numplates.nomera3.modules.baseCore.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** TODO https://nomera.atlassian.net/browse/BR-18804
 * Избавиться от Parcelable
 */
@Parcelize
data class CoordinatesModel(
    val lat: Double,
    val lon: Double
) : Parcelable
