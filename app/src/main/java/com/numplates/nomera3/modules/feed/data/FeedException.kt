package com.numplates.nomera3.modules.feed.data

import com.numplates.nomera3.data.network.core.ResponseError

class FeedException(val msg: String, val error: ResponseError?): Exception(msg)