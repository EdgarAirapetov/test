package com.numplates.nomera3.modules.peoples.ui.content.holder

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.FIRST_POST_ID
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemBloggerImageContentBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.entity.blogger.BloggerMediaContentUiEntity
import com.numplates.nomera3.presentation.view.holder.BaseItemViewHolder
import timber.log.Timber

class BloggerContentPlaceholderViewHolder(
    private val binding: ItemBloggerImageContentBinding,
    private val actionListener: (FriendsContentActions) -> Unit
) : BaseItemViewHolder<BloggerMediaContentUiEntity.BloggerContentPlaceholderUiEntity, ItemBloggerImageContentBinding>(
    binding
) {
    init {
        initListeners()
    }

    override fun bind(item: BloggerMediaContentUiEntity.BloggerContentPlaceholderUiEntity) {
        super.bind(item)
        binding.vgMediaPlaceholder.visible()
        setPlaceholderData(item)
    }

    private fun setPlaceholderData(item: BloggerMediaContentUiEntity.BloggerContentPlaceholderUiEntity) {
        binding.tvBloggerMediaPlaceholder.text = item.placeholderText
        binding.ivBloggerMediaPlaceholder.loadGlide(getDrawablePlaceholder())
    }

    private fun getDrawablePlaceholder(): Drawable? {
        return try {
            ContextCompat.getDrawable(binding.root.context, R.drawable.ic_gallery_road)
        } catch (e: Resources.NotFoundException) {
            Timber.e(e)
            null
        }
    }

    private fun initListeners() {
        binding.root.setThrottledClickListener {
            val rootUserId = item?.userId ?: 0
            val user = item?.user ?: return@setThrottledClickListener
            actionListener.invoke(
                FriendsContentActions.OnMediaPlaceholderClicked(
                    userId = rootUserId,
                    postId = getFirstPostId(),
                    user = user
                )
            )
        }
    }

    /**
     * Данный метод будет возвращать 1 id поста
     * (т.е по такой логике в профиле определяется, что если postId == 1
     * будет осуществляться скролл к первому посту.
     */
    private fun getFirstPostId(): Long = FIRST_POST_ID
}
