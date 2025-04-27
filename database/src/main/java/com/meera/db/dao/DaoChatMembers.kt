package com.meera.db.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.meera.db.models.chatmembers.ChatMember

@Dao
interface DaoChatMembers {

    @Query("SELECT * FROM chat_member WHERE room_id = :roomId")
    fun getAllMembersDataSource(roomId: Long?) : DataSource.Factory<Int, ChatMember>

    @Query("SELECT * FROM chat_member WHERE room_id = :roomId")
    fun getAllMembers(roomId: Long?) : List<ChatMember>

    @Query("SELECT * FROM chat_member WHERE room_id = :roomId")
    fun getAllMembersLive(roomId: Long?) : LiveData<List<ChatMember>>

    @Query("SELECT * FROM chat_member WHERE (type = :typeCreator OR type = :typeAdmin) AND room_id = :roomId")
    fun getAdmins(roomId: Long, typeCreator: String, typeAdmin: String) : DataSource.Factory<Int, ChatMember>

    @Query("SELECT * FROM chat_member WHERE user_id = :userId AND room_id = :roomId")
    fun getMemberById(userId: Long, roomId: Long) : List<ChatMember>

    @Query("SELECT * FROM chat_member WHERE user_id = :userId")
    fun getMember(userId: Long): ChatMember

    @Query("DELETE FROM chat_member WHERE user_id in (:membersIds)")
    fun deleteMembers(membersIds: List<Long>)

    @Transaction
    fun updateMembersType(membersIds: List<Long>, type: String) {
        membersIds.forEach { memberId -> updateMemberType(memberId, type) }
    }

    @Query("UPDATE chat_member SET type = :type WHERE user_id = :userId")
    fun updateMemberType(userId: Long, type: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(member: ChatMember)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(members: List<ChatMember>) : List<Long>

    @Query("DELETE FROM chat_member WHERE user_id = :userId AND room_id = :roomId")
    fun deleteMemberById(userId: Long, roomId: Long)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(member: ChatMember): Int

    @Query("DELETE FROM chat_member")
    fun clearDb()

}
