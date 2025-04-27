package com.numplates.nomera3.presentation.viewmodel.userpersonalinfo

object BirthdayValidator {
    fun validate(birthday: Any?): Boolean {
        return birthday != null && birthday is Long
    }
}
