package com.numplates.nomera3.modules.newroads.data

interface ISensitiveContentManager {
    fun isMarkedAsNonSensitivePost(postId: Long?): Boolean

    fun markPostAsNotSensitiveForUser(postId: Long?, parentPostId: Long?)

    fun getPosts(): HashSet<Long> = hashSetOf()

    fun clear() {}
}
