package com.meera.db.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.LastMessage
import com.meera.db.models.dialog.UserChat
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow


@Dao
abstract class DialogDao {

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(dialog: DialogEntity) : Int

    @Query("SELECT * FROM dialogs")
    abstract fun getAllDialogsSingle() : Single<List<DialogEntity>>

    @Query("SELECT * FROM dialogs WHERE deleted != 1 ORDER BY last_message_updated_at DESC")
    abstract fun getAllDialogsObservable() : Observable<List<DialogEntity>>

    @Query("SELECT * FROM dialogs WHERE deleted != 1 AND approved == :approveStatus ORDER BY last_message_updated_at DESC")
    abstract fun getAllDialogs(approveStatus: Int) : DataSource.Factory<Int, DialogEntity>

    @Query("SELECT * FROM dialogs " +
        "WHERE deleted != 1 " +
        "AND approved == :approveStatus " +
        "AND (companion_name LIKE '%' || :search || '%' OR companion_uniqueName LIKE '%' || :search || '%' OR title LIKE '%' || :search || '%' )" +
        "ORDER BY last_message_updated_at DESC"
    )
    abstract fun getDialogsBySearch(search: String, approveStatus: Int) : DataSource.Factory<Int, DialogEntity>

    @Query("SELECT * FROM dialogs " +
        "WHERE deleted != 1 " +
        "AND approved == :approveStatus " +
        "AND companion_uniqueName LIKE '%' || :search || '%'" +
        "ORDER BY last_message_updated_at DESC"
    )
    abstract fun getDialogsByUniqueName(search: String, approveStatus: Int) : DataSource.Factory<Int, DialogEntity>

    @Query("SELECT * FROM dialogs WHERE deleted != 1 AND approved == :approveStatus ORDER BY last_message_updated_at DESC")
    abstract fun getAllDialogsObservable(approveStatus: Int) : Observable<List<DialogEntity>>

    @Query("SELECT * FROM dialogs " +
        "WHERE deleted != 1 AND (approved == :notDefinedStatus OR approved == :forbiddenStatus) AND is_hidden != 1 " +
        "ORDER BY last_message_updated_at DESC")
    abstract fun getChatRequestDialogs(notDefinedStatus: Int, forbiddenStatus: Int): DataSource.Factory<Int, DialogEntity>

    @Query("SELECT * FROM dialogs WHERE deleted != 1 AND approved == 0 OR approved == 2 ORDER BY last_message_updated_at DESC")
    abstract fun getAllNonApprovedDialogs(): List<DialogEntity>

    @Query("SELECT * FROM dialogs WHERE deleted != 1 ORDER BY last_message_updated_at DESC")
    abstract fun getAllDialogsLive() : LiveData<List<DialogEntity>>

    @Query("SELECT * FROM dialogs WHERE deleted != 1 ORDER BY last_message_updated_at DESC LIMIT 20")
    abstract fun getDialogsLimit(): LiveData<List<DialogEntity>>

    @Query("SELECT * FROM dialogs WHERE deleted != 1 ORDER BY last_message_updated_at DESC LIMIT :limit OFFSET :offset")
    abstract fun getRoomsByPage(limit: Int, offset: Int): List<DialogEntity>

    @Query("SELECT * FROM dialogs WHERE deleted != 1 ORDER BY last_message_updated_at DESC LIMIT :limit OFFSET :offset")
    abstract fun getLiveRoomsByPage(limit: Int, offset: Int): LiveData<List<DialogEntity>>

    @Query("SELECT count(*) FROM dialogs WHERE deleted != 1")
    abstract fun getCountRooms() : LiveData<Long>

    @Query("SELECT count(*) FROM dialogs " +
        "WHERE (approved == :notDefinedStatus OR approved == :forbiddenStatus) AND deleted != 1 AND is_hidden != 1")
    abstract fun getChatRequestRoomsCount(
        notDefinedStatus: Int,
        forbiddenStatus: Int
    ): LiveData<Int>

    @Query("SELECT count(*) FROM dialogs WHERE approved == :approveStatus " +
        "AND unread_message_count > 0 " +
        "AND companion_notifications_off == 0 " +
        "AND deleted != 1")
    abstract fun getChatRequestUnreadMessageCount(approveStatus: Int): LiveData<Int>

    @Query("SELECT * FROM dialogs WHERE room_id = :roomId")
    abstract fun getDialogByRoomIdLive(roomId: Long?) : LiveData<DialogEntity>

    @Query("SELECT * FROM dialogs WHERE room_id = :roomId")
    abstract fun getDialogByRoomIdRx(roomId: Long?) : Single<DialogEntity>

    @Deprecated("This method should delete. Use getRoom(roomId)")
    @Query("SELECT * FROM dialogs WHERE room_id = :roomId")
    abstract fun getDialogByRoomId(roomId: Long?) : DialogEntity

    @Query("SELECT * FROM dialogs WHERE room_id = :roomId")
    abstract fun getDialogByRoomIdSuspend(roomId: Long?) : DialogEntity?

    @Query("SELECT * FROM dialogs WHERE room_id = :roomId")
    abstract fun getRoom(roomId: Long?): DialogEntity?

    @Query("SELECT * FROM dialogs WHERE companion_userId = :companionId")
    abstract fun getRoomByCompanionFlow(companionId: String) : Flow<DialogEntity?>

    @Query("SELECT * FROM dialogs WHERE companion_userId = :companionId")
    abstract fun getRoomByCompanionFlow(companionId: Long) : Flow<DialogEntity?>

