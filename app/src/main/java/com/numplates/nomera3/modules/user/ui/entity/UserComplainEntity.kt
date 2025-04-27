package com.numplates.nomera3.modules.user.ui.entity

import com.meera.core.base.BaseFragment
import com.meera.core.extensions.empty

data class UserComplainEntity(
    val title: String = String.empty(),
    val isShowDetail: Boolean = false,
    val statusId: Int = -1,
    val transitFragmentClass: Class<out BaseFragment>? = null,
    val itemType: UserComplainItemType = UserComplainItemType.COMPLAIN
)

enum class UserComplainItemType(val key: Int) {
    HEADER(1), COMPLAIN(2)
}
