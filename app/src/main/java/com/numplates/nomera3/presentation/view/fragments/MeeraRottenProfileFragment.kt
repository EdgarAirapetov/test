package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.LayoutRottenProfileBinding
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.registration.ui.RegistrationNavigationViewModel
import com.numplates.nomera3.presentation.utils.runOnUiThread
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed

class MeeraRottenProfileFragment : MeeraBaseFragment(
    layout = R.layout.layout_rotten_profile), IOnBackPressed {

    private val binding by viewBinding(LayoutRottenProfileBinding::bind)
    private val navigationViewModel by viewModels<RegistrationNavigationViewModel>(
        ownerProducer = { requireParentFragment() }
    )
    private val act: MeeraAct by lazy {
        requireActivity() as MeeraAct
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        binding?.buttonGoToRegistration?.setThrottledClickListener {
            navigationViewModel.registrationPhoneNext()
        }
    }

    override fun onBackPressed(): Boolean {
        act.getMeeraAuthenticationNavigator().backNavigateRecoveryScreenByBack()
        act.logOutWithDelegate{
            runOnUiThread {
                NavigationManager.getManager().logOutDoPassAndSetState()
            }
        }
        return true
    }
}