    @Query("SELECT * FROM dialogs WHERE companion_uid = :userId")
    abstract fun getRoomByUserId(userId: String) : Single<DialogEntity>

    @Query("SELECT MAX(updated_at) FROM dialogs")
    abstract fun getRoomsMaxUpdatedAt() : Long?

    @Query("SELECT MIN(updated_at) FROM dialogs")
    abstract fun getRoomsMinUpdatedAt() : Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(dialog: DialogEntity) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(dialogs: List<DialogEntity>) : List<Long>

    @Delete
    abstract fun delete(dialog: DialogEntity) : Int

    @Delete
    abstract fun delete(dialogs: List<DialogEntity>) : Int

    @Query("DELETE FROM dialogs WHERE room_id = :roomId")
    abstract fun deleteById(roomId: Long): Int

    @Query("UPDATE dialogs SET is_hidden = :isHidden WHERE room_id = :dialogId")
    abstract fun changeDialogVisibilityById(dialogId: Long, isHidden: Boolean)

    @Query("DELETE FROM dialogs WHERE room_id = :roomId")
    abstract fun deleteDialogByRoomId(roomId: Long) : Int

    @Query("UPDATE dialogs SET deleted = :isDeleted WHERE room_id = :roomId")
    abstract fun markRoomDeleted(roomId: Long, isDeleted: Boolean): Int

    @Query("UPDATE dialogs SET last_text_input_message = :lastMessage WHERE room_id = :roomId")
    abstract fun updateLastTextMessage(lastMessage: String, roomId: Long) : Int

    @Query("SELECT last_text_input_message FROM dialogs WHERE room_id = :roomId")
    abstract fun getLastTextMessage(roomId: Long) : String

    @Query("UPDATE dialogs SET last_message_updated_at = :updatedAt WHERE room_id = :roomId")
    abstract fun updateDialogTime(roomId: Long, updatedAt: Long)

    @Query("UPDATE dialogs SET last_message_sent =:lastMessageSent, last_message = :lastMessage WHERE room_id = :roomId AND deleted != 1")
    abstract fun updateLastMessage(roomId: Long, lastMessage: LastMessage?, lastMessageSent: Boolean?) : Int

    @Transaction
    open fun updateCompanionUser(roomId: Long, user: UserChat?) : Int {
        val item = getDialogByRoomId(roomId)
        item.companion = user ?: UserChat()
        return update(item)
    }

    //Обновление бейджа непрочитанных сообщений в комнате
    @Query("UPDATE dialogs SET need_to_show_unread_badge = :needToShowUnreadBadge WHERE room_id = :roomId")
    abstract fun updateBadgeStatus(needToShowUnreadBadge: Boolean, roomId: Long) : Int

    // creatorUid - not my message
    @Query("UPDATE dialogs SET unread_message_count = unread_message_count + 1 WHERE room_id = :roomId AND deleted != 1")
    abstract fun incrementUnreadMessageCount(roomId: Long) : Int

    @Query("UPDATE dialogs SET approved = :approvedStatus WHERE room_id = :roomId")
    abstract fun updateChatRequestApprovedStatus(roomId: Long, approvedStatus: Int): Int

    @Query("SELECT unread_message_count FROM dialogs WHERE room_id = :roomId")
    abstract fun getUnreadMessageCount(roomId: Long) : Long

    @Query("UPDATE dialogs SET unread_message_count = :count WHERE room_id = :roomId AND deleted != 1")
    abstract fun updateUnreadMessageCount(roomId: Long?, count: Long) : Int

    @Query("UPDATE dialogs SET unreaded_first_at = :timestamp WHERE room_id = :roomId")
    abstract fun updateLastUnreadMessageTs(roomId: Long?, timestamp: Long?): Int

    @Query("UPDATE dialogs SET is_muted = :isMuted WHERE room_id = :roomId")
    abstract fun updateMutedRoomState(roomId: Long, isMuted: Boolean): Int

    @Query("SELECT unreaded_first_at FROM dialogs WHERE room_id = :roomId")
    abstract fun getUnreadedFirstAt(roomId: Long?): Long

    @Query("SELECT * FROM dialogs WHERE room_id = :roomId")
    abstract fun getDialogById(roomId: Long?): List<DialogEntity>

    @Query("SELECT * FROM dialogs WHERE companion_uid = :companionId")
    abstract fun getDialogByCompanionId(companionId: Long?): List<DialogEntity>

    @Query("UPDATE dialogs SET approved = :approvedStatus WHERE room_id = :roomId")
    abstract fun updateLastUnreadMessageTs(roomId: Long?, approvedStatus: Int): Int

    @Query("SELECT * FROM dialogs WHERE deleted != 1")
    abstract fun getUnreadDialogEntities() : Observable<List<DialogEntity>>

    @Query("SELECT SUM(unread_message_count) FROM dialogs WHERE deleted != 1 " +
        "AND companion_notifications_off == 0 " +
        "AND approved == 1 " +
        "AND is_muted != 1")
    abstract fun liveCountUnreadMessages() : LiveData<Int?>

    @Query("SELECT COUNT(*) FROM dialogs WHERE need_to_show_unread_badge == 1 AND last_message_sent == 0")
    abstract fun getBadgedUnreadDialogs() : Observable<Int>

    @Query("SELECT last_message FROM dialogs WHERE room_id == :roomId")
    abstract fun getLastMessageForDialog(roomId: Long) : LastMessage

    @Query("DELETE FROM dialogs")
    abstract fun purge()
}
