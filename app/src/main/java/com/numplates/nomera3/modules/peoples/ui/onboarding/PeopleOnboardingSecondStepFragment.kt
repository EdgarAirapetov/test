package com.numplates.nomera3.modules.peoples.ui.onboarding

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.meera.core.extensions.addClickableText
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentPeopleOnboardingSecondStepBinding
import com.numplates.nomera3.modules.peoples.ui.utils.RefreshOnboardingHeightHandler
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment

class PeopleOnboardingSecondStepFragment : BaseFragmentNew<FragmentPeopleOnboardingSecondStepBinding>() {

    private var supportUserId: Long = -1

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPeopleOnboardingSecondStepBinding
        get() = FragmentPeopleOnboardingSecondStepBinding::inflate

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
        binding?.vgSecondPeopleOnboardingRoot?.post {
            if (!isAdded) return@post
            val height = binding?.vgSecondPeopleOnboardingRoot?.height ?: return@post
            rootFragment.onRefreshPagerHeight(height)
        }
    }

    private fun setSpannableConfirm() {
        binding?.tvPeopleOnboardingDescription?.text = requireContext().getString(R.string.second_people_tab_description).addClickableText(
            ContextCompat.getColor(requireContext(), R.color.ui_purple),
            context?.getString(R.string.second_people_tab_description_link_key).orEmpty(),
            ::addAdminSupportIdUser
        )
        binding?.tvPeopleOnboardingDescription?.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun addAdminSupportIdUser() {
        if (supportUserId == -1L) return
        add(
            UserInfoFragment(),
            Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
            Arg(IArgContainer.ARG_USER_ID, supportUserId)
        )
    }

    companion object {
        const val BUNDLE_KEY_SUPPORT_USER_ID = "bundleKeySupportUserId"
    }
}
