package com.numplates.nomera3.modules.screenshot.ui.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.meera.core.extensions.applyRoundedOutline
import com.meera.core.extensions.dp
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.utils.ApprovedIconSize
import com.meera.core.utils.NSnackbar
import com.meera.core.utils.TopAuthorApprovedUserModel
import com.meera.core.utils.enableTopContentAuthorApprovedUser
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentScreenshotPopupBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.screenshot.AmplitudeScreenshotActionTypeProperty
import com.numplates.nomera3.modules.communities.utils.shareLinkOutside
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPopupData
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPopupUiAction
import com.numplates.nomera3.modules.screenshot.ui.viewmodel.ScreenshotPopupViewModel
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment

class ScreenshotPopupFragment : BaseBottomSheetDialogFragment<FragmentScreenshotPopupBinding>() {

    private val viewModel: ScreenshotPopupViewModel by viewModels(
        factoryProducer = { App.component.getViewModelFactory() }
    )

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentScreenshotPopupBinding
        get() = FragmentScreenshotPopupBinding::inflate

    private val screenshotPopupData: ScreenshotPopupData? by lazy { arguments?.getParcelable(KEY_SCREENSHOT_POPUP_DATA) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logScreenshotPopupOpen()
        initViews()
    }

    private fun logScreenshotPopupOpen() {
        screenshotPopupData?.let { viewModel.handleUiAction(ScreenshotPopupUiAction.LogPopupOpen(it)) }
    }

    private fun initViews() {
        binding?.apply {
            loadImage(screenshotPopupData?.imageLink, screenshotPopupData?.eventIconRes)
            screenshotPopupData?.title?.let { initTitle(it) }
            screenshotPopupData?.description?.let { tvDescription.text = it }
            screenshotPopupData?.additionalInfo?.let { tvAdditionalInfo.text = it }
            screenshotPopupData?.buttonTextStringRes?.let { btnAction.text = getString(it) }
            vBackground.applyRoundedOutline(16f.dp)
            ivPhoto.applyRoundedOutline(16f.dp)
            btnCopyLink.setThrottledClickListener { copyLinkToClipboard() }
            btnAction.setThrottledClickListener { shareLink() }
            ivClose.setThrottledClickListener { dismiss() }
        }
    }

    private fun loadImage(link: String?, eventIconRes: Int?) {
        val imageView = binding?.ivPhoto ?: return
        when {
            link.isNullOrEmpty() && eventIconRes != null -> {
                imageView.setImageResource(eventIconRes)
            }
            !link.isNullOrEmpty() -> {
                Glide.with(imageView)
                    .load(link)
                    .error(R.drawable.ic_screenshot_popup_placeholder)
                    .into(imageView)
            }
        }
    }

    private fun initTitle(title: String) {
        binding?.tvTitle?.text = title
        binding?.tvTitle?.enableTopContentAuthorApprovedUser(
            params = TopAuthorApprovedUserModel(
                isVip = screenshotPopupData?.isVipUser ?: false,
                customIconTopContent = R.drawable.ic_approved_author_gold_10,
                approvedIconSize = ApprovedIconSize.SMALL,
                approved = screenshotPopupData?.isApprovedUser ?: false,
                interestingAuthor = screenshotPopupData?.isInterestingAuthor ?: false
            )
        )
    }

    private fun copyLinkToClipboard() {
        val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?
        clipboardManager?.setPrimaryClip(ClipData.newPlainText("Link", screenshotPopupData?.link))
        NSnackbar.with(act)
            .typeSuccess()
            .showOnTop(true)
            .iconTintColor(R.color.colorVipGreen)
            .text(getString(R.string.copy_link_success))
            .show()
        screenshotPopupData?.let { data ->
            viewModel.handleUiAction(ScreenshotPopupUiAction.LogShareAction(
                actionType = AmplitudeScreenshotActionTypeProperty.LINK,
                data = data
            ))
        }
    }

    private fun shareLink() {
        screenshotPopupData?.link?.let { shareLinkOutside(context, it) }
        screenshotPopupData?.let { data ->
            viewModel.handleUiAction(ScreenshotPopupUiAction.LogShareAction(
                actionType = AmplitudeScreenshotActionTypeProperty.SHARE,
                data = data
            ))
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dialogListener?.onDismissDialog()
        screenshotPopupData?.let { data ->
            viewModel.handleUiAction(ScreenshotPopupUiAction.LogShareAction(
                actionType = AmplitudeScreenshotActionTypeProperty.CLOSE,
                data = data
            ))
        }
    }

    companion object {
        private const val KEY_SCREENSHOT_POPUP_DATA = "KEY_SCREENSHOT_POPUP_DATA"

        fun newInstance(
            screenshotPopupData: ScreenshotPopupData
        ): ScreenshotPopupFragment {
            return ScreenshotPopupFragment().apply {
                arguments = bundleOf(
                    KEY_SCREENSHOT_POPUP_DATA to screenshotPopupData
                )
            }
        }
    }


}
