package com.numplates.nomera3.domain.util

import com.meera.db.models.userprofile.GiftEntity
import com.numplates.nomera3.modules.user.ui.utils.UserBirthdayUtils
import com.numplates.nomera3.presentation.birthday.ui.BirthdayTextUtil
import com.numplates.nomera3.presentation.model.adaptermodel.UserGiftsUiEntity

class UserGiftsMapper constructor(
    private val birthdayTextUtil: BirthdayTextUtil,
    private val userBirthdayUtils: UserBirthdayUtils
) {
    fun mapUserGiftsToUiEntity(
        userGiftsList: List<GiftEntity>,
        viewType: Int,
        dateOfBirth: Long
    ): List<UserGiftsUiEntity> {
        val listResult = mutableListOf<UserGiftsUiEntity>()
        val hasBirthday = userBirthdayUtils.isBirthdayToday(dateOfBirth)
        userGiftsList.forEach { gift ->
            listResult.add(
                UserGiftsUiEntity(
                    giftEntity = gift,
                    giftViewType = viewType,
                    birthdayTextRanges = if (hasBirthday)
                        birthdayTextUtil.getBirthdayTextListRanges(
                            birthdayText = gift.comment ?: ""
                        ) else listOf()
                )
            )
        }

        return listResult
    }

    fun mapUserGiftToUiEntity(
        giftEntity: GiftEntity,
        viewType: Int
    ): UserGiftsUiEntity {
        return UserGiftsUiEntity(
            giftEntity = giftEntity,
            giftViewType = viewType
        )
    }
}
