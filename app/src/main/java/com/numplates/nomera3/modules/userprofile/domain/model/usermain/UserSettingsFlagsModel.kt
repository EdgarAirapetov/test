package com.numplates.nomera3.modules.userprofile.domain.model.usermain

data class UserSettingsFlagsModel(
    val iCanCall: Int? = 0,
    var isInCallBlacklist: Int? = 0,
    var isInCallWhitelist: Int? = 0,
    val userCanCallMe: Int? = 0,
    var notificationsOff: Int? = 0,
    var subscription_on: Int? = 0,
    var subscribedToMe: Int? = 0,
    var subscription_notify: Int? = 0,
    var hideRoadPosts: Int? = 0,
    val friendStatus: Int? = 0,
    val iCanChat: Int? = 0,
    val userCanChatMe: Int? = 0,
    val isInChatBlackList: Int? = 0,
    val isInChatWhiteList: Int? = 0,
    var iCanGreet: Int? = 0
)
