package com.numplates.nomera3.presentation.view.fragments.bottomfragment.postavatar

import android.app.Dialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.simpleName
import com.meera.core.extensions.toInt
import com.numplates.nomera3.databinding.BottomSheetPostAvatarBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeAlertPostWithNewAvatarValuesActionType
import com.numplates.nomera3.presentation.model.enums.CreateAvatarPostEnum.NOT_PUBLIC
import com.numplates.nomera3.presentation.model.enums.CreateAvatarPostEnum.PRIVATE_ROAD
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment

class PostAvatarBottomSheetFragment : BaseBottomSheetDialogFragment<BottomSheetPostAvatarBinding>() {

    val photoUri: String by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getString(ARG_PHOTO_URI) ?: throw IllegalArgumentException("ARG_PHOTO_URI is null")
    }

    val animation: String? by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getString(ARG_PHOTO_ANIMATION)
    }

    private var publishPostValue = false
    private var actionType = AmplitudeAlertPostWithNewAvatarValuesActionType.CLOSE


    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> BottomSheetPostAvatarBinding
        get() = BottomSheetPostAvatarBinding::inflate

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val onCreateDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return onCreateDialog.apply {
            behavior.peekHeight = resources.displayMetrics?.heightPixels ?: 0
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let { existBinding ->
            existBinding.ivProfilePhoto.setImageURI(Uri.parse(photoUri))

            existBinding.ivCloseIcon.setThrottledClickListener {
                publishPostValue = false
                actionType = AmplitudeAlertPostWithNewAvatarValuesActionType.CLOSE
                this@PostAvatarBottomSheetFragment.dismiss()
            }

            existBinding.tvBtnNoThanks.setThrottledClickListener {
                publishPostValue = false
                actionType = AmplitudeAlertPostWithNewAvatarValuesActionType.NO_THANKS
                this@PostAvatarBottomSheetFragment.dismiss()
            }

            existBinding.tvBtnPublish.setThrottledClickListener {
                publishPostValue = true
                actionType = AmplitudeAlertPostWithNewAvatarValuesActionType.PUBLISH
                this@PostAvatarBottomSheetFragment.dismiss()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onFinish(publishPostValue, actionType)
    }

    private fun onFinish(publishPost: Boolean, amplitudeActionType: AmplitudeAlertPostWithNewAvatarValuesActionType) {
        (parentFragment as? PostAvatarAlertListener)?.let {
            binding?.let { existBinding ->

                val createAvatarPost = if (publishPost) PRIVATE_ROAD.state else NOT_PUBLIC.state

                val saveSettings = existBinding.scPostEverytime.isChecked.toInt()

                it.onPublishOptionsSelected(
                    imagePath = photoUri,
                    animation = animation,
                    createAvatarPost = createAvatarPost,
                    saveSettings = saveSettings,
                    amplitudeActionType = amplitudeActionType
                )
            }
        }
    }

    fun show(manager: FragmentManager?) {
        val fragment = manager?.findFragmentByTag(simpleName)
        if (fragment != null)
            return
        manager?.let {
            super.show(manager, simpleName)
        }
    }

    companion object {
        const val ARG_PHOTO_URI = "argPhotoUri"
        const val ARG_PHOTO_ANIMATION = "argPhotoAnimation"

        fun getInstance(photoPath: String, animation: String?) =
            PostAvatarBottomSheetFragment().apply {
                arguments = bundleOf(ARG_PHOTO_URI to photoPath, ARG_PHOTO_ANIMATION to animation)
            }
    }
}
