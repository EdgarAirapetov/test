package com.numplates.nomera3.modules.avatar

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.noomeera.nmravatarssdk.NMRAvatarsSDK
import com.noomeera.nmravatarssdk.REQUEST_NMR_BACK_PRESSED
import com.noomeera.nmravatarssdk.REQUEST_NMR_KEY_AVATAR
import com.noomeera.nmravatarssdk.data.AvatarParams
import com.noomeera.nmravatarssdk.ui.MeeraAvatarEditorFragment
import com.numplates.nomera3.AVATAR_QUALITY_HIGH
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_AVATAR_STATE

class MeeraContainerAvatarFragment :
    MeeraBaseDialogFragment(R.layout.fragment_container_avatar, ScreenBehaviourState.Full) {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private var avatarState: String? = null
    private var avatarEditorFragment: MeeraAvatarEditorFragment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        avatarState = arguments?.getString(ARG_AVATAR_STATE)

        if (!NMRAvatarsSDK.isSdkReady(requireContext())) {
            findNavController().popBackStack()
        } else {
            openCreateAvatarFragment()
            observeChanges()
        }
    }

    private fun openCreateAvatarFragment() {
        avatarEditorFragment = NMRAvatarsSDK.createMeeraAvatarFragment(AvatarParams(avatarState, AVATAR_QUALITY_HIGH))
        avatarEditorFragment?.let {
            childFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, it)
                commit()
            }
            childFragmentManager.setFragmentResultListener(
                REQUEST_NMR_KEY_AVATAR, viewLifecycleOwner
            ) { requestKey, result ->
                setFragmentResult(REQUEST_NMR_KEY_AVATAR, result)
                findNavController().popBackStack()
            }
            childFragmentManager.setFragmentResultListener(REQUEST_NMR_BACK_PRESSED, viewLifecycleOwner) { _, _ ->
                exitScreen()
            }
        }
    }

    private fun observeChanges() {
        setFragmentResultListener(REQUEST_NMR_BACK_PRESSED) { _, _ ->
            exitScreen()
        }
    }

    private fun exitScreen() {
        findNavController().popBackStack()
    }

    fun onBackPressed() {
        avatarEditorFragment?.onHandleBackPress()
    }

}
