package com.numplates.nomera3.modules.feed.ui.viewholder

import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentEntryPoint
import com.numplates.nomera3.modules.feed.ui.PostCallback
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoCarouselUiModel
import com.numplates.nomera3.modules.moments.show.presentation.MomentCallback
import com.numplates.nomera3.modules.moments.show.presentation.view.carousel.MomentsItemRecyclerView
import com.numplates.nomera3.presentation.view.widgets.VipView
import kotlinx.coroutines.launch

class MomentsViewHolder(
    private val view: View,
    postCallback: PostCallback,
    val momentCallback: MomentCallback,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.ViewHolder(view) {

    private var currentMoments: MomentInfoCarouselUiModel? = null
    private val momentCardsRecyclerView = view.findViewById<MomentsItemRecyclerView>(R.id.rv_moments_list)
    private val vgAddMomentContainer = view.findViewById<FrameLayout>(R.id.vg_add_moment_container)
    private val vvAddMomentAvatar = view.findViewById<VipView>(R.id.vv_add_moment_avatar)

    init {
        momentCardsRecyclerView.setListeners(postCallback, momentCallback)
        momentCardsRecyclerView.isNestedScrollingEnabled = false
    }

    fun updatePayload(post: UIPostUpdate.UpdateMoments) {
        currentMoments = post.moments
        momentCardsRecyclerView.scrollToWatchedMomentCard(post.scrollToGroupId)
        momentCardsRecyclerView.submitMoments(post.moments)
        setupUserAvatar(post.momentsBlockAvatar)
    }

    fun bind(post: PostUIEntity) {
        currentMoments = post.moments
        momentCardsRecyclerView.submitMoments(post.moments)
        setupListeners()
        setupUserAvatar(post.momentsBlockAvatar)
    }

    fun scrollMomentsToStart() {
        momentCardsRecyclerView.scrollToStart()
    }

    private fun setupListeners() {
        momentCardsRecyclerView.addOnScrollListener(MomentCarouselScrollListener())
        vgAddMomentContainer.setThrottledClickListener {
            momentCardsRecyclerView.notifyCreateMomentListeners(AmplitudePropertyMomentEntryPoint.BUTTON_AFTER_SCROLL)
        }
    }

    private fun setupUserAvatar(avatarLink: String?) {
        lifecycleOwner.lifecycleScope.launch {
            vvAddMomentAvatar.setUp(context = view.context, avatarLink = avatarLink)
            vvAddMomentAvatar.hideHolidayHat()
            vvAddMomentAvatar.darkAvatar()
        }
    }

    private inner class MomentCarouselScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (currentMoments?.useCreateMomentItem == false) return
            val currentFirstVisible = momentCardsRecyclerView.layoutManager?.findFirstVisibleItemPosition()
            val visibleState = currentFirstVisible != 0
            if (vgAddMomentContainer.isVisible == visibleState) return
            vgAddMomentContainer.isVisible = visibleState
        }
    }
}
