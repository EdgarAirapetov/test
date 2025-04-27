package com.numplates.nomera3.presentation.model.adaptermodel

import android.os.Parcelable
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import kotlinx.android.parcel.Parcelize

data class ExpandedCheckedData(
        var header: String,
        var models: List<CheckedChildModel>
):ExpandableGroup<CheckedChildModel>(header, models)

@Parcelize
data class CheckedChildModel(
        val brandName: String
):Parcelable