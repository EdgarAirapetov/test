package com.numplates.nomera3.modules.chat.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemChatAttachmentBinding
import com.numplates.nomera3.modules.chat.ui.entity.ChatAttachmentUiModel
import java.util.Locale
import java.util.concurrent.TimeUnit

class ChatAttachmentsAdapter(
    private val deleteAttachmentListener: (String) -> Unit,
) : ListAdapter<ChatAttachmentUiModel, ChatAttachmentsAdapter.ChatAttachmentViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatAttachmentViewHolder {
        val binding = ItemChatAttachmentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatAttachmentViewHolder(binding, deleteAttachmentListener)
    }

    override fun onBindViewHolder(holder: ChatAttachmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChatAttachmentViewHolder(
        private val binding: ItemChatAttachmentBinding,
        private val listener: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatAttachmentUiModel) = with(binding) {
            btnDeleteAttachment.setThrottledClickListener { listener.invoke(item.url) }

            Glide.with(itemView.context.applicationContext)
                .load(item.url.trim())
                .apply(
                    RequestOptions().centerCrop()
                        .placeholder(R.drawable.ic_gallery)
                        .error(R.drawable.img_error)
                )
                .into(ivImage)

            if (item.duration != null) {
                clVideo.visible()
                tvVideoTime.text = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(item.duration),
                    TimeUnit.MILLISECONDS.toSeconds(item.duration) % 60
                )
            } else {
                clVideo.gone()
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ChatAttachmentUiModel>() {
            override fun areItemsTheSame(oldItem: ChatAttachmentUiModel, newItem: ChatAttachmentUiModel): Boolean {
                return oldItem.url == newItem.url
            }

            override fun areContentsTheSame(oldItem: ChatAttachmentUiModel, newItem: ChatAttachmentUiModel): Boolean {
                return oldItem.url == newItem.url
                    && oldItem.duration == newItem.duration
            }
        }
    }
}
