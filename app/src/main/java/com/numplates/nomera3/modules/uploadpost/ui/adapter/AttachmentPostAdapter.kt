package com.numplates.nomera3.modules.uploadpost.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.modules.uploadpost.ui.AttachmentPostActions
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentPostModel
import com.numplates.nomera3.modules.uploadpost.ui.viewholder.AttachmentPostHolder

class AttachmentPostAdapter(
    private val actions: AttachmentPostActions
): RecyclerView.Adapter<AttachmentPostHolder>() {

    private val asyncDiffer =
        AsyncListDiffer(this,
            object : DiffUtil.ItemCallback<UIAttachmentPostModel>() {
                override fun areItemsTheSame(
                    oldItem: UIAttachmentPostModel,
                    newItem: UIAttachmentPostModel
                ): Boolean = oldItem == newItem

                override fun areContentsTheSame(
                    oldItem: UIAttachmentPostModel,
                    newItem: UIAttachmentPostModel
                ): Boolean = oldItem == newItem
            })

    fun submitList(items: List<UIAttachmentPostModel>) = asyncDiffer.submitList(items)

    override fun getItemCount() =
        asyncDiffer.currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AttachmentPostHolder(parent, actions)

    override fun onBindViewHolder(holder: AttachmentPostHolder, position: Int) =
        holder.bind(asyncDiffer.currentList[position])

}
