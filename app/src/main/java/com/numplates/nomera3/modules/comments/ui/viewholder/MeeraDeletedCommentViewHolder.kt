package com.numplates.nomera3.modules.comments.ui.viewholder

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp
import com.meera.core.extensions.setMargins
import com.meera.uikit.widgets.userpic.UiKitUserpicImage
import com.meera.uikit.widgets.userpic.UserpicSizeEnum
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemCommentDeletedByPostAuthorBinding
import com.numplates.nomera3.modules.comments.ui.entity.DeletedCommentEntity

class MeeraDeletedCommentViewHolder(
    private val binding: ItemCommentDeletedByPostAuthorBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(model: DeletedCommentEntity?) {
        setCommentDeletionReason(model)
        setStartMargin(model)
    }

    private fun setStartMargin(model: DeletedCommentEntity?) {
        getStartMargin(isParentComment(model)).also { startMargin ->
            binding.root.findViewById<UiKitUserpicImage>(R.id.userPhotoStub)?.apply {
                setSizeIcon(this, isParentComment(model))
                setMargins(start = startMargin)
            }
        }
    }

    private fun setCommentDeletionReason(model: DeletedCommentEntity?) {
        model?.stringResId
                ?.let { binding.root.context.getString(it) }
                ?.takeIf { it.isNotBlank() }
                ?.also { binding.root.findViewById<TextView>(R.id.deletedCommentReason)?.text = it }
    }

    private fun isParentComment(model: DeletedCommentEntity?): Boolean =
            model?.comment?.parentId == null

    private fun getStartMargin(isParentComment: Boolean): Int =
            if (isParentComment) AUTHOR_COMMENT_START_MARGIN.dp
            else (AUTHOR_COMMENT_START_MARGIN + REPLAY_COMMENT_START_MARGIN).dp

    private fun setSizeIcon(userPicImage: UiKitUserpicImage, b: Boolean) {
        val size = if (b) UserpicSizeEnum.Size40 else UserpicSizeEnum.Size24
        userPicImage.setConfig(UserpicUiModel(size = size))
    }

}
