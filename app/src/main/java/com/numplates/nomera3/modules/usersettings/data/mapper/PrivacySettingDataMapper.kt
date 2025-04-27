package com.numplates.nomera3.modules.usersettings.data.mapper

import com.meera.db.models.usersettings.PrivacySettingDbModel
import com.meera.db.models.usersettings.PrivacySettingDto
import com.numplates.nomera3.modules.usersettings.domain.models.PrivacySettingModel
import javax.inject.Inject

class PrivacySettingDataMapper @Inject constructor() {

    fun mapDtoToDb(source: PrivacySettingDto): PrivacySettingDbModel {
        return PrivacySettingDbModel(
            key = source.key,
            value = source.value,
            countBlacklist = source.countBlacklist,
            countWhitelist = source.countWhitelist,
        )
    }

    fun mapDbToDto(source: PrivacySettingDbModel): PrivacySettingDto {
        return PrivacySettingDto(
            key = source.key,
            value = source.value,
            countBlacklist = source.countBlacklist,
            countWhitelist = source.countWhitelist,
        )
    }

    fun mapDbToModel(source: PrivacySettingDbModel): PrivacySettingModel {
        return PrivacySettingModel(
            key = source.key,
            value = source.value,
            countBlacklist = source.countBlacklist,
            countWhitelist = source.countWhitelist,
        )
    }

    fun mapDtoToModel(source: PrivacySettingDto): PrivacySettingModel {
        return PrivacySettingModel(
            key = source.key,
            value = source.value,
            countBlacklist = source.countBlacklist,
            countWhitelist = source.countWhitelist,
        )
    }
}
