package com.meera.core.adapters.baserecycleradapter.example

import android.os.Bundle
import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.databinding.TestBtnLayoutBinding
import com.meera.core.databinding.TestDataLayoutBinding

const val ARG_UPDATE = "update data value"

class BtnVh(
    binding: TestBtnLayoutBinding,
    private val actionListener: (TestAction) -> Unit
) : BaseVH<TestRecyclerData, TestBtnLayoutBinding>(binding) {

    init {
        binding.btnTest.setOnClickListener {
            actionListener(TestAction.TestBtnClick)
        }
    }

    override fun bind(data: TestRecyclerData) = Unit
}


class DataVh(
    private val binding: TestDataLayoutBinding
) : BaseVH<TestRecyclerData, TestDataLayoutBinding>(binding) {

    override fun bind(data: TestRecyclerData) {
        data as TestRecyclerData.RecyclerData
        binding.name.text = data.name
        binding.value.text = data.valueRandom.toString()
    }

    override fun update(bundle: Bundle) {
        binding.value.text = bundle.getInt(ARG_UPDATE).toString()
    }
}
