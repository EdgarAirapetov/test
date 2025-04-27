package com.numplates.nomera3.modules.chat.ui.mapper

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import androidx.core.text.color
import androidx.core.text.toSpannable
import com.meera.core.utils.getDurationSeconds
import com.meera.uikit.widgets.chat.call.CallType
import com.numplates.nomera3.R
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.chat.ui.model.MessageMetadataUiModel
import com.numplates.nomera3.presentation.model.enums.CallStatusEnum
import javax.inject.Inject

/**
 * Local class. Used ONLY for mappers!
 */
internal data class CallData(
    val title: String,
    val isOutgoing: Boolean,
    val description: Spannable,
    val iconType: CallType,
)

class CallDataMapper @Inject constructor(
    private val context: Context,
    private val getUserUidUseCase: GetUserUidUseCase,
) {

    internal fun mapToCallData(metadata: MessageMetadataUiModel?): CallData {
        val callStatus = CallStatusEnum[metadata?.status ?: CallStatusEnum.STOPPED.status]
        val isOutgoing = metadata?.caller?.callerId == getUserUidUseCase.invoke()
        val callType = when {
            isOutgoing && callStatus == CallStatusEnum.STOPPED
                || isOutgoing && callStatus == CallStatusEnum.CALLING -> CallType.OUTGOING

            !isOutgoing && callStatus == CallStatusEnum.STOPPED
                || !isOutgoing && callStatus == CallStatusEnum.CALLING -> CallType.INCOMING

            callStatus == CallStatusEnum.MISSED -> CallType.MISSED
            callStatus == CallStatusEnum.DECLINED || callStatus == CallStatusEnum.REJECTED -> CallType.DECLINED
            else -> error("This status is not allowed for the view. status: $callStatus")
        }
        return CallData(
            title = titleString(isOutgoing),
            isOutgoing = isOutgoing,
            iconType = iconType(
                isOutgoing = isOutgoing,
                callStatus = callStatus,
            ),
            description = descriptionString(
                metadata = metadata,
                callType = callType,
                isOutgoing = isOutgoing,
            )
        )
    }

    private fun titleString(isOutgoing: Boolean): String {
        val stringRes = when (isOutgoing) {
            true -> R.string.outgoing_call
            else -> R.string.incoming_call
        }
        return context.getString(stringRes)
    }

    private fun descriptionString(
        metadata: MessageMetadataUiModel?,
        callType: CallType,
        isOutgoing: Boolean,
    ): Spannable {
        val builder = SpannableStringBuilder()
        when (callType) {
            CallType.OUTGOING,
            CallType.INCOMING -> builder.color(context.getColor(R.color.uiKitColorForegroundSecondary)) {
                val durationDescription = context.getString(
                    R.string.call_status_call_time,
                    getDurationSeconds(metadata?.callDuration ?: 0)
                )
                append(durationDescription)
            }

            CallType.MISSED,
            CallType.DECLINED -> builder.color(context.getColor(R.color.uiKitColorAccentWrong)) {
                if (isOutgoing) {
                    append(context.getString(R.string.call_declined))
                } else {
                    append(context.getString(R.string.call_missed))
                }
            }

            else -> error("This state is not allowed for the view.")
        }
        return builder.toSpannable()
    }

    private fun iconType(
        isOutgoing: Boolean,
        callStatus: CallStatusEnum,
    ): CallType {
        return when {
            isOutgoing -> CallType.OUTGOING
            !isOutgoing && callStatus == CallStatusEnum.STOPPED -> CallType.INCOMING
            else -> CallType.DECLINED
        }
    }
}
