package com.numplates.nomera3.presentation.view.fragments.bottomfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.numplates.nomera3.databinding.FragmentBottomSheetCheckedListBinding
import com.numplates.nomera3.presentation.model.adaptermodel.CheckedListModel
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.numplates.nomera3.presentation.view.adapter.checkedList.CheckedListAdapter
import timber.log.Timber

class BottomSheetCheckedListFragment: BaseBottomSheetDialogFragment<FragmentBottomSheetCheckedListBinding>() {

    internal var clickListener: (MutableList<CheckedListModel>) -> Unit = { _ -> }

    private var selectedItems = mutableListOf<CheckedListModel>()
    private var header: String = ""

    private var adapter: CheckedListAdapter? = null


    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBottomSheetCheckedListBinding
        get() = FragmentBottomSheetCheckedListBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListeners()
        binding?.tvCheckedListHeader?.text = header
        initRecycler()
    }

    private fun initClickListeners() {
        binding?.cvCheckedListContinue?.setOnClickListener {
            clickListener.invoke(selectedItems)
            dismiss()
        }

        binding?.ivCheckedListClose?.setOnClickListener {
            dismiss()
        }

    }

    private fun initRecycler() {
        binding?.rvCheckedList?.layoutManager = LinearLayoutManager(context)
        binding?.rvCheckedList?.setHasFixedSize(true)

        adapter?.let {
            it.interactor = object: CheckedListAdapter.IAdapterInteractor{
                override fun onChecked(model: CheckedListModel) {
                    selectedItems.add(model)
                    Timber.d("$selectedItems")
                }

                override fun onUnCheck(model: CheckedListModel) {
                    selectedItems.remove(model)
                    Timber.d("$selectedItems")
                }
            }
            binding?.rvCheckedList?.adapter = it
        }

    }
}
