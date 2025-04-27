package com.numplates.nomera3.modules.communities.ui.entity


data class CommunityListItemUIModel(
    val id: Int?,
    val coverImage: String, // обложка группы
    val isPrivate: Boolean, // закрытая группа?
    var isMember: Boolean,  // участник группы?
    val isCreator: Boolean, // создатель группы?
    val isModerator: Boolean, // администратор группы?
    val name: String,         // имя группы
    val memberCount: Int,     // количество участников
    val isUserApproved: Boolean = false, //
    val memberRole: CommunityMemberRole,  //
    var userStatus: Int
)