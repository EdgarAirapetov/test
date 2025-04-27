package com.meera.db.models.notifications

import androidx.room.Entity
import androidx.room.PrimaryKey

const val ACTION_TYPE_READ_ALL = "READ_ALL"
const val ACTION_TYPE_DELETE_ALL = "DELETE_ALL"
const val ACTION_TYPE_NOTHING = "NOTHING"

const val HIGH_PRIORITY = 1

const val INFO_SECTION_ENTITY_TABLE_NAME = "InfoSectionEntity"


@Entity(tableName = INFO_SECTION_ENTITY_TABLE_NAME)
data class InfoSectionEntity(
    @PrimaryKey
    val id: String,
    val priority: Int,
    val name: String,
    val action: String
)
