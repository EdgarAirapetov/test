package com.numplates.nomera3.modules.complains.ui.reason

import android.content.DialogInterface
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.complains.ui.ComplainsNavigator
import com.numplates.nomera3.modules.complains.ui.ComplaintFlowInteraction
import com.numplates.nomera3.modules.complains.ui.ComplaintFlowResult
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_MOMENT_ID
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_ROOM_ID
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_SEND_RESULT
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_USER_ID
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_WHERE_VALUE
import com.numplates.nomera3.modules.user.BaseListFragmentsBottomSheet
import com.meera.core.base.BaseFragment
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAIN_TYPE

/**
 * Первый экран списка жалоб на пользователя
 */
class UserComplainReasonBottomSheet private constructor() : BaseListFragmentsBottomSheet() {

    private val complainsNavigator by lazy(LazyThreadSafetyMode.NONE) { ComplainsNavigator(requireActivity()) }

    override fun getRootFragment(): BaseFragment = UserComplainReasonFragment()

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        getComplaintFlowInteraction()?.finishComplaintFlow(ComplaintFlowResult.CANCELLED)
    }

    private fun getComplaintFlowInteraction(): ComplaintFlowInteraction? = act

    companion object {
        private const val TAG = "user_complain_bottom_sheet"

        fun showInstance(
            fragmentManager: FragmentManager,
            userId: Long,
            where: AmplitudePropertyWhere,
            momentId: Long? = null,
            roomId: Long? = null,
            sendResult: Boolean = true,
            complainType: ComplainType = ComplainType.USER
        ) {
            UserComplainReasonBottomSheet().apply {
                arguments = bundleOf(
                    KEY_COMPLAIN_TYPE to complainType.key,
                    KEY_COMPLAINT_USER_ID to userId,
                    KEY_COMPLAINT_WHERE_VALUE to where,
                    KEY_COMPLAINT_ROOM_ID to roomId,
                    KEY_COMPLAINT_SEND_RESULT to sendResult,
                    KEY_COMPLAINT_MOMENT_ID to momentId
                )
                show(fragmentManager, TAG)
            }
        }
    }

    override fun closeBottomSheet() {
        complainsNavigator.sendDialogChainResult(dismissed = true)
        super.closeBottomSheet()
    }

    override fun onCancel(dialog: DialogInterface) {
        complainsNavigator.sendDialogChainResult(dismissed = true)
        super.onCancel(dialog)
    }
}

enum class ComplainType(val key: Int) {
    USER(1), CHAT(2), MOMENT(3)
}
