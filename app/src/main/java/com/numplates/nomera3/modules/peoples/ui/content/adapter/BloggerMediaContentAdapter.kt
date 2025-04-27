package com.numplates.nomera3.modules.peoples.ui.content.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.databinding.ItemBloggerImageContentBinding
import com.numplates.nomera3.databinding.ItemBloggerMediaContentBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.compare
import com.numplates.nomera3.modules.peoples.ui.content.entity.blogger.BloggerMediaContentUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.holder.BloggerContentPlaceholderViewHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.BloggerImageContentHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.BloggerVideoContentHolder
import com.numplates.nomera3.presentation.view.utils.inflateBinding
import timber.log.Timber

class BloggerMediaContentAdapter(
    private val actionListener: (FriendsContentActions) -> Unit
) : ListAdapter<BloggerMediaContentUiEntity, RecyclerView.ViewHolder>(BloggerMediaContentDiff()) {

    override fun getItemViewType(position: Int): Int {
        return currentList[position].getItemViewType.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            BloggerMediaViewType.BLOGGER_VIDEO_MEDIA_CONTENT.ordinal -> {
                createBloggerVideoMediaContent(parent)
            }
            BloggerMediaViewType.BLOGGER_IMAGE_MEDIA_CONTENT.ordinal -> {
                createBloggerImageMediaContent(parent)
            }
            BloggerMediaViewType.BLOGGER_PLACEHOLDER.ordinal -> {
                createBloggerContentPlaceholderViewHolder(parent)
            }
            else -> throw IllegalArgumentException("Unknown view type!")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BloggerVideoContentHolder -> {
                holder.bind(currentList[position] as BloggerMediaContentUiEntity.BloggerVideoContentUiEntity)
            }
            is BloggerContentPlaceholderViewHolder -> {
                holder.bind(currentList[position] as BloggerMediaContentUiEntity.BloggerContentPlaceholderUiEntity)
            }
            is BloggerImageContentHolder -> {
                holder.bind(currentList[position] as BloggerMediaContentUiEntity.BloggerImageContentUiEntity)
            }
        }
    }

    fun getItemByPosition(position: Int) = try {
        currentList[position]
    } catch (e: Exception) {
        Timber.e(e)
        null
    }

    private fun createBloggerVideoMediaContent(viewGroup: ViewGroup): BloggerVideoContentHolder {
        return BloggerVideoContentHolder(
            binding = viewGroup.inflateBinding(ItemBloggerMediaContentBinding::inflate),
            actionListener = actionListener
        )
    }

    private fun createBloggerImageMediaContent(viewGroup: ViewGroup): BloggerImageContentHolder {
        return BloggerImageContentHolder(
            binding = viewGroup.inflateBinding(ItemBloggerImageContentBinding::inflate),
            actionListener = actionListener
        )
    }

    private fun createBloggerContentPlaceholderViewHolder(
        viewGroup: ViewGroup
    ): BloggerContentPlaceholderViewHolder {
        return BloggerContentPlaceholderViewHolder(
            binding = viewGroup.inflateBinding(ItemBloggerImageContentBinding::inflate),
            actionListener = actionListener
        )
    }

    private class BloggerMediaContentDiff : DiffUtil.ItemCallback<BloggerMediaContentUiEntity>() {

        override fun areItemsTheSame(
            oldItem: BloggerMediaContentUiEntity,
            newItem: BloggerMediaContentUiEntity
        ): Boolean = oldItem.compare(newItem)

        override fun areContentsTheSame(
            oldItem: BloggerMediaContentUiEntity,
            newItem: BloggerMediaContentUiEntity
        ): Boolean = oldItem.compare(newItem)
    }
}

enum class BloggerMediaViewType {
    BLOGGER_VIDEO_MEDIA_CONTENT,
    BLOGGER_IMAGE_MEDIA_CONTENT,
    BLOGGER_PLACEHOLDER
}
