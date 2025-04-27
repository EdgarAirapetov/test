package com.numplates.nomera3.modules.screenshot.ui.fragment


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_FADE
import com.meera.core.extensions.applyRoundedOutline
import com.meera.core.extensions.dp
import com.meera.core.extensions.setThrottledClickListener
import com.meera.db.models.message.ParsedUniquename
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.people.ApprovedIconSize
import com.meera.uikit.widgets.people.TopAuthorApprovedUserModel
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentScreenshotPopupBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.screenshot.AmplitudeScreenshotActionTypeProperty
import com.numplates.nomera3.modules.communities.utils.shareLinkOutside
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPopupData
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPopupUiAction
import com.numplates.nomera3.modules.screenshot.ui.viewmodel.ScreenshotPopupViewModel
import com.numplates.nomera3.presentation.utils.setTextNoSpans
import com.numplates.nomera3.presentation.utils.spanTagsTextInPosts


private const val MEERA_SCREENSHOT_POPUP_DIALOG = "MeeraScreenshotPopupFragment"
private const val ELEVATION_SNACKBAR = 50F

/**
 * https://www.figma.com/design/E49e2Oygaplv9JTqCSFCgZ/People?node-id=2268-74129&t=Bw361rbIExITfs0g-1
 */
class MeeraScreenshotPopupFragment : UiKitBottomSheetDialog<MeeraFragmentScreenshotPopupBinding>() {

    private val viewModel: ScreenshotPopupViewModel by viewModels(
        factoryProducer = { App.component.getViewModelFactory() }
    )
    var data: ScreenshotPopupData? = null
    var action: (MeeraScreenshotPopupFragmentAction) -> Unit = {}
    private var infoSnackbar: UiKitSnackBar? = null

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraFragmentScreenshotPopupBinding
        get() = MeeraFragmentScreenshotPopupBinding::inflate

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun createDialogState(): UiKitBottomSheetDialogParams =
        UiKitBottomSheetDialogParams(labelText = context?.getString(R.string.link_is_better_than_screenshot))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        action.invoke(MeeraScreenshotPopupFragmentAction.OnCreateDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        infoSnackbar?.dismiss()
        action.invoke(MeeraScreenshotPopupFragmentAction.OnDismissDialog)
    }

    fun show(
        fm: FragmentManager,
        data: ScreenshotPopupData,
        action: (MeeraScreenshotPopupFragmentAction) -> Unit
    ): MeeraScreenshotPopupFragment {
        val dialog = MeeraScreenshotPopupFragment()
        dialog.data = data
        dialog.action = action
        dialog.isCancelable = this.isCancelable
        dialog.show(fm, MEERA_SCREENSHOT_POPUP_DIALOG)
        return dialog
    }

    private fun initViews() {
        contentBinding?.apply {
            loadImage(this, data?.imageLink, data?.eventIconRes)
            data?.title?.let { initTitle(this, it) }
            data?.description?.let { tvScreenshotContentDescription.text = it }
            data?.additionalInfo?.let { setupAdditionalInfo(data?.tagSpan, it) }
            data?.buttonTextStringRes?.let { btnScreenshotContentAction.text = getString(it) }
            vScreenshotContentBackground.applyRoundedOutline(16f.dp)
            ivScreenshotContentImage.applyRoundedOutline(16f.dp)
            btnScreenshotCopyLink.setThrottledClickListener { copyLinkToClipboard() }
            btnScreenshotContentAction.setThrottledClickListener { shareLink() }
        }
    }

    private fun initTitle(binding: MeeraFragmentScreenshotPopupBinding, title: String) {
        binding.tvScreenshotContentTitle.text = title
        binding.tvScreenshotContentTitle.enableTopContentAuthorApprovedUser(
            params = TopAuthorApprovedUserModel(
                approvedIconSize = ApprovedIconSize.SMALL,
                approved = data?.isApprovedUser ?: false,
                interestingAuthor = data?.isInterestingAuthor ?: false
            )
        )
    }

    private fun setupAdditionalInfo(tagSpan: ParsedUniquename?, additionalInfo: String) {
        tagSpan?.let { notNullSpan ->
            setupSpanText(notNullSpan)
        } ?: kotlin.run {
            contentBinding?.tvScreenshotContentAdditionalInfo?.setTextNoSpans(additionalInfo)
        }
    }

    private fun setupSpanText(tagSpan: ParsedUniquename) {
        contentBinding?.tvScreenshotContentAdditionalInfo?.let { tvText ->
            spanTagsTextInPosts(
                context = tvText.context,
                tvText = tvText,
                post = tagSpan
            )
        }
    }

    private fun loadImage(
        binding: MeeraFragmentScreenshotPopupBinding,
        link: String?,
        eventIconRes: Int?
    ) {
        when {
            link.isNullOrEmpty() && eventIconRes != null -> {
                binding.ivScreenshotContentImage.setImageResource(eventIconRes)
            }

            !link.isNullOrEmpty() -> {
                Glide.with(requireContext())
                    .load(link)
                    .circleCrop()
                    .error(R.drawable.ic_meera_screenshot_no_image_placeholder)
                    .into(binding.ivScreenshotContentImage)
            }
        }
    }

    private fun copyLinkToClipboard() {
        val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?
        clipboardManager?.setPrimaryClip(ClipData.newPlainText("Link", data?.link))

        data?.let { data ->
            viewModel.handleUiAction(
                ScreenshotPopupUiAction.LogShareAction(
                    actionType = AmplitudeScreenshotActionTypeProperty.LINK,
                    data = data
                )
            )
        }
        UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(R.string.copy_link_success),
                    avatarUiState = AvatarUiState.SuccessIconState,
                )
            )
        ).apply {
            animationMode = ANIMATION_MODE_FADE
            ViewCompat.setElevation(this.view, ELEVATION_SNACKBAR)
            val params = view.layoutParams as CoordinatorLayout.LayoutParams
            params.gravity = Gravity.TOP
            view.layoutParams = params
        }.show()
    }

    private fun shareLink() {
        data?.link?.let { shareLinkOutside(context, it) }
        data?.let { data ->
            viewModel.handleUiAction(
                ScreenshotPopupUiAction.LogShareAction(
                    actionType = AmplitudeScreenshotActionTypeProperty.SHARE,
                    data = data
                )
            )
        }
    }

}

sealed interface MeeraScreenshotPopupFragmentAction {
    data object OnCreateDialog : MeeraScreenshotPopupFragmentAction
    data object OnDismissDialog : MeeraScreenshotPopupFragmentAction
}
