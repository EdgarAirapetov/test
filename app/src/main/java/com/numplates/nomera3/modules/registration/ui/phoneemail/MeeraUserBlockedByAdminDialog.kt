package com.numplates.nomera3.modules.registration.ui.phoneemail

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.parcelable
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setVisible
import com.meera.core.extensions.writeToTechSupport
import com.meera.core.utils.convertUnixDate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraDialogUserBlockedByAdminBinding
import com.numplates.nomera3.modules.redesign.MeeraAct
import kotlinx.parcelize.Parcelize

class MeeraUserBlockedByAdminDialog : UiKitBottomSheetDialog<MeeraDialogUserBlockedByAdminBinding>() {

    val headerDialogType: HeaderDialogType by lazy {
        arguments?.parcelable(KEY_BLOCK_HEADER_TYPE) ?: HeaderDialogType.BlockedProfileType
    }
    val blockReason by lazy { arguments?.getString(KEY_BLOCK_REASON) ?: String.empty() }
    val blockDateUnixTimeSec by lazy { arguments?.getLong(KEY_BLOCK_DATE) ?: 0L }
    private val act: MeeraAct by lazy { activity as MeeraAct }

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraDialogUserBlockedByAdminBinding
        get() = MeeraDialogUserBlockedByAdminBinding::inflate

    override fun createDialogState(): UiKitBottomSheetDialogParams {
        val dialogTitle = initDialogTitle()
        return UiKitBottomSheetDialogParams(
            labelText = dialogTitle,
            needShowToolbar = true,
            needShowGrabberView = true,
            needShowCloseButton = true
        )
    }

    private fun initDialogTitle(): String {
        return when (headerDialogType) {
            HeaderDialogType.GroupRoadType -> getString(R.string.meera_publishing_forbidden)
            HeaderDialogType.MainRoadType -> getString(R.string.meera_action_impossible)
            else -> getString(R.string.profile_blocked_str)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        contentBinding?.apply {
            tvMainRoadForbidden.setVisible(headerDialogType is HeaderDialogType.MainRoadType)

            if (blockReason.isNotEmpty()) {
                tvBlockReason.text = blockReason
            } else {
                tvBlockReasonTitle.gone()
                tvBlockReason.gone()
            }

            if (blockDateUnixTimeSec != 0L) {
                tvBlockPeriod.text = getString(
                    R.string.popup_block_user_until,
                    convertUnixDate(blockDateUnixTimeSec)
                )
            } else {
                tvBlockPeriod.gone()
                tvBlockPeriodTitle.gone()
                tvBlockReason.setMargins(bottom = BLOCK_REASON_TEXT_MARGIN_BOTTOM.dp)
            }
            btnWrite.setThrottledClickListener {
                act?.writeToTechSupport() }
        }
    }

    companion object {
        private const val KEY_BLOCK_HEADER_TYPE = "KEY_BLOCK_HEADER_TYPE"
        private const val KEY_BLOCK_REASON = "KEY_BLOCK_REASON"
        private const val KEY_BLOCK_DATE = "KEY_BLOCK_DATE"
        private const val BLOCK_REASON_TEXT_MARGIN_BOTTOM = 8

        fun newInstance(
            blockReason: String,
            blockDate: Long,
            headerDialogType: HeaderDialogType? = null
        ): MeeraUserBlockedByAdminDialog {
            return MeeraUserBlockedByAdminDialog().apply {
                arguments = bundleOf(
                    KEY_BLOCK_REASON to blockReason,
                    KEY_BLOCK_DATE to blockDate,
                    KEY_BLOCK_HEADER_TYPE to headerDialogType
                )
            }
        }
    }
}

sealed class HeaderDialogType : Parcelable {
    @Parcelize
    data object MainRoadType : HeaderDialogType()

    @Parcelize
    data object GroupRoadType : HeaderDialogType()

    @Parcelize
    data object BlockedProfileType : HeaderDialogType()
}
