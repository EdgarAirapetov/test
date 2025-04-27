package com.numplates.nomera3.modules.complains.ui

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.numplates.nomera3.modules.complains.ui.model.UserComplainUiModel
import timber.log.Timber

// for example complain to user or group chat etc
const val KEY_COMPLAIN_TYPE = "key_complain_type"

// key to send ui complaint object
const val KEY_EXTRA_USER_COMPLAIN = "key_extra_user_complain"

// flag which marks if the result should be sent from complaint details bottom menu.
// result will not be send if the value is 'false'.
const val KEY_COMPLAINT_SEND_RESULT = "key_complaint_send_result"

// user id
const val KEY_COMPLAINT_USER_ID = "key_complaint_user_id"

// room id
const val KEY_COMPLAINT_ROOM_ID = "key_complaint_room_id"

// amplitude 'where' property for analytics
const val KEY_COMPLAINT_WHERE_VALUE = "key_complaint_where_value"
const val KEY_COMPLAINT_MOMENT_ID = "key_complaint_moment_id"

// send true if user dismissed | cancelled complaints flow
const val KEY_COMPLAIN_ON_USER_BLOCK = "key_complain_on_user_block"

// send complaint reason id to the sender
const val KEY_COMPLAIN_ON_USER_REPORT = "key_complain_on_user_report"

// send user id as a result through listener
private const val KEY_COMPLAIN_ON_USER_RESULT = "key_complain_on_user_result"

private const val REQUEST_CODE_CHANGE_REASON = "request_code_complains_change_reason"
private const val REQUEST_CODE_ADDITIONAL_ACTION = "request_code_complains_additional_action"
private const val REQUEST_CODE_DIALOG_CHAIN = "request_code_complains_dialog_chain"

class ComplainsNavigator(private val activity: FragmentActivity) {

    fun registerChangeReasonListener(lifecycleOwner: LifecycleOwner, listener: (UserComplainUiModel) -> Unit) {
        activity.supportFragmentManager.setFragmentResultListener(
            REQUEST_CODE_CHANGE_REASON,
            lifecycleOwner
        ) { _, bundle ->
            listener.invoke(bundle.getSerializable(KEY_EXTRA_USER_COMPLAIN) as UserComplainUiModel)
        }
    }

    fun unregisterChangeReasonListener() {
        activity.supportFragmentManager.clearFragmentResultListener(REQUEST_CODE_CHANGE_REASON)
    }

    fun sendChangeReasonResult(complain: UserComplainUiModel) {
        activity.supportFragmentManager.setFragmentResult(
            REQUEST_CODE_CHANGE_REASON,
            bundleOf(KEY_EXTRA_USER_COMPLAIN to complain)
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun registerAdditionalActionListener(lifecycleOwner: LifecycleOwner, listener: (Result<Long>) -> Unit) {
        activity.supportFragmentManager.setFragmentResultListener(
            REQUEST_CODE_ADDITIONAL_ACTION,
            lifecycleOwner
        ) { _, bundle ->
            listener.invoke(bundle.getSerializable(KEY_COMPLAIN_ON_USER_RESULT) as Result<Long>)
        }
    }

    fun unregisterAdditionalActionListener() {
        activity.supportFragmentManager.clearFragmentResultListener(REQUEST_CODE_ADDITIONAL_ACTION)
    }

    fun sendAdditionalActionResult(result: Result<Long>) {
        activity.supportFragmentManager.setFragmentResult(
            REQUEST_CODE_ADDITIONAL_ACTION,
            bundleOf(KEY_COMPLAIN_ON_USER_RESULT to result)
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun registerDialogChainListener(lifecycleOwner: LifecycleOwner, listener: (Bundle) -> Unit) {
        activity.supportFragmentManager.setFragmentResultListener(
            REQUEST_CODE_DIALOG_CHAIN,
            lifecycleOwner
        ) { _, bundle -> listener.invoke(bundle) }
    }

    fun unregisterDialogChainListener() {
        activity.supportFragmentManager.clearFragmentResultListener(REQUEST_CODE_DIALOG_CHAIN)
    }

    fun sendDialogChainResult(complaintReasonId: Int = -1, dismissed: Boolean = false) {
        Timber.d("sendDialogChainResult; complaintReasonId: $complaintReasonId, dismissed: $dismissed,")
        activity.supportFragmentManager.setFragmentResult(
            REQUEST_CODE_DIALOG_CHAIN,
            bundleOf(
                KEY_COMPLAIN_ON_USER_REPORT to complaintReasonId,
                KEY_COMPLAIN_ON_USER_BLOCK to dismissed,
            )
        )
    }
}
