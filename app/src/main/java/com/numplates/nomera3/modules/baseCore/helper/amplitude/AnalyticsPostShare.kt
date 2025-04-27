package com.numplates.nomera3.modules.baseCore.helper.amplitude

data class AnalyticsPostShare(
    val momentId:Long = 0,
    val postId: Long = 0,
    val authorId : Long,
    val where: AmplitudePropertyWhere,
    val groupCount: Int = 0,
    val chatCount: Int = 0,
    val textAdded: Boolean = false,
    val whereSent: AmplitudePropertyWhereSent = AmplitudePropertyWhereSent.CHAT,
    val search: Boolean = false,
    val publicType: AmplitudePropertyPublicType
)
