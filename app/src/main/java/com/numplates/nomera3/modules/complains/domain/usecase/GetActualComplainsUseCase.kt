package com.numplates.nomera3.modules.complains.domain.usecase

import com.numplates.nomera3.modules.complains.ui.reason.ComplainType
import com.numplates.nomera3.modules.user.ui.entity.ComplainReasonId
import javax.inject.Inject

private const val HACKING_ITEM_LIST_POSITION = 1

class GetActualComplainsUseCase @Inject constructor() {

    private val commonListComplaints = mutableListOf(
        ComplainReasonId.SPAM,
        ComplainReasonId.HACKING,
        ComplainReasonId.FRAUD,
        ComplainReasonId.HOSTILE_SPEECH,
        ComplainReasonId.NUDES,
        ComplainReasonId.PROSTITUTION,
        ComplainReasonId.ILLEGAL_GOODS,
        ComplainReasonId.ADVERTISING,
        ComplainReasonId.OTHER,
    )

    fun invoke(complaintType: Int?): List<ComplainReasonId> {
        return when (complaintType) {
            ComplainType.USER.key -> commonListComplaints
            ComplainType.CHAT.key -> commonListComplaints
                .filterIndexed { index, _ -> index != HACKING_ITEM_LIST_POSITION }
            else -> commonListComplaints
        }
    }
}
