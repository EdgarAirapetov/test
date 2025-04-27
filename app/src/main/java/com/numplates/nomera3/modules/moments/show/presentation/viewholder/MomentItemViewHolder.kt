package com.numplates.nomera3.modules.moments.show.presentation.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.longClick
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.moments.show.presentation.adapter.MomentItemAdapter
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentCarouselItem
import com.numplates.nomera3.modules.moments.show.presentation.view.carousel.MomentsItemPlaceView
import com.numplates.nomera3.modules.moments.show.presentation.view.carousel.MomentsItemUserView

open class MomentItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    open fun setListeners(
        uiModel: MomentCarouselItem? = null,
        listener: MomentItemAdapter.MomentAdapterClickListener?
    ) {
        itemView.setThrottledClickListener { listener?.onClick(adapterPos = bindingAdapterPosition, view = itemView) }
        itemView.longClick { listener?.onLongClick(bindingAdapterPosition) }
    }

    open fun clearResources() {
        itemView.setOnClickListener(null)
        itemView.setOnLongClickListener(null)
    }
}

/**
 * TODO (BR-13815) add proper logic when backend will be able to return Place cards.
 */
class MomentPlaceItemHolder(private val momentView: MomentsItemPlaceView) : MomentItemViewHolder(momentView) {
    fun bindMoment(uiModel: MomentCarouselItem) {
        momentView.apply {
            when (uiModel) {
                is MomentCarouselItem.MomentGroupItem -> {
                    setName("TODO")
                    setDistance("TODO")
                    setBackground(uiModel.group.firstNotViewedMomentPreview ?: uiModel.group.latestMomentPreview)
                    setWatchedStatus(uiModel.group.isViewed)
                }
                else -> Unit
            }
        }
    }
}

class MomentUserItemHolder(private val momentView: MomentsItemUserView) : MomentItemViewHolder(momentView) {

    override fun setListeners(
        uiModel: MomentCarouselItem?,
        listener: MomentItemAdapter.MomentAdapterClickListener?
    ) {
        momentView.apply {
            if (uiModel is MomentCarouselItem.MomentGroupItem) {
                if (!uiModel.group.isMine) onLongClickMoment { listener?.onLongClick(bindingAdapterPosition) }
                if (uiModel.group.moments.isNotEmpty()) {
                    onClickMoment { listener?.onClick(adapterPos = bindingAdapterPosition, view = it) }
                }
                onClickProfile { listener?.onProfileClick(uiModel.group) }
            }
        }
    }

    override fun clearResources() {
        momentView.clearListeners()
        super.clearResources()
    }

    fun bindMoment(uiModel: MomentCarouselItem) {
        if (uiModel is MomentCarouselItem.MomentGroupItem) {
            momentView.apply {
                val firstMoment = uiModel.group.moments.firstOrNull()
                toggleType(MomentsItemUserView.UserMomentType.USER)
                setAvatar(
                    avatarLink = firstMoment?.userAvatarSmall,
                    accountType = firstMoment?.userAccountType
                )
                setText(firstMoment?.userName)
                setBackground(uiModel.group.firstNotViewedMomentPreview ?: uiModel.group.latestMomentPreview)
                setWatchedStatus(uiModel.group.isViewed)
                setupViewsByMomentType(MomentsItemUserView.UserMomentType.USER)
            }
        }
    }

    fun bindPayload(payload: MomentCardUpdatePayload) {
        momentView.apply {
            payload.newBackgroundImage?.let { setBackground(it) }
            payload.newIsViewedState?.let { setWatchedStatus(it) }
        }
    }
}

class MomentCreateItemHolder(private val momentView: MomentsItemUserView) : MomentItemViewHolder(momentView) {

    override fun setListeners(
        uiModel: MomentCarouselItem?,
        listener: MomentItemAdapter.MomentAdapterClickListener?
    ) {
        momentView.apply {
            if (uiModel is MomentCarouselItem.MomentCreateItem) {
                onClickAddMoment { listener?.onAddMomentClick() }
                if (uiModel.group.moments.isNotEmpty() && uiModel.group.placeholder == null) {
                    onClickMoment { listener?.onClick(adapterPos = bindingAdapterPosition, view = it) }
                } else {
                    onClickMoment { listener?.onAddMomentClick() }
                }
            }
        }
    }

    override fun clearResources() {
        momentView.clearListeners()
        super.clearResources()
    }

    fun bindMoment(uiModel: MomentCarouselItem) {
        if (uiModel is MomentCarouselItem.MomentCreateItem) {
            momentView.apply {
                toggleType(MomentsItemUserView.UserMomentType.ADD_MOMENT)
                setText(
                    if (uiModel.group.moments.isEmpty()) R.string.moments_carousel_create
                    else R.string.moments_carousel_add
                )
                setupViewsByMomentType(
                    userMomentType = MomentsItemUserView.UserMomentType.ADD_MOMENT,
                    hasMoments = uiModel.group.moments.isNotEmpty()
                )
                getContentView().post { setupBackground(uiModel) }
                setWatchedStatus(uiModel.group.isViewed)
            }
        }
    }

    fun bindPayload(payload: MomentCardUpdatePayload) {
        momentView.apply {
            payload.newBackgroundImage?.let { setBackground(it) }
        }
    }

    private fun MomentsItemUserView.setupBackground(item: MomentCarouselItem.MomentCreateItem) {
        val group = item.group
        if (group.moments.isEmpty()) {
            setBackground(if (group.placeholder.isNullOrEmpty()) R.drawable.fill_8 else group.placeholder)
        } else {
            setBackground(group.latestMomentPreview)
        }
    }
}

class ShimmerViewHolder(view: View): MomentItemViewHolder(view) {
    override fun setListeners(
        uiModel: MomentCarouselItem?,
        listener: MomentItemAdapter.MomentAdapterClickListener?
    ) = Unit
}

class MomentCardUpdatePayload(
    val newBackgroundImage: String?,
    val newIsViewedState: Boolean?
)
