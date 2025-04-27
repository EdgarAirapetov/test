package com.numplates.nomera3.presentation.model.adaptermodel

import com.meera.db.models.userprofile.UserSimple

data class SearchUserModel(
        val clearOldList: Boolean,
        val list: List<UserSimple>
)
