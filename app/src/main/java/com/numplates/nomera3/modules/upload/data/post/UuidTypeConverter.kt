package com.numplates.nomera3.modules.upload.data.post

import androidx.room.TypeConverter
import java.util.UUID

object UuidTypeConverter {
    @TypeConverter
    @JvmStatic
    fun fromUuid(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    @JvmStatic
    fun toUuid(uuid: String?): UUID? {
        return if (uuid == null)
            null
        else {
            UUID.fromString(uuid)
        }
    }
}
