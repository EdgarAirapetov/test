package com.numplates.nomera3.modules.uploadpost.ui.viewholder

import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.Target
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.BaseViewHolder
import com.numplates.nomera3.modules.uploadpost.ui.AttachmentPostActions
import com.numplates.nomera3.modules.uploadpost.ui.data.AttachmentPostType
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentPostModel
import com.meera.core.extensions.loadGlideCustomSize
import com.meera.core.extensions.loadGlideCustomSizeAsBitmap


private const val DEFAULT_IMAGE_HEIGHT = 144

class AttachmentPostHolder(
    private val parent: ViewGroup,
    private val actions: AttachmentPostActions
): BaseViewHolder(parent, R.layout.item_create_post_attachment) {

    private val ivMediaAttachment: ImageView = itemView.findViewById(R.id.iv_media_attachment)
    private val ivDeleteAttachment: ImageView = itemView.findViewById(R.id.iv_delete_attachment)
    private val ivEditAttachment: ImageView = itemView.findViewById(R.id.iv_edit_attachment)
    private val cvPlayBtn: CardView = itemView.findViewById(R.id.cv_play_btn)

    fun bind(attachment: UIAttachmentPostModel){
        ivMediaAttachment.visible()
        ivDeleteAttachment.visible()
        when (attachment.type) {
            AttachmentPostType.ATTACHMENT_PHOTO,
            AttachmentPostType.ATTACHMENT_PREVIEW-> setupImageAttachment(attachment)
            AttachmentPostType.ATTACHMENT_GIF -> setupGifAttachment(attachment)
            AttachmentPostType.ATTACHMENT_VIDEO -> setupVideoAttachment(attachment)
        }
        initClickListeners(attachment)
    }

    private fun initClickListeners(attachment: UIAttachmentPostModel) {
        ivMediaAttachment.click { actions.onItemClicked(attachment) }
        cvPlayBtn.click { actions.onItemClicked(attachment) }
        ivEditAttachment.click { actions.onItemEditClick(attachment) }
        ivDeleteAttachment.click { actions.onItemCloseClick(attachment) }
    }

    private fun calculateAttachmentHeight() = DEFAULT_IMAGE_HEIGHT.dp

    private fun calculateAttachmentWeight(attachment: UIAttachmentPostModel) =
        (attachment.attachmentWidth * calculateAttachmentHeight() / attachment.attachmentHeight)

    private fun setupGifAttachment(attachment: UIAttachmentPostModel) {
        ivMediaAttachment.loadGlideCustomSizeAsBitmap(
            attachment.attachmentResource,
            calculateAttachmentWeight(attachment),
            calculateAttachmentHeight()
        )
        cvPlayBtn.visible()
        ivEditAttachment.gone()
    }

    private fun setupImageAttachment(attachment: UIAttachmentPostModel) {
        ivMediaAttachment.loadGlideCustomSize(
            attachment.attachmentResource,
            calculateAttachmentWeight(attachment),
            calculateAttachmentHeight()
        )
        cvPlayBtn.gone()
        ivEditAttachment.visible()
    }

    private fun setupVideoAttachment(attachment: UIAttachmentPostModel) {
        Glide.with(ivMediaAttachment.context)
            .load(attachment.attachmentResource)
            .transition(DrawableTransitionOptions.withCrossFade())
            .override(Target.SIZE_ORIGINAL)
            .into(ivMediaAttachment)
        cvPlayBtn.visible()
        ivEditAttachment.visible()
    }

}
