package com.numplates.nomera3.modules.purchase.ui.send

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraSuccessSendGiftFragmentBinding
import com.numplates.nomera3.modules.purchase.ui.gift.MeeraGiftsListFragment
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment

const val ARG_GIFT_IMAGE = "ARG_GIFT_IMAGE"

class MeeraSuccessSentGiftFragment : MeeraBaseFragment(
    layout = R.layout.meera_success_send_gift_fragment) {
    private val binding by viewBinding(MeeraSuccessSendGiftFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { args ->
            args.get(ARG_GIFT_IMAGE)?.let {
                binding?.ivGiftPicture?.loadGlide(it as String)
            }
        }

        binding?.vGiftDoneBtn?.setThrottledClickListener {
            findNavController().popBackStack()
            setFragmentResult(MeeraGiftsListFragment.SHOW_RATE_APP, bundleOf())
        }
    }
}
