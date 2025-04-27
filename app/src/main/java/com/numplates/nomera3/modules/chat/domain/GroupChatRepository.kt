package com.numplates.nomera3.modules.chat.domain

import com.numplates.nomera3.data.newmessenger.FriendEntity

interface GroupChatRepository {
    fun addUserToCache(user: FriendEntity)
    fun removeUserFromCache(user: FriendEntity)
    fun addUsersToCache(users: List<FriendEntity>)
    fun getUsersFromCache(): List<FriendEntity>
    fun setAdminsToCache(userIds: List<Long>)
    fun getAdminsFromCache(): List<FriendEntity>
    fun clearCachedUsers()
    fun clearCachedAdmins()
}
