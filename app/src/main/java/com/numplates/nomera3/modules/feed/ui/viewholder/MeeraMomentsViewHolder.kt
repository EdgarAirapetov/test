package com.numplates.nomera3.modules.feed.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.glideClear
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlideCircle
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.databinding.MeeraMomentsPostViewholderBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentEntryPoint
import com.numplates.nomera3.modules.feed.ui.MeeraPostCallback
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoCarouselUiModel
import com.numplates.nomera3.modules.moments.show.presentation.MomentCallback

const val ADD_MOMENT_CONTAINER_ANIMATION_TIME = 200L

class MeeraMomentsViewHolder(
    private val binding: MeeraMomentsPostViewholderBinding
) : RecyclerView.ViewHolder(binding.root) {

    private var currentMoments: MomentInfoCarouselUiModel? = null
    private var addMomentIsVisible = false
    private var momentCarouselScrollListener : MomentCarouselScrollListener? = null
    private var postCallback : MeeraPostCallback? = null
    private var momentCallback : MomentCallback? = null

    init {
        binding.rvMomentsList.isNestedScrollingEnabled = false
    }

    fun initCallbacks(postCallback: MeeraPostCallback?, momentCallback: MomentCallback?) {
        this.postCallback = postCallback
        this.momentCallback = momentCallback
    }

    fun updatePayload(post: UIPostUpdate.UpdateMoments) {
        currentMoments = post.moments
        binding.rvMomentsList.scrollToWatchedMomentCard(post.scrollToGroupId)
        binding.rvMomentsList.submitMoments(post.moments)
        setupUserAvatar(post.momentsBlockAvatar)
    }

    fun bind(post: PostUIEntity) {
        currentMoments = post.moments
        binding.rvMomentsList.submitMoments(post.moments)
        binding.rvMomentsList.setListeners(postCallback, momentCallback)
        setupListeners()
        setupUserAvatar(post.momentsBlockAvatar)
    }

    fun scrollMomentsToStart(smoothScroll: Boolean) {
        binding.rvMomentsList.scrollToStart(smoothScroll)
    }

     fun clearResource() {
        momentCarouselScrollListener?.let { listener ->
            binding.rvMomentsList.removeOnScrollListener(listener)
            momentCarouselScrollListener = null
        }
         binding.vgAddMomentContainer.setOnClickListener(null)
         binding.vvAddMomentAvatar.glideClear()
         postCallback = null
         momentCallback = null
    }

    private fun setupListeners() {
        momentCarouselScrollListener = MomentCarouselScrollListener().also { listener ->
            binding.rvMomentsList.addOnScrollListener(listener)
        }

        binding.vgAddMomentContainer.setThrottledClickListener {
            binding.rvMomentsList.notifyCreateMomentListeners(AmplitudePropertyMomentEntryPoint.BUTTON_AFTER_SCROLL)
        }
    }

    private fun setupUserAvatar(avatarLink: String?) {
        binding.vvAddMomentAvatar.loadGlideCircle(avatarLink)
    }

    private inner class MomentCarouselScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (currentMoments?.useCreateMomentItem == false) return
            val currentFirstVisible = binding.rvMomentsList.layoutManager?.findFirstVisibleItemPosition()
            val visibleState = currentFirstVisible != 0
            animateAddMomentContainer(toVisible = visibleState)
        }
    }

    private fun animateAddMomentContainer(toVisible: Boolean) {
        with(binding) {
            if (addMomentIsVisible == toVisible) return
            val width = vgAddMomentContainer.width.toFloat()
            if (toVisible) {
                vgAddMomentContainer.translationX = -width
                vgAddMomentContainer.visible()
                vgAddMomentContainer.animate().translationX(0f).setDuration(ADD_MOMENT_CONTAINER_ANIMATION_TIME).start()
            } else {
                vgAddMomentContainer.animate().translationX(-width).setDuration(ADD_MOMENT_CONTAINER_ANIMATION_TIME).withEndAction {
                    vgAddMomentContainer.gone()
                }
            }
            addMomentIsVisible = toVisible
        }
    }
}
