package com.numplates.nomera3.presentation.viewmodel.userpersonalinfo

private const val UNIQUE_NAME_MAX_LENGTH = 30
object NicknameValidator {
    private var isMaxLengthName = false
    fun validate(nickname: String?): NicknameValidationResult {
        if (nickname == null) {
            return NicknameValidationResult.IsEmpty
        }

        if (nickname.length > UNIQUE_NAME_MAX_LENGTH) {
            if (isMaxLengthName) {
                return NicknameValidationResult.IsTooLong
            } else {
                isMaxLengthName = true
            }
        } else {
            isMaxLengthName = false
        }

        if (nickname.isEmpty() || nickname.isBlank()) {
            return NicknameValidationResult.IsEmpty
        }

        return NicknameValidationResult.IsValid
    }
}
