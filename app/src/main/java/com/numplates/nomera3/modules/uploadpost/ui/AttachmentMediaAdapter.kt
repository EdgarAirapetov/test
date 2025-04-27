package com.numplates.nomera3.modules.uploadpost.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.getScreenWidth
import com.numplates.nomera3.databinding.ItemMediaAttachmentBinding
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentMediaModel

class AttachmentMediaAdapter(
    val imagePositioningListener: (imagePositioningInProcess: Boolean) -> Unit,
): RecyclerView.Adapter<AttachmentMediaAdapter.ViewHolder>() {

    private var mediaPreviewStrictHeight = 0
    private var mediaPreviewStrictWidth = 0
    var items: List<UIAttachmentMediaModel> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var actions: AttachmentMediaActions? = null
    fun setAttachmentActions(attachmentMediaActions: AttachmentMediaActions) {
        actions = attachmentMediaActions
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMediaAttachmentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            items[position],
            actions,
            mediaPreviewStrictHeight,
            mediaPreviewStrictWidth,
            imagePositioningListener
        )
    }
    override fun getItemCount() = items.size
    fun setStrictMeasures(mediaPreviewStrictHeight: Int, mediaPreviewStrictWidth: Int) {
        this.mediaPreviewStrictHeight = mediaPreviewStrictHeight
        this.mediaPreviewStrictWidth = mediaPreviewStrictWidth
    }

    class ViewHolder(
        private val binding: ItemMediaAttachmentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: UIAttachmentMediaModel,
            actions: AttachmentMediaActions?,
            mediaPreviewStrictHeight: Int,
            mediaPreviewStrictWidth: Int,
            imagePositioningListener: (imagePositioningInProcess: Boolean) -> Unit
        ) {
            binding.addMediaAttachmentItemView.apply {
                bind(
                    actions = actions,
                    attachment = item,

                    mediaPreviewMaxWidth = getScreenWidth(),
                    mediaPreviewMaxHeight = mediaPreviewStrictHeight,

                    attachmentStrictWidth = mediaPreviewStrictWidth,
                    attachmentStrictHeight = mediaPreviewStrictHeight
                )
                initImagePositioningInProcessListener(imagePositioningListener)
            }
        }
    }

}

