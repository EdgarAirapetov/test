package com.numplates.nomera3.modules.comments.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.databinding.ItemCommentDeletedByPostAuthorBinding
import com.numplates.nomera3.databinding.MeeraItemCommentBinding
import com.numplates.nomera3.modules.baseCore.helper.ObservableMutableList
import com.numplates.nomera3.modules.comments.data.api.OrderType
import com.numplates.nomera3.modules.comments.ui.entity.CommentChunk
import com.numplates.nomera3.modules.comments.ui.entity.CommentEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentProgressEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentSeparatorEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentSeparatorType
import com.numplates.nomera3.modules.comments.ui.entity.CommentUIType
import com.numplates.nomera3.modules.comments.ui.entity.CommentUpdate
import com.numplates.nomera3.modules.comments.ui.entity.CommentViewHolderType
import com.numplates.nomera3.modules.comments.ui.entity.DeletedCommentEntity
import com.numplates.nomera3.modules.comments.ui.fragment.WhoDeleteComment
import com.numplates.nomera3.modules.comments.ui.viewholder.CommentViewHolderPlayAnimation
import com.numplates.nomera3.modules.comments.ui.viewholder.MeeraCommentProgressViewHolder
import com.numplates.nomera3.modules.comments.ui.viewholder.MeeraCommentSeparatorViewHolder
import com.numplates.nomera3.modules.comments.ui.viewholder.MeeraCommentViewHolder
import com.numplates.nomera3.modules.comments.ui.viewholder.MeeraDeletedCommentViewHolder
import com.numplates.nomera3.presentation.view.utils.inflateBinding
import timber.log.Timber
import java.util.Observer

private const val MAX_INNER_SIZE = 4
private const val MAX_COMMENT = 3
private const val BAD_INDEX = -1

