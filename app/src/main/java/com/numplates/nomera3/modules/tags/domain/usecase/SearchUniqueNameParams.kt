package com.numplates.nomera3.modules.tags.domain.usecase

import com.numplates.nomera3.modules.comments.domain.DefParams

class SearchUniqueNameParams(val query: String, val roomId: Int? = null) : DefParams()