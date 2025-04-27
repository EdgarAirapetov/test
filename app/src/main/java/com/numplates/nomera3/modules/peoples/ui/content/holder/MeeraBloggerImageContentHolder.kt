package com.numplates.nomera3.modules.peoples.ui.content.holder

import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlideWithCallback
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.databinding.MeeraItemBloggerImageContentBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.entity.blogger.BloggerMediaContentUiEntity
import com.numplates.nomera3.presentation.view.holder.BaseItemViewHolder

class MeeraBloggerImageContentHolder(
    private val binding: MeeraItemBloggerImageContentBinding,
    private val actionListener: (FriendsContentActions) -> Unit
) : BaseItemViewHolder<BloggerMediaContentUiEntity.BloggerImageContentUiEntity, MeeraItemBloggerImageContentBinding>(binding) {

    init {
        initListeners()
    }

    override fun bind(item: BloggerMediaContentUiEntity.BloggerImageContentUiEntity) {
        super.bind(item)
        loadBloggerImage(item.imageUrl)
    }

    private fun loadBloggerImage(imageUrl: String) {
        binding.vgMediaContentShimmer.visible()
        binding.ivBloggerImage.loadGlideWithCallback(imageUrl) {
            binding.vgMediaContentShimmer.gone()
        }
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

