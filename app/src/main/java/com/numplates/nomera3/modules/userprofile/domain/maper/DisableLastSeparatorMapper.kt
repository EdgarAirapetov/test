package com.numplates.nomera3.modules.userprofile.domain.maper

import com.numplates.nomera3.modules.baseCore.ui.Separable
import com.numplates.nomera3.modules.userprofile.ui.entity.UserUIEntity
import timber.log.Timber
import java.lang.Exception

class DisableLastSeparatorMapper {
    fun map(data: List<UserUIEntity>): List<UserUIEntity> {
        val result = mutableListOf<UserUIEntity>()
        result.addAll(data)
        if (result.size <= 1) return result
        try {
            val lastItem = result[result.size - 2]
            when(lastItem) {
                is Separable -> lastItem.isSeparable = false
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return result
    }
}