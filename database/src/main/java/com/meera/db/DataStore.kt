package com.meera.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.meera.db.dao.AppSettingsDao
import com.meera.db.dao.DaoChatMembers
import com.meera.db.dao.DialogDao
import com.meera.db.dao.DialogExtraDao
import com.meera.db.dao.DraftsDao
import com.meera.db.dao.EditMessageDataDao
import com.meera.db.dao.MediakeyboardFavoritesDao
import com.meera.db.dao.MessageDao
import com.meera.db.dao.NotificationDao
import com.meera.db.dao.PeopleApprovedUsersDao
import com.meera.db.dao.PeopleRelatedUsersDao
import com.meera.db.dao.PostViewStatisticDao
import com.meera.db.dao.PrivacySettingsDao
import com.meera.db.dao.RecentGifsDao
import com.meera.db.dao.RegistrationCountriesDao
import com.meera.db.dao.SendMessageDataDao
import com.meera.db.dao.UploadDao
import com.meera.db.dao.UserProfileDao
import com.meera.db.models.AppSettingsEntity
import com.meera.db.models.DialogExtraEntity
import com.meera.db.models.DraftDbModel
import com.meera.db.models.MediakeyboardFavoriteDbModel
import com.meera.db.models.PostViewLocalData
import com.meera.db.models.RecentGifEntity
import com.meera.db.models.RegistrationCountryDbModel
import com.meera.db.models.UploadItem
import com.meera.db.models.chatmembers.ChatMember
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.message.EditMessageDataDbModel
import com.meera.db.models.message.MessageEntity
import com.meera.db.models.message.SendMessageDataDbModel
import com.meera.db.models.notifications.AvatarMetaEntity
import com.meera.db.models.notifications.InfoSectionEntity
import com.meera.db.models.notifications.MetaNotificationEntity
import com.meera.db.models.notifications.NotificationEntity
import com.meera.db.models.people.PeopleApprovedUserDbModel
import com.meera.db.models.people.PeopleRelatedUserDbModel
import com.meera.db.models.userprofile.UserProfileNew
import com.meera.db.models.usersettings.PrivacySettingDbModel
import com.meera.db.typeconverters.chatmembers.ConvertToUserEntity
import com.meera.db.typeconverters.dialog.ConvertToDialogStyle
import com.meera.db.typeconverters.dialog.ConvertToLastMessage
import com.meera.db.typeconverters.dialog.ConvertToUserChat
import com.meera.db.typeconverters.message.ConvertToHashMap
import com.meera.db.typeconverters.message.ConvertToListMessageAttachment
import com.meera.db.typeconverters.message.ConvertToListString
import com.meera.db.typeconverters.message.ConvertToMessageAttachment
import com.meera.db.typeconverters.message.ConvertToMessageMetadata
import com.meera.db.typeconverters.message.ConvertToParentMessage
import com.meera.db.typeconverters.message.ConvertToParsedUniquename
import com.meera.db.typeconverters.message.ConvertToResponseData
import com.meera.db.typeconverters.message.ConvertToUniquenameEntity
import com.meera.db.typeconverters.message.ConverterToDate
import com.meera.db.typeconverters.message.CovertToListIntRanges
import com.meera.db.typeconverters.message.CovertToMessageEntity
import com.meera.db.typeconverters.notifications.ConvertToInfoSectionEntity
import com.meera.db.typeconverters.notifications.ConvertToListUserEntityNotification
import com.meera.db.typeconverters.notifications.ConvertToMediaEntity
import com.meera.db.typeconverters.notifications.ConvertToMetaNotificationEntity
import com.meera.db.typeconverters.notifications.ConverterToNotificationMomentAsset
import com.meera.db.typeconverters.notifications.ConverterToNotificationPostAsset
import com.meera.db.typeconverters.people.MutualUsersTypeConvertor
import com.meera.db.typeconverters.people.PeopleUserPostTypeConvertor
import com.meera.db.typeconverters.userprofile.ConvertListToUserSimple
import com.meera.db.typeconverters.userprofile.ConvertToAvatarModel
import com.meera.db.typeconverters.userprofile.ConvertToCity
import com.meera.db.typeconverters.userprofile.ConvertToCoordinates
import com.meera.db.typeconverters.userprofile.ConvertToCountry
import com.meera.db.typeconverters.userprofile.ConvertToGiftSenderUser
import com.meera.db.typeconverters.userprofile.ConvertToListGiftEntity
import com.meera.db.typeconverters.userprofile.ConvertToListGroups
import com.meera.db.typeconverters.userprofile.ConvertToListPhotos
import com.meera.db.typeconverters.userprofile.ConvertToListVehicleEntities
import com.meera.db.typeconverters.userprofile.ConvertToMetadata
import com.meera.db.typeconverters.userprofile.ConvertToProductEntity
import com.meera.db.typeconverters.userprofile.ConvertToUserMoments
import com.meera.db.typeconverters.userprofile.ConvertToUserSimple
import com.meera.db.typeconverters.userprofile.ConvertToVehicleCountry
import com.meera.db.typeconverters.userprofile.ConvertToVehicleEntity
import com.meera.db.typeconverters.userprofile.ConvertToVehicleModel
import com.meera.db.typeconverters.userprofile.ConvertToVehicleType
import com.meera.db.typeconverters.userprofile.ConverterToUserOnlineStatus
import com.meera.db.typeconverters.userprofile.ConverterToUserSettingsFlags
import com.meera.db.typeconverters.userprofile.CovertToVehicleBrand
import com.meera.db.typeconverters.userprofile.TypeConvertorToMutualUsers

