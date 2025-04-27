package com.numplates.nomera3.modules.moments.util

import com.numplates.nomera3.modules.moments.show.data.entity.MomentPagingParams

fun isMomentPagingOnLastPage(pagingParams: MomentPagingParams?): Boolean {
    return pagingParams?.isLastPage ?: true
}
