package com.numplates.nomera3.presentation.view.fragments.bottomfragment.postavatar

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.simpleName
import com.meera.core.extensions.toInt
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.numplates.nomera3.databinding.MeeraBottomSheetPostAvatarBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeAlertPostWithNewAvatarValuesActionType
import com.numplates.nomera3.presentation.model.enums.CreateAvatarPostEnum.NOT_PUBLIC
import com.numplates.nomera3.presentation.model.enums.CreateAvatarPostEnum.PRIVATE_ROAD

class MeeraPostAvatarBottomSheetFragment : UiKitBottomSheetDialog<MeeraBottomSheetPostAvatarBinding>() {

    val photoUri: String by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getString(ARG_PHOTO_URI) ?: throw IllegalArgumentException("ARG_PHOTO_URI is null")
    }

    val animation: String? by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getString(ARG_PHOTO_ANIMATION)
    }

    private var publishPostValue = false
    private var actionType = AmplitudeAlertPostWithNewAvatarValuesActionType.CLOSE

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraBottomSheetPostAvatarBinding
        get() = MeeraBottomSheetPostAvatarBinding::inflate

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(false)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding?.let { existBinding ->
            existBinding.ivProfilePhoto.setImageURI(Uri.parse(photoUri))

            rootBinding?.ivBottomSheetDialogClose?.setThrottledClickListener {
                publishPostValue = false
                actionType = AmplitudeAlertPostWithNewAvatarValuesActionType.CLOSE
                this@MeeraPostAvatarBottomSheetFragment.dismiss()
            }

            existBinding.scPostEverytime.setCellRightElementChecked(true)
            existBinding.tvBtnNoThanks.setThrottledClickListener {
                publishPostValue = false
                actionType = AmplitudeAlertPostWithNewAvatarValuesActionType.NO_THANKS
                this@MeeraPostAvatarBottomSheetFragment.dismiss()
            }

            existBinding.tvBtnPublish.setThrottledClickListener {
                publishPostValue = true
                actionType = AmplitudeAlertPostWithNewAvatarValuesActionType.PUBLISH
                this@MeeraPostAvatarBottomSheetFragment.dismiss()
            }

            existBinding.scPostEverytime.cellCityText = false
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onFinish(publishPostValue, actionType)
    }

    private fun onFinish(publishPost: Boolean, amplitudeActionType: AmplitudeAlertPostWithNewAvatarValuesActionType) {
        (parentFragment as? PostAvatarAlertListener)?.let {
            contentBinding?.let { existBinding ->
                val createAvatarPost = if (publishPost) PRIVATE_ROAD.state else NOT_PUBLIC.state

                val saveSettings = existBinding.scPostEverytime.isCheckButton.toInt()

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
            MeeraPostAvatarBottomSheetFragment().apply {
                arguments = bundleOf(ARG_PHOTO_URI to photoPath, ARG_PHOTO_ANIMATION to animation)
            }
    }
}
