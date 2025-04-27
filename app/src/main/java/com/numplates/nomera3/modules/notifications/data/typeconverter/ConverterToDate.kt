package com.numplates.nomera3.modules.notifications.data.typeconverter

import androidx.room.TypeConverter
import java.util.Date


object ConverterToDate {

    @TypeConverter
    @JvmStatic
    fun from(value: Long): Date = Date(value)

    @TypeConverter
    @JvmStatic
    fun to(date: Date): Long = date.time

}
