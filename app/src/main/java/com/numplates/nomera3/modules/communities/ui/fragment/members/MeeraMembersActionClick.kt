package com.numplates.nomera3.modules.communities.ui.fragment.members

import com.numplates.nomera3.data.network.MeeraUserInfoModel

sealed class MeeraMembersActionClick {
    class MemberClicked(val model: MeeraUserInfoModel) : MeeraMembersActionClick()
    class MemberActionClicked(val member: MeeraUserInfoModel, val position: Int) : MeeraMembersActionClick()
    class MembershipApproveClicked(val member: MeeraUserInfoModel, val position: Int) : MeeraMembersActionClick()
}
