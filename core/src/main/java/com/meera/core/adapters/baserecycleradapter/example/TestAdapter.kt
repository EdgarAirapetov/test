package com.meera.core.adapters.baserecycleradapter.example

import android.os.Bundle
import android.view.ViewGroup
import com.meera.core.adapters.baserecycleradapter.BaseAsyncAdapter
import com.meera.core.adapters.baserecycleradapter.BaseDiffUtil
import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.adapters.baserecycleradapter.toBinding


sealed class TestAction {
    object TestBtnClick : TestAction()
}

class TestAdapter(
    private val actionListener: (TestAction) -> Unit
) : BaseAsyncAdapter<String, TestRecyclerData>(MyDif()) {

    override fun getHolderType(viewType: Int, parent: ViewGroup): BaseVH<TestRecyclerData, *> {
        return when (viewType) {
            TYPE_DATA -> DataVh(parent.toBinding())
            TYPE_BTN -> BtnVh(parent.toBinding(), actionListener)
            else -> throw RuntimeException("Missing data adapter type")
        }
    }
}

/**
 * Переопределяется если есть необходимость в пейлоадах
 * */
class MyDif : BaseDiffUtil<String, TestRecyclerData>() {

    override fun getChangePayload(oldItem: TestRecyclerData, newItem: TestRecyclerData): Any? {
        if (oldItem.getItemId() == newItem.getItemId()) {
            return when (oldItem) {
                is TestRecyclerData.RecyclerData -> handleRecyclerDataPayload(oldItem, newItem)
                else -> throw RuntimeException("Not implemented change payload for current type")
            }
        }
        return super.getChangePayload(oldItem, newItem)
    }

    private fun handleRecyclerDataPayload(
        oldItem: TestRecyclerData,
        newItem: TestRecyclerData
    ): Any? {

        oldItem as TestRecyclerData.RecyclerData
        newItem as TestRecyclerData.RecyclerData

        return if (oldItem.valueRandom == newItem.valueRandom) {
            super.getChangePayload(oldItem, newItem)
        } else {
            Bundle().also {
                it.putInt(ARG_UPDATE, newItem.valueRandom)
            }
        }
    }
}
