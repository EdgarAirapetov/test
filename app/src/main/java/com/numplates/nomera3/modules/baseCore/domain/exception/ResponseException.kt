package com.numplates.nomera3.modules.baseCore.domain.exception

import com.numplates.nomera3.data.network.core.ResponseError

data class ResponseException(val responseError: ResponseError?) : Exception()