package com.numplates.nomera3.modules.avatar

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import com.noomeera.nmravatarssdk.NMRAvatarsSDK
import com.noomeera.nmravatarssdk.REQUEST_NMR_BACK_PRESSED
import com.noomeera.nmravatarssdk.data.AvatarParams
import com.noomeera.nmravatarssdk.ui.AvatarEditorFragment
import com.numplates.nomera3.AVATAR_QUALITY_HIGH
import com.numplates.nomera3.AVATAR_QUALITY_LOW
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentContainerAvatarBinding
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_AVATAR_STATE
import com.numplates.nomera3.presentation.view.navigator.NavigatorViewPager

class ContainerAvatarFragment : BaseFragmentNew<FragmentContainerAvatarBinding>() {

    private var avatarState : String? = null
    private var avatarEditorFragment: AvatarEditorFragment? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentContainerAvatarBinding
        get() = FragmentContainerAvatarBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lockSwipeLeft()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        avatarState = arguments?.getString(ARG_AVATAR_STATE)

        if (!NMRAvatarsSDK.isSdkReady(requireContext())) {
            requireActivity().onBackPressed()
        } else {
            openCreateAvatarFragment()
            observeChanges()
        }

    }

    private fun openCreateAvatarFragment() {
        val quality = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) AVATAR_QUALITY_HIGH else AVATAR_QUALITY_LOW
        avatarEditorFragment = NMRAvatarsSDK.createAvatarFragment(AvatarParams(avatarState, quality))
        avatarEditorFragment?.let {
            childFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, it)
                commit()
            }
        }
    }

    private fun observeChanges() {
        setFragmentResultListener(REQUEST_NMR_BACK_PRESSED) { _, bundle ->
            exitScreen()
        }
    }

    private fun exitScreen() {
        act?.isSubscribeFloorFragment = true
        act?.navigatorViewPager?.setCurrentItem(act.navigatorViewPager.currentItem - 1, true)
    }

    private fun lockSwipeLeft() {
        act?.navigatorViewPager?.setAllowedSwipeDirection(NavigatorViewPager.SwipeDirection.NONE)
    }

    fun onBackPressed() {
        avatarEditorFragment?.onHandleBackPress()
    }


}
