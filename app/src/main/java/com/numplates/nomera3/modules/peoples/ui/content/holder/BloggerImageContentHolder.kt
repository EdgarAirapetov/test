package com.numplates.nomera3.modules.peoples.ui.content.holder

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.ItemBloggerImageContentBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.entity.blogger.BloggerMediaContentUiEntity
import com.numplates.nomera3.presentation.view.holder.BaseItemViewHolder

class BloggerImageContentHolder(
    private val binding: ItemBloggerImageContentBinding,
    private val actionListener: (FriendsContentActions) -> Unit
) : BaseItemViewHolder<BloggerMediaContentUiEntity.BloggerImageContentUiEntity, ItemBloggerImageContentBinding>(
    binding
) {

    init {
        initListeners()
    }

    override fun bind(item: BloggerMediaContentUiEntity.BloggerImageContentUiEntity) {
        super.bind(item)
        loadBloggerImage(item.imageUrl)
    }

    private fun loadBloggerImage(imageUrl: String) {
        Glide.with(binding.root.context)
            .load(imageUrl)
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.ivBloggerImage)
    }

    private fun initListeners() {
        binding.root.setThrottledClickListener {
            val entity = item ?: return@setThrottledClickListener
            actionListener.invoke(
                FriendsContentActions.OnImagePostClicked(
                    entity = entity
                )
            )
        }
    }
}
