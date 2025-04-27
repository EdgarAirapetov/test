package com.numplates.nomera3.modules.peoples.ui.content.holder

import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.FIRST_POST_ID
import com.numplates.nomera3.databinding.MeeraItemBloggerImageContentBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.entity.blogger.BloggerMediaContentUiEntity
import com.numplates.nomera3.presentation.view.holder.BaseItemViewHolder

class MeeraBloggerContentPlaceholderViewHolder(
    private val binding: MeeraItemBloggerImageContentBinding,
    private val actionListener: (FriendsContentActions) -> Unit
) : BaseItemViewHolder<BloggerMediaContentUiEntity.BloggerContentPlaceholderUiEntity, MeeraItemBloggerImageContentBinding>(
    binding
) {
    init {
        initListeners()
    }

    override fun bind(item: BloggerMediaContentUiEntity.BloggerContentPlaceholderUiEntity) {
        super.bind(item)
        binding.vgMediaPlaceholder.visible()
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
