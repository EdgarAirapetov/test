package com.numplates.nomera3.presentation.viewmodel

data class UserPersonalInfoContainer(
    val photo: String? = null,
    val nickname: String? = null,
    val nicknameValidationErrorText: String? = null,
    val animatedPhoto: String? = null,
    val username: String? = null,
    val usernameValidationErrorText: String? = null,
    val isMale: Boolean = true,
    val birthday: Long? = null,
    val birthdayStr: String? = null,
    val countryId: Long? = null,
    val countryName: String? = null,
    val countryFlag: String? = null,
    val cityId: Long? = null,
    val cityName: String? = null,
    val cityNameTextError: String? = null,
    val avatarAnimation: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null
)
