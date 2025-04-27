package com.numplates.nomera3.modules.peoples.ui.onboarding

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.addClickWithDataColored
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentPeopleOnboardingSecondStepBinding
import com.numplates.nomera3.modules.peoples.ui.utils.RefreshOnboardingHeightHandler
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.presentation.router.IArgContainer

class MeeraPeopleOnboardingSecondStepFragment : MeeraBaseFragment(R.layout.meera_fragment_people_onboarding_second_step) {

    private val binding by viewBinding(MeeraFragmentPeopleOnboardingSecondStepBinding::bind)

    // Последовательность для spannable слова "Подтверди"
    private val wordsRangeDescriptionConfirm = 28..38
    private var supportUserId: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getArg()
        setSpannableConfirm()
    }

    override fun onResume() {
        super.onResume()
        updatePagerHeight()
    }

    private fun getArg() {
        val args = arguments ?: return
        this.supportUserId = args.getLong(BUNDLE_KEY_SUPPORT_USER_ID, -1)
    }

    private fun updatePagerHeight() {
        val rootFragment = requireParentFragment()
        if (rootFragment !is RefreshOnboardingHeightHandler) return
        binding.vgSecondPeopleOnboardingRoot.post {
            if (!isAdded) return@post
            val height = binding.vgSecondPeopleOnboardingRoot.height
            rootFragment.onRefreshPagerHeight(height)
        }
    }

    private fun setSpannableConfirm() {
        val spanText = SpannableStringBuilder(context?.getString(R.string.second_people_tab_description))
        spanText.addClickWithDataColored(
            range = wordsRangeDescriptionConfirm,
            color = ContextCompat.getColor(requireContext(), R.color.uiKitColorAccentPrimary)
        ) {
            addAdminSupportIdUser()
        }
        binding.tvPeopleOnboardingDescription.text = spanText
        binding.tvPeopleOnboardingDescription.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun addAdminSupportIdUser() {
        if (supportUserId == 0L) return
        findNavController().navigate(
            R.id.action_peoplesFragment_to_userInfoFragment,
            bundleOf(IArgContainer.ARG_USER_ID to supportUserId)
        )
        (parentFragment as? BottomSheetDialogFragment?)?.dismiss()
    }

    companion object {
        const val BUNDLE_KEY_SUPPORT_USER_ID = "bundleKeySupportUserId"
    }
}
