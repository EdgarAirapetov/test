package com.numplates.nomera3.presentation.view.fragments.market.configurevehicle

import android.os.Parcelable
import com.numplates.nomera3.data.network.market.Field
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VehicleParamFillModel (
        val mode:Int,
        val typeId:Int,
        val brandId: Int?,  // optional if mode != model
        val field: Field? // optional if mode != field
):Parcelable