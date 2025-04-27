package com.numplates.nomera3.presentation.model.adaptermodel

import com.numplates.nomera3.presentation.view.adapter.checkedList.CheckedPagedAdapter

class CheckedListModel(
        var text: String = "",
        var isChecked: Boolean = false,
        val viewType: Int = CheckedPagedAdapter.VIEW_TYPE_CHECKED_ITEM,
        var dataType: Int? = null
){
    constructor(dataType: Int, isChecked: Boolean, viewType: Int): this(
            "", isChecked, viewType, dataType
    )
}