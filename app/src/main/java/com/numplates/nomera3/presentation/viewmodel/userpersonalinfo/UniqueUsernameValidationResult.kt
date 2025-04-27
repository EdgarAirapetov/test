package com.numplates.nomera3.presentation.viewmodel.userpersonalinfo

/*
* Список ошибок уникального имени пользователя
* страница: https://nomera.atlassian.net/wiki/spaces/NOM/pages/1321795792
* раздел: Формат уникального имени пользователя
* */
sealed class UniqueUsernameValidationResult {

    // пустой
    object IsEmpty : UniqueUsernameValidationResult()

    // минимум 3
    object IsTooShort : UniqueUsernameValidationResult()

    // максимум 25
    object IsTooLong : UniqueUsernameValidationResult()

    // не может начинаться с точки
    object IsStartedByDot : UniqueUsernameValidationResult()

    // не может заканчиваться точкой
    object IsEndedByDot : UniqueUsernameValidationResult()

    // не может быть две точки подряд
    object IsTwoDotOneByOne : UniqueUsernameValidationResult()

    // занято другим пользователем
    object IsTookByAnotherUser : UniqueUsernameValidationResult()

    // зарезервировано системой
    object IsNotAllowed : UniqueUsernameValidationResult()

    // зарезервировано системой
    object IsContainsInvalidCharacters : UniqueUsernameValidationResult()

    // нет ошибок
    object IsValid : UniqueUsernameValidationResult()

    object IsUnknownError : UniqueUsernameValidationResult()
}
