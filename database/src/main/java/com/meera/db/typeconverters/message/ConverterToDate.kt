package com.meera.db.typeconverters.message

import androidx.room.TypeConverter
import java.util.Date

class ConverterToDate {

    companion object {

        @TypeConverter
        @JvmStatic
        fun from(value: Long): Date = Date(value)

        @TypeConverter
        @JvmStatic
        fun to(date: Date): Long = date.time

    }

}
