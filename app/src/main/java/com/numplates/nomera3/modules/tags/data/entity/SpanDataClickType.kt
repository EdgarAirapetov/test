package com.numplates.nomera3.modules.tags.data.entity

sealed class SpanDataClickType {
    class ClickUserId(val userId: Long?) : SpanDataClickType()
    class ClickGroupId(val groupId: Long?) : SpanDataClickType()
    class ClickHashtag(val hashtag: String?) : SpanDataClickType()
    class ClickBadWord(val badWord: String?,
                       val startIndex: Int,
                       val endIndex: Int,
                       val tagSpanId: String?) : SpanDataClickType()
    object ClickUnknownUser : SpanDataClickType()
    object ClickBirthdayText: SpanDataClickType()
    object ClickMore: SpanDataClickType()
    class ClickLink(val link: String?) : SpanDataClickType()
}
