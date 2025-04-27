package com.numplates.nomera3.modules.registration.ui.avatar

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.cell.CellPosition
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraAvatarSelectionFragmentBinding
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment

class MeeraSelectAvatarBottomSheetDialog(
    val photoClickListener: PhotoSelectorDismissListener,
    val fromScreen: FromScreen = FromScreen.PROFILE
) :
    BaseBottomSheetDialogFragment<MeeraAvatarSelectionFragmentBinding>(),
    BasePermission by BasePermissionDelegate(),
    BaseLoadImages by BaseLoadImagesDelegate() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> MeeraAvatarSelectionFragmentBinding
        get() = MeeraAvatarSelectionFragmentBinding::inflate

    private var bottomSheet: FrameLayout? = null

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            bottomSheet =
                bottomSheetDialog.findViewById(R.id.design_bottom_sheet) as FrameLayout?
            bottomSheet?.let {
                val bottomSheetBehavior = BottomSheetBehavior.from(it)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetBehavior.skipCollapsed = true
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.selectPhoto?.setThrottledClickListener {
            photoClickListener.selectPhoto()
            dismiss()
        }
        binding?.selectAvatar?.setThrottledClickListener {
            photoClickListener.selectAvatar()
            dismiss()
        }
        binding?.navViewAvatarSelection?.closeButtonClickListener = {
            dismiss()
        }
        setupState()
    }

    private fun setupState() {
        when (fromScreen) {
            FromScreen.PROFILE -> {
                binding?.selectGenerate?.gone()
                binding?.selectAvatar?.setTitleValue(getString(R.string.create_avatar_action))
                binding?.selectAvatar?.cellPosition = CellPosition.BOTTOM
            }
            FromScreen.REGISTRATION -> {
                binding?.selectGenerate?.visible()
                binding?.selectAvatar?.setTitleValue(getString(R.string.meera_create_in_constructor))
                binding?.selectAvatar?.cellPosition = CellPosition.MIDDLE
                binding?.selectGenerate?.cellPosition = CellPosition.BOTTOM
                binding?.selectGenerate?.setThrottledClickListener {
                    photoClickListener.generateAvatar()
                    dismiss()
                }
            }
        }
    }

    interface PhotoSelectorDismissListener {
        fun selectPhoto()
        fun selectAvatar()
        fun generateAvatar() = Unit
    }

    enum class FromScreen {
        PROFILE, REGISTRATION
    }
}
