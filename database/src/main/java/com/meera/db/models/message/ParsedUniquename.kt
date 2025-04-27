package com.meera.db.models.message

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ParsedUniquename(

    var text: String?,

    var spanData: List<UniquenameSpanData>,

    var shortText: String? = null,

    var lineCount: Int? = null,

    var showFullText: Boolean = true

) : Parcelable {

    fun addSpanData(spanDataItem: UniquenameSpanData) {
        val newList = mutableListOf<UniquenameSpanData>()
        newList.addAll(spanData)
        newList.add(spanDataItem)
        spanData = newList
    }

    fun deleteSpanDataById(id: String?) {
        val item = spanData.find { it.id == id }
        val newList = mutableListOf<UniquenameSpanData>()
        newList.addAll(spanData)
        newList.remove(item)
        spanData = newList
    }

}
