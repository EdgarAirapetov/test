package com.meera.db.models.people

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.meera.db.models.userprofile.UserSettingsFlags
import kotlinx.parcelize.Parcelize

private const val PEOPLE_APPROVED_USER_TABLE_NAME = "peopleApprovedUser"

@Parcelize
@Entity(tableName = PEOPLE_APPROVED_USER_TABLE_NAME)
class PeopleApprovedUserDbModel(
    @PrimaryKey
    val userId: Long,
    @ColumnInfo(name = "subscribers_count")
    val subscribersCount: Int,
    @ColumnInfo(name = "user_name")
    val userName: String,
    @ColumnInfo(name = "account_type")
    val accountType: Int,
    @ColumnInfo(name = "approved")
    val approved: Int,
    @ColumnInfo(name = "account_color")
    val accountColor: Int,
    @ColumnInfo(name = "top_content_maker")
    val topContentMaker: Int,
    @ColumnInfo(name = "avatar_small")
    val avatarSmall: String,
    @ColumnInfo(name = "uniqueName")
    val uniqueName: String,
    @ColumnInfo(name = "settings_flags")
    val settingsFlags: UserSettingsFlags?,
    @ColumnInfo(name = "posts")
    val posts: List<PeopleUserPostDbModel>?,
    @ColumnInfo(name = "user_subscribed")
    val isUserSubscribed: Boolean,
    @ColumnInfo(name = "has_new_moments")
    val hasNewMoments: Boolean,
    @ColumnInfo(name = "has_moments")
    val hasMoments: Boolean,
) : Parcelable
