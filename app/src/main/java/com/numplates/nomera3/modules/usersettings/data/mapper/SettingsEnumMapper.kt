package com.numplates.nomera3.modules.usersettings.data.mapper

import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.ALLOW_CONTACT_SYNC
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.ALLOW_SCREENSHOT_SHARING
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.ALLOW_SHAKE_GESTURE
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.BLACKLIST
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.CLOSED_PROFILE
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.CREATE_AVATAR_POST
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.HIDE_POSTS
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.HOW_CAN_CALL
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.IS_ANONYMOUS_BLOCKED
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.MOMENTS_ALLOW_COMMENT
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.MOMENTS_HIDE_FROM
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.MOMENTS_NOT_SHOW
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.PROFANITY_ENABLED
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.REMIND_MY_BIRTHDAY
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.SAVE_MOMENTS_TO_ARCHIVE
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.SAVE_MOMENTS_TO_GALLERY
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.SHOW_ABOUT_ME
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.SHOW_BIRTHDAY
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.SHOW_FRIENDS_AND_FOLLOWERS
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.SHOW_GARAGE
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.SHOW_GENDER
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.SHOW_GIFTS
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.SHOW_MOMENTS_ONLY_FOR_FRIENDS
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.SHOW_ONLINE
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.SHOW_ON_MAP
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.SHOW_PERSONAL_ROAD
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.WHO_CAN_CHAT
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum.ALL
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum.FRIENDS
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum.NOBODY
import javax.inject.Inject

class SettingsEnumMapper @Inject constructor() {

    fun mapKeyToType(settingKey: SettingsKeyEnum): SettingsUserTypeEnum? {
        return when (settingKey) {
            SHOW_ON_MAP,
            SHOW_GENDER,
            SHOW_BIRTHDAY,
            SHOW_ABOUT_ME,
            SHOW_GARAGE,
            SHOW_GIFTS,
            SHOW_PERSONAL_ROAD,
            SHOW_ONLINE,
            WHO_CAN_CHAT,
            REMIND_MY_BIRTHDAY,
            PROFANITY_ENABLED,
            SHOW_FRIENDS_AND_FOLLOWERS -> ALL
            ALLOW_SHAKE_GESTURE -> ALL
            ALLOW_SCREENSHOT_SHARING -> ALL
            HOW_CAN_CALL -> FRIENDS
            IS_ANONYMOUS_BLOCKED,
            CLOSED_PROFILE,
            CREATE_AVATAR_POST -> NOBODY
            HIDE_POSTS,
            ALLOW_CONTACT_SYNC,
            MOMENTS_NOT_SHOW,
            MOMENTS_HIDE_FROM,
            MOMENTS_ALLOW_COMMENT,
            SHOW_MOMENTS_ONLY_FOR_FRIENDS,
            SAVE_MOMENTS_TO_GALLERY,
            SAVE_MOMENTS_TO_ARCHIVE,
            BLACKLIST -> null
        }
    }
}
