package com.numplates.nomera3.modules.places.domain.model

import android.net.Uri
import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel
import java.util.TimeZone

data class PlaceModel(
    val addressString: String,
    val location: CoordinatesModel,
    val name: String,
    val timeZone: TimeZone,
    val placeId: Long,
    val imageUri: Uri? = null
)
