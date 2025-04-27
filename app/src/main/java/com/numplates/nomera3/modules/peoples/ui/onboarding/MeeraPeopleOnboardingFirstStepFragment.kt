package com.numplates.nomera3.modules.peoples.ui.onboarding

import com.meera.core.base.viewbinding.viewBinding
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentPeopleOnboardingFirstStepBinding
import com.numplates.nomera3.modules.peoples.ui.utils.RefreshOnboardingHeightHandler
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment

class MeeraPeopleOnboardingFirstStepFragment : MeeraBaseFragment(R.layout.meera_fragment_people_onboarding_first_step) {

    private val binding by viewBinding(MeeraFragmentPeopleOnboardingFirstStepBinding::bind)

    override fun onResume() {
        super.onResume()
        updatePagerHeight()
    }

    private fun updatePagerHeight() {
        val rootFragment = requireParentFragment()
        if (rootFragment !is RefreshOnboardingHeightHandler) return
        binding.vgFirstPeopleOnboadridngRoot.post {
            if (!isAdded) return@post
            val height = binding.vgFirstPeopleOnboadridngRoot.height
            rootFragment.onRefreshPagerHeight(height)
        }
    }
}

