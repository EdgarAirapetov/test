package com.numplates.nomera3.presentation.viewmodel.userpersonalinfo

import java.util.regex.Pattern

object UniqueUsernameValidator {
    private var isMaxLengthName = false
    fun validate(username: String?): UniqueUsernameValidationResult {
        if (username == null) {
            return UniqueUsernameValidationResult.IsEmpty
        }

        if (username.isBlank() || username.isEmpty()) {
            return UniqueUsernameValidationResult.IsEmpty
        }

        if (username.startsWith(".")) {
            return UniqueUsernameValidationResult.IsStartedByDot
        }

        if (username.contains(" ")) {
            return UniqueUsernameValidationResult.IsNotAllowed
        }

        if (username.endsWith(".")) {
            return UniqueUsernameValidationResult.IsEndedByDot
        }

        if (username.contains("..")) {
            return UniqueUsernameValidationResult.IsTwoDotOneByOne
        }

        if (Pattern.compile("[^a-z0-9._-]", Pattern.CASE_INSENSITIVE)
                .matcher(username).find()
        ) {
            return UniqueUsernameValidationResult.IsContainsInvalidCharacters
        }

        if (username.length < 3) {
            return UniqueUsernameValidationResult.IsTooShort
        }

        if (username.length == 25) {
            if (isMaxLengthName) {
                return UniqueUsernameValidationResult.IsTooLong
            } else {
                isMaxLengthName = true
            }
        } else {
            isMaxLengthName = false
        }

        return UniqueUsernameValidationResult.IsValid
    }
}
