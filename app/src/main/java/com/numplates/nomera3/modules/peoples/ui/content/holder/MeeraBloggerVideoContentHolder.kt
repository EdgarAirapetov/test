package com.numplates.nomera3.modules.peoples.ui.content.holder

import android.view.View
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.google.android.exoplayer2.ui.PlayerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlideWithCallback
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.utils.getDurationSeconds
import com.numplates.nomera3.databinding.MeeraItemBloggerMediaContentBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.entity.blogger.BloggerMediaContentUiEntity
import com.numplates.nomera3.modules.peoples.ui.utils.BloggerVideoPlayHandler
import com.numplates.nomera3.presentation.view.holder.BaseItemViewHolder

class MeeraBloggerVideoContentHolder(
    private val binding: MeeraItemBloggerMediaContentBinding,
    private val actionListener: (FriendsContentActions) -> Unit
) : BaseItemViewHolder<BloggerMediaContentUiEntity.BloggerVideoContentUiEntity, MeeraItemBloggerMediaContentBinding>(
    binding
), BloggerVideoPlayHandler {

    init {
        initListeners()
    }

    override fun getPlayerView(): PlayerView = binding.pvVideoBloggerContent

    override fun getVideoUrlString(): String? = item?.videoUrl

    override fun getThumbnail(): ImageView = binding.ivPreview

    override fun getStaticDurationView(): View = binding.cvMediaContentDuration

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
        binding.vgMediaContentShimmer.visible()
        binding.ivPreview.loadGlideWithCallback(previewUrl) {
            binding.vgMediaContentShimmer.gone()
        }
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
        binding.cvMediaContentDuration.text = getDurationSeconds(duration)
    }
}
