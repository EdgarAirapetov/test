package com.numplates.nomera3.modules.redesign.fragments.secondary

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraChatDetailsFragmentBinding
import com.numplates.nomera3.modules.redesign.dialog.MeeraBottomSheet
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.util.NavigationManager

class MeeraChatDetailsFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_chat_details_fragment,
    behaviourConfigState = ScreenBehaviourState.Full
) {

    private val binding by viewBinding(MeeraChatDetailsFragmentBinding::bind)

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textViewOpenBottomSheet.setThrottledClickListener {
            MeeraBottomSheet().show(childFragmentManager, null)
        }

        binding.textViewOpenBottomFlow.setThrottledClickListener {
            val navManager = NavigationManager.getManager()
            navManager.initGraph(R.navigation.bottom_test_flow)
        }

        binding.btnBack.setThrottledClickListener {
            findNavController().popBackStack()
        }
    }

}
