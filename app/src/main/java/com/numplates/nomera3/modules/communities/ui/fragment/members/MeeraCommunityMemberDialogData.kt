package com.numplates.nomera3.modules.communities.ui.fragment.members

data class MeeraCommunityMemberDialogData(
    val firstItemTitle: String? = null,
    val firstItemSubtitle: String? = null,
    val firstItemIcon: Int? = null,
    val firstItemColorIcon: Int? = null,
    val secondItemTitle: String? = null,
    val secondItemSubtitle: String? = null,
    val secondItemIcon: Int? = null,
    val secondItemColorIcon: Int? = null,
    val thirdItemTitle: String? = null,
    val thirdItemSubtitle: String? = null,
    val thirdItemIcon: Int? = null,
    val thirdItemColorIcon: Int? = null,
    val menuItemClickListener: ((action: MeeraCommunityMembersDialogAction) -> Unit)? = null
)