class MeeraCommentAdapter(
    private val commentListCallback: ICommentsActionsCallback,
    private val separatorListener: (CommentSeparatorEntity) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val _collection = mutableListOf<CommentUIType>()
    private val collection: ObservableMutableList<CommentUIType> =
        ObservableMutableList(_collection)

    var innerSeparatorItemClickListener: () -> Unit = {}
    var collectionUpdateListener: (MutableList<CommentUIType>) -> Unit = {}
    private val observer = Observer { _, _ ->
        collectionUpdateListener(_collection)
    }

    init {
        collection.addObserver(observer)
    }

    fun removeObserver() {
        collection.deleteObserver(observer)
    }

    fun getItem(position: Int): CommentEntity? {
        return try {
            collection[position] as CommentEntity
        } catch (e: Exception) {
            Timber.d("Item isn't CommentEntity. ${e.message}")
            null
        }
    }

    fun addItemsNext(model: CommentChunk) {
        removeLoadingProgressAfter()
        if (model.separator == null) {
            collection.addAll(model.items)
            notifyItemRangeInserted(collection.size, model.items.size)
        } else {
            addInnerItems(model)
            innerSeparatorItemClickListener()
        }
    }

    fun addItemsNext(model: List<CommentUIType>) {
        removeLoadingProgressAfter()
        if (model.isEmpty()) return
        collection.addAll(model)
        notifyItemRangeInserted(collection.size, model.size)
    }

    fun addItemsNext(parentCommentId: Long, chunk: CommentChunk, scrollAction: (Int) -> Unit) {
        removeLoadingProgressAfter()
        val parentList = collection.filter { it.parentId == parentCommentId }.toMutableList()

        val firstItemFromCollection = collection.indexOfFirst { it.parentId == parentCommentId }

        val firstId = chunk.items.first { it.type == CommentViewHolderType.COMMENT }

        val indexIntersections = parentList.indexOfFirst { it.id == firstId.id }
        val newRawList = chunk.items.toMutableList()

        if (parentList.isEmpty()) {
            val index = collection.indexOfFirst { it.id == parentCommentId } + 1

            collection.addAll(index, newRawList)
            notifyItemRangeInserted(index, newRawList.size)
            scrollAction(index + newRawList.size)
            return
        }

        if (indexIntersections != BAD_INDEX) {
            if (chunk.order == OrderType.BEFORE
                && (chunk.items.indexOfFirst { it.type == CommentViewHolderType.SEPARATOR } == 0
                    || chunk.items.indexOfFirst { it.type == CommentViewHolderType.DELETED_COMMENT } == 0)
            ) {
                newRawList.removeAt(0)
            }
            val repeat = parentList.size
            val tempList = parentList.subList(0, indexIntersections)
            tempList.addAll(newRawList)

            repeat(repeat) { collection.removeAt(firstItemFromCollection) }
            notifyItemRangeRemoved(firstItemFromCollection, repeat)
            collection.addAll(firstItemFromCollection, tempList)
            notifyItemRangeInserted(firstItemFromCollection, tempList.size)
            scrollAction(firstItemFromCollection + tempList.size)
            return
        } else {
            repeat(parentList.size) { collection.removeAt(firstItemFromCollection) }
            notifyItemRangeRemoved(firstItemFromCollection, parentList.size)

            collection.addAll(firstItemFromCollection, newRawList)
            notifyItemRangeInserted(firstItemFromCollection, newRawList.size)
            scrollAction(firstItemFromCollection + newRawList.size)
            return
        }
    }

    fun addItemsPrevious(model: CommentChunk) {
        removeLoadingProgressBefore()
        if (model.separator == null) {
            collection.addAll(0, model.items)
            notifyItemRangeInserted(0, model.items.size)
        } else addInnerItems(model)

    }

    fun addItemsPrevious(model: List<CommentUIType>) {
        if (model.isEmpty()) return
        collection.addAll(0, model)
        notifyItemRangeInserted(0, model.size)
    }

    fun updateAllReplyButtonsState(needToShowReplyBtn: Boolean) {
        for (i in 0 until collection.size) {
            notifyItemChanged(i, CommentUpdate(needToShowReplyBtn = needToShowReplyBtn))
        }
    }

    private fun addInnerItems(model: CommentChunk) {
        val separator = model.separator ?: return

        var indexForDeleting = collection.indexOfFirst { it.id == separator.id }
        if (indexForDeleting == -1) return
        val parentCollection = collection.filter { it.parentId == model.separator.parentId }
        if (separator.immutable) {
            val item = collection[indexForDeleting] as CommentSeparatorEntity
            val updatedItem = item.copy(separatorType = CommentSeparatorType.HIDE_ALL)
            collection.removeAt(indexForDeleting)
            if (model.items.isNotEmpty() || parentCollection.size > 4) {
                collection.add(indexForDeleting, updatedItem)
                notifyItemChanged(indexForDeleting)
                indexForDeleting += 1
            } else {
                notifyItemRemoved(indexForDeleting)
                indexForDeleting -= 1
            }
        } else {
            collection.removeAt(indexForDeleting)
            notifyItemRemoved(indexForDeleting)
        }

        collection.addAll(indexForDeleting, model.items)
        notifyItemRangeInserted(indexForDeleting, model.items.size)
    }

    private fun hideAllAnswers(parentId: Long) {
        val parentCollection = collection.filter { it.parentId == parentId }
        if (parentCollection.isEmpty() && parentCollection.size < MAX_INNER_SIZE) return

        val newSpoilerCount = parentCollection.filterIsInstance<CommentEntity>().size
        val spoiler = parentCollection[MAX_INNER_SIZE - 1] as? CommentSeparatorEntity
        spoiler ?: return
        if (spoiler.count == 0) {
            if (newSpoilerCount > MAX_COMMENT) spoiler.count =
                newSpoilerCount - MAX_COMMENT
        } else if ((newSpoilerCount - MAX_COMMENT) > spoiler.count ?: 0) { //todo заменить на пересчет с бэка
            spoiler.count = newSpoilerCount - MAX_COMMENT
        }
        val newSpoiler = spoiler.copy(separatorType = CommentSeparatorType.SHOW_MORE)
        val indexOfSpoiler = collection.indexOfFirst { it.id == spoiler.id }

        val idFirst = parentCollection[MAX_INNER_SIZE].id
        val idLast = parentCollection.last().id
        val indexFirst = collection.indexOfFirst { it.id == idFirst }
        val indexLast = collection.indexOfLast { it.id == idLast }

        val count = indexLast - indexFirst + 1
        notifyItemRangeRemoved(indexFirst, count)

        repeat(count) {
            collection.removeAt(indexFirst)
        }
        collection[indexOfSpoiler] = newSpoiler
        notifyItemChanged(indexOfSpoiler)
        innerSeparatorItemClickListener()
    }

    fun removeItemsBefore(index: Int) {
        if (index < 0) return
        val newList = collection.subList(index, collection.size).toMutableList()
        collection.clear()
        collection.addAll(newList)
        notifyItemRangeRemoved(0, index)
    }

    fun refresh(model: CommentChunk, scrollAction: (Int) -> Unit) {
        val count = itemCount

        if (collection.size > 0) {
            collection.clear()
            notifyItemRangeRemoved(0, count)
        }

        addItemsNext(model)

        collection.indexOfFirst { it.id == model.scrollCommentId }.let {
            if (it == -1) return@let
            scrollAction(it)
        }
    }

    fun findCommentById(commentID: Long): CommentUIType? {
        return collection.find { it.id == commentID }
    }

    fun playCommentAnimation(commentId: Long, animation: CommentViewHolderPlayAnimation) {
        val index = collection.indexOfFirst { it.id == commentId }

        when (animation) {
            CommentViewHolderPlayAnimation.PlayLikeOnDoubleClickAnimation -> {
                notifyItemChanged(index,
                    CommentViewHolderPlayAnimation.PlayLikeOnDoubleClickAnimation
                )
            }
        }
    }

    fun replaceCommentByDeletion(
        commentID: Long, whoDeleteComment: WhoDeleteComment?
    ): CommentEntity? {
        if (whoDeleteComment != null) {
            val commentForReplaceIndex = findCommentEntityIndexById(commentID)
            if (commentForReplaceIndex != null) {
                val commentForReplace = collection[commentForReplaceIndex] as? CommentEntity?
                if (commentForReplace != null) {
                    val replacedCommentEntity =
                        collection[commentForReplaceIndex] as? CommentEntity?

                    val deletedCommentEntity = DeletedCommentEntity(
                        comment = replacedCommentEntity?.comment,
                        stringResId = whoDeleteComment.stringResId
                    )

                    collection[commentForReplaceIndex] = deletedCommentEntity
                    notifyItemChanged(commentForReplaceIndex)

                    return replacedCommentEntity
                }
            }
        }
        return null
    }

    fun restoreComment(comment: CommentUIType) {
        findCommentEntityIndexById(comment.id)
            ?.takeIf { it >= 0 }
            ?.also { index ->
                collection[index] = comment
                notifyItemChanged(index)
            }
    }

    /**
     * Найти индекс комментария по ид комментария
     * */
    private fun findCommentEntityIndexById(commentID: Long): Int? =
        collection.find { it.id == commentID }
            ?.let { collection.indexOf(it) }

    override fun getItemViewType(position: Int): Int =
        when (collection[position].type) {
            CommentViewHolderType.COMMENT -> ITEM_TYPE_PARENT_COMMENT
            CommentViewHolderType.SEPARATOR -> ITEM_TYPE_ACTION_COMMENT
            CommentViewHolderType.DELETED_COMMENT -> ITEM_TYPE_DELETED_COMMENT
            CommentViewHolderType.PROGRESS -> ITEM_TYPE_PROGRESS_COMMENT
        }

    override fun getItemCount(): Int = collection.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_PARENT_COMMENT ->
                MeeraCommentViewHolder(
                    binding = parent.inflateBinding(MeeraItemCommentBinding::inflate),
                    callback = commentListCallback
                )

            ITEM_TYPE_ACTION_COMMENT ->
                MeeraCommentSeparatorViewHolder(parent)

            ITEM_TYPE_PROGRESS_COMMENT ->
                MeeraCommentProgressViewHolder(parent)

            ITEM_TYPE_DELETED_COMMENT ->
                MeeraDeletedCommentViewHolder(parent.inflateBinding(ItemCommentDeletedByPostAuthorBinding::inflate))

            else ->
                throw IllegalArgumentException("Don't exist view type $viewType")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            onBindPayload(holder, payloads)
        }
    }

    private fun onBindPayload(
        holder: RecyclerView.ViewHolder,
        payloads: MutableList<Any>
    ) {
        if (holder is MeeraCommentViewHolder) {
            payloads.forEach { holder.bindPayload(it) }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MeeraCommentViewHolder -> holder.bind(collection[position] as CommentEntity)

            is MeeraCommentSeparatorViewHolder ->
                holder.bind(collection[position] as CommentSeparatorEntity, insideListener)

            is MeeraCommentProgressViewHolder -> holder.bind()

            is MeeraDeletedCommentViewHolder ->
                holder.bind(collection[position] as? DeletedCommentEntity)

            else -> Unit
        }
    }

    private val insideListener: (CommentSeparatorEntity) -> Unit = {
        val index = collection.indexOfFirst { item -> item.id == it.id }

        val target = if (it.data.orderType == OrderType.BEFORE) {
            if (collection[index + 1] is CommentSeparatorEntity) {
                collection[index + 2]
            } else {
                collection[index + 1]
            }
        } else {
            collection[index - 1]
        }

        if (it.separatorType == CommentSeparatorType.SHOW_MORE)
            separatorListener(it.copy(data = it.data.copy(targetCommentId = target.id)))
        else hideAllAnswers(parentId = it.parentId)
    }

    fun addLoadingProgressBefore() {
        if (collection.size > 0 && collection[0].type != CommentViewHolderType.PROGRESS) {
            val progress = CommentProgressEntity()
            collection.add(0, progress)
            notifyItemInserted(0)
        }
    }

    fun removeLoadingProgressBefore() {
        if (collection.size > 0 && collection[0].type == CommentViewHolderType.PROGRESS) {
            collection.removeAt(0)
            notifyItemRemoved(0)
        }
    }

    fun addLoadingProgressAfter() {
        if (collection.size > 0 && collection.last().type != CommentViewHolderType.PROGRESS) {
            val progress = CommentProgressEntity()
            collection.add(progress)
            notifyItemInserted(collection.size - 1)
        }
    }

    fun removeLoadingProgressAfter() {
        if (collection.size > 0 && collection.last().type == CommentViewHolderType.PROGRESS) {
            collection.removeAt(collection.size - 1)
            notifyItemRemoved(collection.size)
        }
    }

    fun stopProgressInnerPagination(data: CommentSeparatorEntity) {
        val index = findCommentEntityIndexById(data.id)
        index?.let {
            if (collection.isNotEmpty() && index >= 0 && index <= collection.size) {
                notifyItemChanged(index)
            }
        }
    }

    fun release() {
        collection.deleteObserver(observer)
    }

    companion object {
        const val ITEM_TYPE_DELETED_COMMENT = 4
        const val ITEM_TYPE_PROGRESS_COMMENT = 3
        const val ITEM_TYPE_ACTION_COMMENT = 2
        const val ITEM_TYPE_PARENT_COMMENT = 1
    }

}
