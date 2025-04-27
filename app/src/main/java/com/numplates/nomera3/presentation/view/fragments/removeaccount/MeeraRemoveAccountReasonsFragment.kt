package com.numplates.nomera3.presentation.view.fragments.removeaccount

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.safeNavigate
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentAccountRemoveReasonsMeeraBinding
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_REMOVE_ACCOUNT_REASON_ID

class MeeraRemoveAccountReasonsFragment : MeeraBaseDialogFragment(R.layout.fragment_account_remove_reasons_meera, ScreenBehaviourState.Full) {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(FragmentAccountRemoveReasonsMeeraBinding::bind)

    private var currentReasonId: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        binding.vNavView.backButtonClickListener = {
            findNavController().popBackStack()
        }
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = MeeraRemoveAccountReasonsAdapter(
                recyclerView = this,
                ::onActionListener
            )
        }
    }

    private fun onActionListener(action: ReasonFragmentAction) {
        when (action) {
            ReasonFragmentAction.ContinueButtonAction -> onButtonClicked()
            else -> onReasonClicked(action.reason)
        }
    }

    private fun onReasonClicked(reasonId: Int) {
        currentReasonId = reasonId
    }

    private fun onButtonClicked() {
        val args = bundleOf(ARG_REMOVE_ACCOUNT_REASON_ID to currentReasonId)
        findNavController().safeNavigate(R.id.action_meeraRemoveAccountReasonsFragment_to_meeraDeleteProfileFragment, args)
    }
}
