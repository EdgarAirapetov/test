package com.numplates.nomera3.modules.chat.data.repository

import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.data.newmessenger.FriendEntity
import com.numplates.nomera3.modules.chat.domain.GroupChatRepository
import javax.inject.Inject

@AppScope
class GroupChatRepositoryImpl @Inject constructor() : GroupChatRepository {

    private val allUsers: HashMap<Long, FriendEntity> = HashMap()
    private val adminUsers: HashMap<Long, FriendEntity> = HashMap()

    override fun addUserToCache(user: FriendEntity) {
        allUsers[user.id] = user
    }

    override fun removeUserFromCache(user: FriendEntity) {
        allUsers.remove(user.id)
    }

    override fun addUsersToCache(users: List<FriendEntity>) {
        users.forEach { user -> allUsers[user.id] = user }
    }

    override fun getUsersFromCache(): List<FriendEntity> {
        return allUsers.values.toList()
    }

    override fun setAdminsToCache(userIds: List<Long>) {
        userIds.forEach { uid ->
            allUsers[uid]?.let { admin -> adminUsers[uid] = admin }
        }
    }

    override fun getAdminsFromCache(): List<FriendEntity> {
        return adminUsers.values.toList()
    }

    override fun clearCachedUsers() {
        allUsers.clear()
    }

    override fun clearCachedAdmins() {
        adminUsers.clear()
    }
}
