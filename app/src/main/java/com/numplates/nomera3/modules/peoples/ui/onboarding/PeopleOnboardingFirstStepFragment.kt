package com.numplates.nomera3.modules.peoples.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import com.numplates.nomera3.databinding.FragmentPeopleOnboardingFirstStepBinding
import com.numplates.nomera3.modules.peoples.ui.utils.RefreshOnboardingHeightHandler
import com.numplates.nomera3.presentation.router.BaseFragmentNew

class PeopleOnboardingFirstStepFragment : BaseFragmentNew<FragmentPeopleOnboardingFirstStepBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPeopleOnboardingFirstStepBinding
        get() = FragmentPeopleOnboardingFirstStepBinding::inflate

    override fun onResume() {
        super.onResume()
        updatePagerHeight()
    }

    private fun updatePagerHeight() {
        val rootFragment = requireParentFragment()
        if (rootFragment !is RefreshOnboardingHeightHandler) return
        binding?.vgFirstPeopleOnboadridngRoot?.post {
            if (!isAdded) return@post
            val height = binding?.vgFirstPeopleOnboadridngRoot?.height ?: return@post
            rootFragment.onRefreshPagerHeight(height)
        }
    }
}
