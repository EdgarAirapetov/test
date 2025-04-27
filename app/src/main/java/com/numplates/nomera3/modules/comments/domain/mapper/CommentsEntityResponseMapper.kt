package com.numplates.nomera3.modules.comments.domain.mapper

import com.numplates.nomera3.modules.comments.data.api.OrderType
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import com.numplates.nomera3.modules.comments.data.entity.CommentsEntityResponse
import com.numplates.nomera3.modules.comments.ui.entity.CommentChunk
import com.numplates.nomera3.modules.comments.ui.entity.CommentEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentSeparatorEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentSeparatorType
import com.numplates.nomera3.modules.comments.ui.entity.CommentUIType
import com.numplates.nomera3.modules.comments.ui.entity.DeletedCommentEntity
import com.numplates.nomera3.modules.comments.ui.entity.SeparatorData
import com.numplates.nomera3.modules.comments.ui.entity.ToBeDeletedCommentEntity
import com.numplates.nomera3.modules.comments.ui.fragment.WhoBlockedMe
import com.numplates.nomera3.modules.comments.ui.fragment.WhoDeleteComment
import com.numplates.nomera3.modules.comments.ui.fragment.WhoDeleteComment.COMMENT_AUTHOR
import com.numplates.nomera3.modules.comments.ui.fragment.WhoDeleteComment.MODERATOR
import com.numplates.nomera3.modules.comments.ui.fragment.WhoDeleteComment.POST_AUTHOR
import com.numplates.nomera3.modules.comments.ui.util.PaginationHelper
import com.numplates.nomera3.presentation.birthday.ui.BirthdayTextUtil
import com.numplates.nomera3.presentation.utils.parseUniquename
import timber.log.Timber

private const val MIN_INNER_COMMENT_SIZE = 3

