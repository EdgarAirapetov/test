package com.meera.db.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

const val ROOMS_EXTRA_TABLE_NAME = "dialogs_extra"
const val ROOMS_EXTRA_ID = "room_id"
const val ROOMS_EXTRA_LAST_INPUT_TEXT = "last_input_text"


/**
 * Дополнительные данные которые относятся к комнате, но не хранятся на сервере
 */
@Parcelize
@Entity(tableName = ROOMS_EXTRA_TABLE_NAME)
data class DialogExtraEntity(

    @PrimaryKey
    @ColumnInfo(name = ROOMS_EXTRA_ID)
    var id: Long = 0L,

    @ColumnInfo(name = ROOMS_EXTRA_LAST_INPUT_TEXT)
    var lastInputText: String = ""

) : Parcelable
