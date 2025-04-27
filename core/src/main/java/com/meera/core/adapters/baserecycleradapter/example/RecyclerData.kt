package com.meera.core.adapters.baserecycleradapter.example

import com.meera.core.adapters.baserecycleradapter.RecyclerData

const val TYPE_DATA = 1
const val TYPE_BTN = 2

sealed interface TestRecyclerData : RecyclerData<String, TestRecyclerData> {

    object ListBnt : TestRecyclerData {
        override fun getItemId() = ""
        override fun contentTheSame(newItem: TestRecyclerData) = true
        override fun itemViewType() = TYPE_BTN
    }

    data class RecyclerData(
        val id: Long,
        val name: String,
        val valueRandom: Int
    ) : TestRecyclerData {
        override fun getItemId() = id.toString()
        override fun contentTheSame(newItem: TestRecyclerData) = this.equals(newItem)
        override fun itemViewType() = TYPE_DATA
    }

}