@TypeConverters(
    ConvertToCity::class,
    ConvertToCountry::class,
    ConvertToVehicleEntity::class,
    ConvertToListVehicleEntities::class,
    ConvertToListGiftEntity::class,
    ConvertToListPhotos::class,
    ConvertToListGroups::class,
    ConverterToUserSettingsFlags::class,
    ConverterToUserOnlineStatus::class,
    ConvertToProductEntity::class,
    TypeConvertorToMutualUsers::class,
    ConvertToCoordinates::class,
    ConvertToVehicleType::class,
    CovertToVehicleBrand::class,
    ConvertToVehicleModel::class,
    ConvertToVehicleCountry::class,
    ConvertToGiftSenderUser::class,
    ConvertToAvatarModel::class,
    ConvertToMetadata::class,
    ConvertToUserSimple::class,
    ConvertToDialogStyle::class,
    ConvertToLastMessage::class,
    ConvertToUserChat::class,
    CovertToMessageEntity::class,
    ConvertToMessageMetadata::class,
    ConvertToMessageAttachment::class,
    ConvertToListMessageAttachment::class,
    ConvertToResponseData::class,
    ConvertToUniquenameEntity::class,
    ConvertToParsedUniquename::class,
    ConvertToParentMessage::class,
    ConvertToListString::class,
    CovertToListIntRanges::class,
    ConvertToHashMap::class,
    ConverterToDate::class,
    ConvertToUserEntity::class,
    ConvertToMetaNotificationEntity::class,
    ConvertToMediaEntity::class,
    ConvertToListUserEntityNotification::class,
    ConvertToInfoSectionEntity::class,
    ConverterToNotificationPostAsset::class,
    ConverterToNotificationMomentAsset::class,
    PeopleUserPostTypeConvertor::class,
    MutualUsersTypeConvertor::class,
    ConvertListToUserSimple::class,
    ConvertToUserMoments::class
)
@Database(
    entities = [
        UserProfileNew::class,
        UploadItem::class,
        DialogEntity::class,
        MessageEntity::class,
        ChatMember::class,
        DialogExtraEntity::class,
        RecentGifEntity::class,
        NotificationEntity::class,
        MetaNotificationEntity::class,
        AvatarMetaEntity::class,
        InfoSectionEntity::class,
        AppSettingsEntity::class,
        PostViewLocalData::class,
        RegistrationCountryDbModel::class,
        SendMessageDataDbModel::class,
        EditMessageDataDbModel::class,
        MediakeyboardFavoriteDbModel::class,
        DraftDbModel::class,
        PrivacySettingDbModel::class,
        PeopleApprovedUserDbModel::class,
        PeopleRelatedUserDbModel::class,
    ],
    version = 222,
)

/**
 * DB version
 * - 193 (7.06.23)
 * - 194 (21.06.23)
 * - 195 (5.07.23)
 * - 196 (19.07.23)
 * - 197 (1.08.23)
 * - 198 (16.08.23)
 * - 199 (30.08.23)
 * - 200 (12.09.23)
 * - 201 (18.09.23)
 * - 202 (27.09.23)
 * - 203 (11.10.23)
 * - 204 (7.11.23)
 * - 205 (01.12.23)
 * - 206 (6.05.12)
 *  - 207 (20.12.23)
 *  - 208 (11.01.24)
 *  - 209 (6.02.24)
 *  - 210 (7.02.24)
 *  - 212 (20.02.24)
 *  - 213 (21.02.24)
 *  - 214 (13.03.24)
 *  - 215 (02.04.24)
 *  - 216 (15.04.24)
 *  - 217 (2.05.24
 *  - 218 (29.05.24)
 *  - 219 (11.07.24)
 *  - 220 (24.07.24)
 *  - 221 (4.10.24)
 *  - 222 (25.03.25)
 */
abstract class DataStore : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun uploadDao(): UploadDao
    abstract fun dialogDao(): DialogDao
    abstract fun dialogExtraDao(): DialogExtraDao
    abstract fun messageDao(): MessageDao
    abstract fun daoChatMembers(): DaoChatMembers
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun recentGifsDao(): RecentGifsDao
    abstract fun notificationDao(): NotificationDao
    abstract fun postViewStatisticDao(): PostViewStatisticDao
    abstract fun registrationCountriesDao(): RegistrationCountriesDao
    abstract fun sendMessageDataDao(): SendMessageDataDao
    abstract fun editMessageDataDao(): EditMessageDataDao
    abstract fun mediakeyboardFavoritesDao(): MediakeyboardFavoritesDao
    abstract fun draftsDao(): DraftsDao
    abstract fun privacySettingsDao(): PrivacySettingsDao
    abstract fun peopleApprovedUsersDao(): PeopleApprovedUsersDao
    abstract fun peopleRelatedUsersDao(): PeopleRelatedUsersDao
}