class CommentsEntityResponseMapper(
    private val paginationHelper: PaginationHelper,
    private val toBeDeletedComments: Set<ToBeDeletedCommentEntity>
) {
    var birthdayTextUtil: BirthdayTextUtil? = null

    fun map(old: CommentsEntityResponse, order: OrderType): CommentChunk {
        return CommentChunk(
            countBefore = old.countBefore,
            countAfter = old.countAfter,
            items = mapCommentToUiType(old.comments, order),
            order = order
        )
    }

    // https://nomera.atlassian.net/wiki/spaces/NOMIT/pages/1260945417/Posts#Get-post-comments-(tree-like)
    // "deleted_by": "comment_author" | "post_author" | "admin" | null
    private fun getWhoDeleteComment(deletedBy: String): WhoDeleteComment {
        if (deletedBy == "comment_author") return COMMENT_AUTHOR
        if (deletedBy == "post_author") return POST_AUTHOR
        if (deletedBy == "admin") return MODERATOR
        return COMMENT_AUTHOR
    }

    private fun getWhoBlocked(blocked: String) = if (blocked == "me") WhoBlockedMe.ME else WhoBlockedMe.USER

    private fun mapCommentToUiType(
        oldList: List<CommentEntityResponse>,
        order: OrderType,
    ): List<CommentUIType> {
        val newList = mutableListOf<CommentUIType>()

        if (oldList.isEmpty()) {
            when (order) {
                OrderType.INITIALIZE -> {
                    paginationHelper.isLastPage = true
                }

                OrderType.AFTER -> {
                    paginationHelper.isLastPage = true
                }

                OrderType.BEFORE -> {
                    paginationHelper.isTopPage = true
                }
            }

            return emptyList()
        }

        when (order) {
            OrderType.INITIALIZE -> {
                paginationHelper.firstCommentId = oldList.first().id
                paginationHelper.lastCommentId = oldList.last().id
            }

            OrderType.AFTER -> {
                paginationHelper.lastCommentId = oldList.last().id
            }

            OrderType.BEFORE -> {
                paginationHelper.firstCommentId = oldList.first().id
            }
        }

        oldList.forEach { mainItem ->
            // если поле deletedBy у ответа не пустое,
            // то комментарий считается удаленным,
            // иначе обычный комментарий
            val deletedBy: String? = mainItem.deletedBy
            val blockedBy: String? = mainItem.blockedBy
            val toBeDeleted = toBeDeletedComments.find { it.id == mainItem.id }
            Timber.d("Main Item deletedBy = $deletedBy blockedby = $blockedBy")

            val comment = getProperCommentEntity(
                comment = mainItem,
                deletedBy = deletedBy,
                blockedBy = blockedBy,
                toBeDeleted = toBeDeleted
            )
            newList.add(comment)
            if ((deletedBy != null || blockedBy != null) && toBeDeleted != null) {
                toBeDeleted.originalComment = comment
            }

            val inner = mainItem.comments

            if (inner?.comments != null && inner.comments.isNotEmpty()) {
                val ch0 = paginationHelper.hash[mainItem.id]

                if (ch0 == null) {
                    paginationHelper.hash[mainItem.id] = IntRange(inner.countBefore, inner.countAfter)
                } else {
                    when (order) {
                        OrderType.AFTER ->
                            paginationHelper.hash[mainItem.id] = IntRange(ch0.first, inner.countAfter)

                        OrderType.BEFORE ->
                            paginationHelper.hash[mainItem.id] = IntRange(inner.countBefore, ch0.last)

                        OrderType.INITIALIZE ->
                            paginationHelper.hash[mainItem.id] = IntRange(inner.countBefore, inner.countAfter)
                    }
                }

                inner.comments.forEach { innerItem ->
                    val deletedByInner: String? = innerItem.deletedBy
                    val blockedByInner: String? = innerItem.blockedBy
                    val toBeDeletedInner = toBeDeletedComments.find { it.id == innerItem.id }
                    val comment = getProperCommentEntity(
                        comment = innerItem,
                        deletedBy = deletedByInner,
                        blockedBy = blockedByInner,
                        toBeDeleted = toBeDeletedInner
                    )
                    newList.add(comment)
                    if ((deletedByInner != null || blockedByInner != null) && toBeDeletedInner != null) {
                        toBeDeletedInner.originalComment = comment
                    }
                }

                val last = inner.comments.last().id
                val first = inner.comments.first().id

                val ch1 = paginationHelper.hash[mainItem.id]

                if (ch1 != null && newList.isNotEmpty()) {
                    if (ch1.first != 0) {
                        newList.add(
                            newList.size - inner.comments.size,
                            CommentSeparatorEntity(
                                separatorType = CommentSeparatorType.SHOW_MORE,
                                count = ch1.first,
                                data = SeparatorData(
                                    orderType = OrderType.BEFORE,
                                    parentId = mainItem.id,
                                    targetCommentId = first
                                )
                            )
                        )
                    }

                    if (ch1.last == 0 && inner.comments.size > MIN_INNER_COMMENT_SIZE) {
                        newList.add(
                            newList.size,
                            CommentSeparatorEntity(
                                separatorType = CommentSeparatorType.HIDE_ALL,
                                immutable = true,
                                data = SeparatorData(
                                    orderType = OrderType.AFTER,
                                    parentId = mainItem.id,
                                    targetCommentId = last
                                )
                            )
                        )
                    } else if (ch1.last != 0) {
                        newList.add(
                            newList.size,
                            CommentSeparatorEntity(
                                separatorType = CommentSeparatorType.SHOW_MORE,
                                immutable = true,
                                count = ch1.last,
                                data = SeparatorData(
                                    orderType = OrderType.AFTER,
                                    parentId = mainItem.id,
                                    targetCommentId = last
                                )
                            )
                        )
                    }
                }
            }
        }

        return newList
    }

    fun mapInnerCommentsToChunk(
        old: CommentsEntityResponse,
        order: OrderType,
        separator: CommentSeparatorEntity,
    ): CommentChunk =
        CommentChunk(
            countBefore = old.countBefore,
            countAfter = old.countAfter,
            items = mapInnerComment(old, order, separator),
            order = order,
            separator = separator
        )

    fun mapInnerCommentsToChunk(
        old: CommentsEntityResponse,
        order: OrderType,
        parentId: Long,
    ): CommentChunk =
        CommentChunk(
            countBefore = old.countBefore,
            countAfter = old.countAfter,
            items = mapInnerComment(old, order, parentId),
            order = order,
        )

    private fun mapInnerComment(
        old: CommentsEntityResponse,
        order: OrderType,
        separator: CommentSeparatorEntity,
    ): List<CommentUIType> {

        if (old.comments.isEmpty()) return emptyList()

        val newList = mutableListOf<CommentUIType>()

        old.comments.forEach {
            val deletedByInner: String? = it.deletedBy
            val blockedByInner: String? = it.blockedBy
            val toBeDeletedInner = toBeDeletedComments.find { deleted -> deleted.id == it.id }

            val comment = getProperCommentEntity(
                comment = it,
                deletedBy = deletedByInner,
                blockedBy = blockedByInner,
                toBeDeleted = toBeDeletedInner
            )
            newList.add(comment)
            if ((deletedByInner != null || blockedByInner != null) && toBeDeletedInner != null) {
                toBeDeletedInner.originalComment = comment
            }
        }

        val ch1 = paginationHelper.hash[separator.parentId]

        if (ch1 != null && newList.isNotEmpty()) {

            val last = newList.last().id
            val first = newList.first().id

            when (order) {
                OrderType.AFTER -> {
                    paginationHelper.hash[separator.parentId] = ch1.copy(l = old.countAfter)

                    if (old.countAfter == 0) {
                        newList.add(
                            newList.size,
                            CommentSeparatorEntity(
                                separatorType = CommentSeparatorType.HIDE_ALL,
                                data = SeparatorData(
                                    orderType = order,
                                    parentId = separator.parentId,
                                    targetCommentId = last
                                )
                            )
                        )
                    } else {
                        newList.add(
                            newList.size,
                            CommentSeparatorEntity(
                                separatorType = CommentSeparatorType.SHOW_MORE,
                                count = old.countAfter,
                                data = SeparatorData(
                                    orderType = order,
                                    parentId = separator.parentId,
                                    targetCommentId = last
                                )
                            )
                        )
                    }
                }

                OrderType.BEFORE -> {
                    paginationHelper.hash[separator.parentId] = ch1.copy(s = old.countBefore)

                    if (old.countBefore <= 3) {
                        if (newList.size > 3) {
                            val index = 3 - old.countBefore
                            newList.add(
                                index,
                                CommentSeparatorEntity(
                                    separatorType = CommentSeparatorType.HIDE_ALL,
                                    immutable = true,
                                    count = ch1.last,
                                    data = SeparatorData(
                                        orderType = OrderType.AFTER,
                                        parentId = separator.parentId,
                                        targetCommentId = newList[index].id
                                    )
                                )
                            )
                        }
                    }
                    if (old.countBefore != 0) {
                        newList.add(
                            0,
                            CommentSeparatorEntity(
                                separatorType = CommentSeparatorType.SHOW_MORE,
                                count = old.countBefore,
                                data = SeparatorData(
                                    orderType = order,
                                    parentId = separator.parentId,
                                    targetCommentId = first
                                )
                            )
                        )
                    }
                }

                else -> Unit
            }
        }

        return newList
    }

    private fun mapInnerComment(
        old: CommentsEntityResponse,
        order: OrderType,
        parentId: Long,
    ): List<CommentUIType> {

        if (old.comments.isEmpty()) return emptyList()

        val newList = mutableListOf<CommentUIType>()

        old.comments.forEach {
            val deletedByInner: String? = it.deletedBy
            val blockedByInner: String? = it.blockedBy
            val toBeDeletedInner = toBeDeletedComments.find { deleted -> deleted.id == it.id }

            val comment = getProperCommentEntity(
                comment = it,
                deletedBy = deletedByInner,
                blockedBy = blockedByInner,
                toBeDeleted = toBeDeletedInner
            )
            newList.add(comment)
            if ((deletedByInner != null || blockedByInner != null) && toBeDeletedInner != null) {
                toBeDeletedInner.originalComment = comment
            }
        }

        val ch1 = paginationHelper.hash[parentId]

        if (ch1 != null && newList.isNotEmpty()) {

            val last = newList.last().id
            val first = newList.first().id

            when (order) {
                OrderType.AFTER -> {
                    paginationHelper.hash[parentId] = ch1.copy(l = old.countAfter)

                    if (old.countAfter == 0) {
                        newList.add(
                            newList.size,
                            CommentSeparatorEntity(
                                separatorType = CommentSeparatorType.HIDE_ALL,
                                data = SeparatorData(
                                    orderType = order,
                                    parentId = parentId,
                                    targetCommentId = last
                                )
                            )
                        )
                    } else {
                        newList.add(
                            newList.size,
                            CommentSeparatorEntity(
                                separatorType = CommentSeparatorType.SHOW_MORE,
                                count = old.countAfter,
                                data = SeparatorData(
                                    orderType = order,
                                    parentId = parentId,
                                    targetCommentId = last
                                )
                            )
                        )
                    }
                }

                OrderType.BEFORE -> {
                    paginationHelper.hash[parentId] = ch1.copy(s = old.countBefore)

                    if (old.countBefore == 0) {
                        newList.add(
                            0,
                            CommentSeparatorEntity(
                                separatorType = CommentSeparatorType.HIDE_ALL,
                                data = SeparatorData(
                                    orderType = order,
                                    parentId = parentId,
                                    targetCommentId = first
                                )
                            )
                        )
                    } else {
                        newList.add(
                            0,
                            CommentSeparatorEntity(
                                separatorType = CommentSeparatorType.SHOW_MORE,
                                count = old.countBefore,
                                data = SeparatorData(
                                    orderType = order,
                                    parentId = parentId,
                                    targetCommentId = first
                                )
                            )
                        )
                    }
                }

                else -> Unit
            }
        } else if (newList.size > 3 && old.countBefore <= 3) {
            ch1?.copy(s = old.countBefore)?.let {
                paginationHelper.hash[parentId] = it
            }

            val index = 3 - old.countBefore
            newList.add(
                index,
                CommentSeparatorEntity(
                    separatorType = CommentSeparatorType.HIDE_ALL,
                    count = newList.size - index - 1,
                    immutable = true,
                    data = SeparatorData(
                        orderType = OrderType.AFTER,
                        parentId = parentId,
                        targetCommentId = newList[index].id
                    )
                )
            )
        }

        return newList
    }

    fun mapCommentsForNewMessage(
        oldList: List<CommentEntityResponse>,
        order: OrderType,
        hasIntersection: Boolean,
    ): List<CommentUIType> {
        val newList = mutableListOf<CommentUIType>()

        if (oldList.isEmpty()) {
            when (order) {
                OrderType.INITIALIZE -> {
                    paginationHelper.isLastPage = true
                }

                OrderType.AFTER -> {
                    paginationHelper.isLastPage = true

                }

                OrderType.BEFORE -> {
                    paginationHelper.isTopPage = true
                }
            }

            return emptyList()
        }

        when (order) {
            OrderType.INITIALIZE -> {
                paginationHelper.firstCommentId = oldList.first().id
                paginationHelper.lastCommentId = oldList.last().id
            }

            OrderType.AFTER -> {
                paginationHelper.lastCommentId = oldList.last().id
            }

            OrderType.BEFORE -> {
                paginationHelper.lastCommentId = oldList.last().id
                if (!hasIntersection) paginationHelper.firstCommentId = oldList.first().id
            }
        }

        oldList.forEach { mainItem ->
            // если поле deletedBy у ответа не пустое,
            // то комментарий считается удаленным,
            // иначе обычный комментарий
            val deletedBy: String? = mainItem.deletedBy
            val blockedBy: String? = mainItem.blockedBy
            val toBeDeleted = toBeDeletedComments.find { it.id == mainItem.id }
            Timber.d("Main Item deletedBy = $deletedBy blockedby = $blockedBy")

            val comment = getProperCommentEntity(
                comment = mainItem,
                deletedBy = deletedBy,
                blockedBy = blockedBy,
                toBeDeleted = toBeDeleted
            )
            newList.add(comment)
            if ((deletedBy != null || blockedBy != null) && toBeDeleted != null) {
                toBeDeleted.originalComment = comment
            }

            val inner = mainItem.comments

            if (inner?.comments != null && inner.comments.isNotEmpty()) {
                val ch0 = paginationHelper.hash[mainItem.id]

                if (ch0 == null) {
                    paginationHelper.hash[mainItem.id] = IntRange(inner.countBefore, inner.countAfter)
                } else {
                    when (order) {
                        OrderType.AFTER ->
                            paginationHelper.hash[mainItem.id] = IntRange(ch0.first, inner.countAfter)

                        OrderType.BEFORE ->
                            paginationHelper.hash[mainItem.id] = IntRange(inner.countBefore, ch0.last)

                        OrderType.INITIALIZE ->
                            paginationHelper.hash[mainItem.id] = IntRange(inner.countBefore, inner.countAfter)
                    }
                }

                inner.comments.forEach { innerItem ->
                    val deletedByInner: String? = innerItem.deletedBy
                    val blockedByInner: String? = innerItem.blockedBy
                    val toBeDeletedInner = toBeDeletedComments.find { it.id == innerItem.id }
                    val comment = getProperCommentEntity(
                        comment = innerItem,
                        deletedBy = deletedByInner,
                        blockedBy = blockedByInner,
                        toBeDeleted = toBeDeletedInner
                    )
                    newList.add(comment)
                    if ((deletedByInner != null || blockedByInner != null) && toBeDeletedInner != null) {
                        toBeDeletedInner.originalComment = comment
                    }
                }

                val last = inner.comments.last().id
                val first = inner.comments.first().id

                val ch1 = paginationHelper.hash[mainItem.id]

                if (ch1 != null && newList.isNotEmpty()) {
                    if (ch1.first != 0) {
                        newList.add(
                            0,
                            CommentSeparatorEntity(
                                separatorType = CommentSeparatorType.SHOW_MORE,
                                count = ch1.first,
                                data = SeparatorData(
                                    orderType = OrderType.BEFORE,
                                    parentId = mainItem.id,
                                    targetCommentId = first
                                )
                            )
                        )
                    }

                    if (ch1.last == 0 && inner.comments.size > MIN_INNER_COMMENT_SIZE) {
                        newList.add(
                            newList.size,
                            CommentSeparatorEntity(
                                separatorType = CommentSeparatorType.HIDE_ALL,
                                immutable = true,
                                data = SeparatorData(
                                    orderType = OrderType.AFTER,
                                    parentId = mainItem.id,
                                    targetCommentId = last
                                )
                            )
                        )
                    } else if (ch1.last != 0) {
                        newList.add(
                            newList.size,
                            CommentSeparatorEntity(
                                separatorType = CommentSeparatorType.SHOW_MORE,
                                immutable = true,
                                count = ch1.last,
                                data = SeparatorData(
                                    orderType = OrderType.AFTER,
                                    parentId = mainItem.id,
                                    targetCommentId = last
                                )
                            )
                        )
                    }
                }
            }
        }

        return newList
    }

    private fun getBirthdayRanges(item: CommentEntityResponse): List<IntRange>? {
        return birthdayTextUtil?.getBirthdayTextListRanges(
            dateOfBirth = item.user.birthday,
            birthdayText = item.text ?: ""
        )
    }

    private fun getProperCommentEntity(
        comment: CommentEntityResponse,
        deletedBy: String?,
        blockedBy: String?,
        toBeDeleted: ToBeDeletedCommentEntity?
    ): CommentUIType {
        return when {
            deletedBy != null -> comment.getDeletedCommentEntity(getWhoDeleteComment(deletedBy).stringResId)
            blockedBy != null -> comment.getDeletedCommentEntity(getWhoBlocked(blockedBy).stringRes)
            toBeDeleted != null -> comment.getDeletedCommentEntity(toBeDeleted.whoDeleteComment.stringResId)
            else -> comment.getNormalCommentEntity()
        }
    }

    private fun CommentEntityResponse.getNormalCommentEntity() = CommentEntity(
        comment = this,
        needToShowReplyBtn = paginationHelper.needToShowReplyBtn,
        flyingReactionType = paginationHelper
            .flyingReactionType
            .takeIf { paginationHelper.flyingReactionCommentId == id },
        tagSpan = if (tags != null) parseUniquename(text, tags) else null,
        birthdayTextRanges = getBirthdayRanges(this)
    )

    private fun CommentEntityResponse.getDeletedCommentEntity(
        stringResId: Int
    ) = DeletedCommentEntity(
        comment = this,
        stringResId = stringResId
    )
}

fun IntRange.copy(s: Int? = null, l: Int? = null): IntRange = IntRange(s ?: start, l ?: last)
