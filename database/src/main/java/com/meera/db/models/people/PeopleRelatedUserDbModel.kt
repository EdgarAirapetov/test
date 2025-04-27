package com.meera.db.models.people

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.meera.db.models.userprofile.City
import com.meera.db.models.userprofile.Country
import com.meera.db.models.userprofile.UserSettingsFlags
import com.meera.db.models.userprofile.UserSimple
import kotlinx.parcelize.Parcelize

private const val PEOPLE_RELATED_USERS_TABLE_NAME = "peopleRelatedUsers"

@Parcelize
@Entity(tableName = PEOPLE_RELATED_USERS_TABLE_NAME)
data class PeopleRelatedUserDbModel(
    @PrimaryKey
    val userId: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "account_color")
    val accountColor: Int,
    @ColumnInfo(name = "account_type")
    val accountType: Int,
    @ColumnInfo(name = "approved")
    val approved: Int,
    @ColumnInfo(name = "avatar_small")
    val avatar: String,
    @ColumnInfo(name = "birthday")
    val birthday: Long?,
    @ColumnInfo(name = "city")
    val city: City?,
    @ColumnInfo(name = "country")
    val country: Country?,
    @ColumnInfo(name = "country_id")
    val countryId: Long,
    @ColumnInfo(name = "gender")
    val gender: Int,
    @ColumnInfo(name = "settings_flags")
    val settingsFlags: UserSettingsFlags?,
    @ColumnInfo(name = "mutual_users")
    val mutualFriends: List<UserSimple>?,
    @ColumnInfo(name = "mutual_total_count")
    val mutualTotalCount: Int,
    @ColumnInfo(name = "friend_request")
    val hasFriendRequest: Boolean,
    @ColumnInfo(name = "top_content_maker")
    val topContentMaker: Int?
) : Parcelable
