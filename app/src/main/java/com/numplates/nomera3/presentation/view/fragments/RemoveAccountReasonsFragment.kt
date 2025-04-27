package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.numplates.nomera3.Act
import com.numplates.nomera3.databinding.FragmentAccountRemoveReasonsBinding
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.view.adapter.RemoveAccountReasonsAdapter
import com.meera.core.extensions.click
import com.meera.core.extensions.getStatusBarHeight
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_REMOVE_ACCOUNT_REASON_ID

class RemoveAccountReasonsFragment : BaseFragmentNew<FragmentAccountRemoveReasonsBinding>() {

    private var currentReasonId: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        binding?.statusBarRemoveAccount?.layoutParams?.height = context.getStatusBarHeight()
        binding?.backArrow?.click {
            act?.onBackPressed()
        }
        binding?.recycler?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = RemoveAccountReasonsAdapter(
                recyclerView = this,
                onReasonClicked = ::onReasonClicked,
                onButtonClicked = ::onButtonClicked
            )
        }
    }

    private fun onReasonClicked(reasonId: Int) {
        currentReasonId = reasonId
    }

    private fun onButtonClicked() {
        add(
            DeleteProfileFragment(),
            Act.LIGHT_STATUSBAR,
            Arg(ARG_REMOVE_ACCOUNT_REASON_ID, currentReasonId)
        )
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAccountRemoveReasonsBinding
        get() = FragmentAccountRemoveReasonsBinding::inflate

}
