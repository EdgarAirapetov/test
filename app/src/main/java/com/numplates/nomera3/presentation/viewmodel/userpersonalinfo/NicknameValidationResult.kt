package com.numplates.nomera3.presentation.viewmodel.userpersonalinfo

/*
* Список ошибок уникального имени пользователя
* страница: https://nomera.atlassian.net/wiki/spaces/NOM/pages/360808548
* */
sealed class NicknameValidationResult {

    object IsEmpty : NicknameValidationResult()

    object IsTooLong : NicknameValidationResult()

    object IsValid : NicknameValidationResult()

    object IsUnknownError : NicknameValidationResult()
}