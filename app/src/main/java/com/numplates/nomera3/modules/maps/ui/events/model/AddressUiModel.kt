package com.numplates.nomera3.modules.maps.ui.events.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddressUiModel(
    val name: String,
    val addressString: String,
    val location: LatLng,
    val timeZoneId: String,
    val distanceMeters: Float?
) : Parcelable
