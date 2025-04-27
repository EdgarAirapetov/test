package com.numplates.nomera3.presentation.view.utils.sharedialog

interface IOnSharePost {
    fun onShareFindGroup()
    fun onShareFindFriend()
    fun onShareToGroupSuccess(groupName: String?)
    fun onShareToRoadSuccess()
    fun onShareToChatSuccess(repostTargetCount: Int)
    fun onPostItemUniqnameUserClick(userId: Long?)
    fun onOpenShareOutside() {}
}
