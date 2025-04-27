package com.meera.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "registration_country")
data class RegistrationCountryDbModel(
    @PrimaryKey
    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "code")
    val code: String,

    @ColumnInfo(name = "flag")
    val flag: String,

    @ColumnInfo(name = "mask")
    val mask: String

)
