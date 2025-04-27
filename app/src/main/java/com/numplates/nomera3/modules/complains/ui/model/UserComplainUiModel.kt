package com.numplates.nomera3.modules.complains.ui.model

import androidx.annotation.StringRes
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.user.ui.entity.ComplainReasonId
import com.numplates.nomera3.modules.user.ui.entity.ComplainReasonId.OTHER
import com.numplates.nomera3.modules.user.ui.entity.UserComplainItemType
import java.io.Serializable

data class UserComplainUiModel(
    @StringRes val titleRes: Int = -1,
    val reasonId: ComplainReasonId = OTHER,
    val itemType: UserComplainItemType = UserComplainItemType.COMPLAIN,
    @StringRes val dialogHeaderTitle: Int = R.string.user_complain_question_title_two_lines
) : Serializable
