package com.numplates.nomera3.modules.peoples.ui.content.holder

import android.view.View
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.exoplayer2.ui.PlayerView
import com.meera.core.extensions.GLIDE_THUMBNAIL_SIZE_MULTIPLIER
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.utils.getDurationSeconds
import com.numplates.nomera3.databinding.ItemBloggerMediaContentBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.entity.blogger.BloggerMediaContentUiEntity
import com.numplates.nomera3.modules.peoples.ui.utils.BloggerVideoPlayHandler
import com.numplates.nomera3.presentation.view.holder.BaseItemViewHolder

class BloggerVideoContentHolder(
    private val binding: ItemBloggerMediaContentBinding,
    private val actionListener: (FriendsContentActions) -> Unit
) : BaseItemViewHolder<BloggerMediaContentUiEntity.BloggerVideoContentUiEntity, ItemBloggerMediaContentBinding>(
    binding
), BloggerVideoPlayHandler {

    init {
        initListeners()
    }

    override fun getPlayerView(): PlayerView = binding.pvVideoBloggerContent

    override fun getVideoUrlString(): String? = item?.videoUrl

    override fun getThumbnail(): ImageView = binding.ivPreview

    override fun getStaticDurationView(): View = binding.vVideoDuration.root

    override fun getRoot(): CardView = binding.vgBloggerVideoRoot

    override fun getItemView(): View = binding.root

    override fun bind(item: BloggerMediaContentUiEntity.BloggerVideoContentUiEntity) {
        super.bind(item)
        handleMediaType(item)
    }

    private fun handleMediaType(item: BloggerMediaContentUiEntity.BloggerVideoContentUiEntity) {
        loadPreviewGlide(item.preview)
        setVideoDuration(item.videoDuration)
    }

    private fun loadPreviewGlide(previewUrl: String?) {
        Glide.with(binding.root.context)
            .load(previewUrl)
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .thumbnail(GLIDE_THUMBNAIL_SIZE_MULTIPLIER)
            .into(binding.ivPreview)
    }

    private fun initListeners() {
        binding.root.setThrottledClickListener {
            val entity = item ?: return@setThrottledClickListener
            actionListener.invoke(
                FriendsContentActions.OnVideoPostClicked(
                    entity = entity
                )
            )
        }
    }

    private fun setVideoDuration(duration: Int) {
        binding.vVideoDuration.exoPosition.text = getDurationSeconds(duration)
    }
}
