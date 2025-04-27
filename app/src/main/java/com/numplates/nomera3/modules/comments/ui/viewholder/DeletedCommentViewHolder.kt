package com.numplates.nomera3.modules.comments.ui.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.comments.ui.entity.DeletedCommentEntity

//TODO ROAD_FIX
//private const val COMPENSATION_VALUE = 4
//
//private const val SIZE_ICON_ZERO_LEVEL = 36
//
//private const val SIZE_ICON_FIRST_LEVEL = 30

class DeletedCommentViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    fun bind(model: DeletedCommentEntity?) {
        setCommentDeletionReason(model)
//        setStartMargin(model)
    }

    //TODO ROAD_FIX
//    private fun setStartMargin(model: DeletedCommentEntity?) {
//        getStartMargin(isParentComment(model)).also { startMargin ->
//            view.findViewById<UiKitUserpicImage>(R.id.userPhotoStub)?.apply {
//                setSizeIcon(this, isParentComment(model))
//                setMargins(startMargin)
//            }
//        }
//    }

    private fun setCommentDeletionReason(model: DeletedCommentEntity?) {
        model?.stringResId
                ?.let { view.context.getString(it) }
                ?.takeIf { it.isNotBlank() }
                ?.also { view.findViewById<TextView>(R.id.deletedCommentReason)?.text = it }
    }

    //TODO ROAD_FIX
//    private fun isParentComment(model: DeletedCommentEntity?): Boolean =
//            model?.comment?.parentId == null
//
//    private fun getStartMargin(isParentComment: Boolean): Int =
//            if (isParentComment) (COMMENT_START_MARGIN + COMPENSATION_VALUE).dp
//            else (INNER_COMMENT_START_MARGIN + COMPENSATION_VALUE).dp
//
//    private fun setSizeIcon(imageView: UiKitUserpicImage, b: Boolean) {
//        val size = if (b) SIZE_ICON_ZERO_LEVEL else SIZE_ICON_FIRST_LEVEL
//        imageView.newSize(size.dp, size.dp)
//    }

}
