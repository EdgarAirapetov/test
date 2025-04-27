package com.numplates.nomera3.modules.moments.show.presentation.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.longClick
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.moments.show.presentation.adapter.MeeraMomentItemAdapter
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentCarouselItem
import com.numplates.nomera3.modules.moments.show.presentation.view.carousel.MeeraMomentsItemPlaceView
import com.numplates.nomera3.modules.moments.show.presentation.view.carousel.MeeraMomentsItemUserView

open class MeeraMomentItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    open fun setListeners(
        uiModel: MomentCarouselItem? = null,
        listener: MeeraMomentItemAdapter.MomentAdapterClickListener
    ) {
        itemView.setThrottledClickListener { listener.onClick(adapterPos = bindingAdapterPosition, view = itemView) }
        itemView.longClick { listener.onLongClick(bindingAdapterPosition) }
    }
}

/**
 * TODO (BR-13815) add proper logic when backend will be able to return Place cards.
 */
class MeeraMomentPlaceItemHolder(private val momentView: MeeraMomentsItemPlaceView) : MeeraMomentItemViewHolder(momentView) {
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

class MeeraMomentUserItemHolder(private val momentView: MeeraMomentsItemUserView) : MeeraMomentItemViewHolder(momentView) {

    override fun setListeners(
        uiModel: MomentCarouselItem?,
        listener: MeeraMomentItemAdapter.MomentAdapterClickListener
    ) {
        momentView.apply {
            clearListeners()
            if (uiModel is MomentCarouselItem.MomentGroupItem) {
                if (!uiModel.group.isMine) onLongClickMoment { listener.onLongClick(bindingAdapterPosition) }
                if (uiModel.group.moments.isNotEmpty()) {
                    onClickMoment { listener.onClick(adapterPos = bindingAdapterPosition, view = it) }
                }
                onClickProfile { listener.onProfileClick(uiModel.group) }
            }
        }
    }

    fun bindMoment(uiModel: MomentCarouselItem) {
        if (uiModel is MomentCarouselItem.MomentGroupItem) {
            momentView.apply {
                val firstMoment = uiModel.group.moments.firstOrNull()
                toggleType(MeeraMomentsItemUserView.UserMomentType.USER)
                setAvatar(avatarLink = firstMoment?.userAvatarSmall)
                setText(firstMoment?.userName)
                setBackground(uiModel.group.firstNotViewedMomentPreview ?: uiModel.group.latestMomentPreview)
                setWatchedStatus(uiModel.group.isViewed)
                setupViewsByMomentType(MeeraMomentsItemUserView.UserMomentType.USER)
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

class MeeraMomentCreateItemHolder(private val momentView: MeeraMomentsItemUserView) : MeeraMomentItemViewHolder(momentView) {

    override fun setListeners(
        uiModel: MomentCarouselItem?,
        listener: MeeraMomentItemAdapter.MomentAdapterClickListener
    ) {
        momentView.apply {
            clearListeners()
            if (uiModel is MomentCarouselItem.MomentCreateItem) {
                onClickAddMoment { listener.onAddMomentClick() }
                if (uiModel.group.moments.isNotEmpty() && uiModel.group.placeholder == null) {
                    onClickMoment { listener.onClick(adapterPos = bindingAdapterPosition, view = it) }
                } else {
                    onClickMoment { listener.onAddMomentClick() }
                }
            }
        }
    }

    fun bindMoment(uiModel: MomentCarouselItem) {
        if (uiModel is MomentCarouselItem.MomentCreateItem) {
            momentView.apply {
                toggleType(MeeraMomentsItemUserView.UserMomentType.ADD_MOMENT)
                setText(
                    if (uiModel.group.moments.isEmpty()) R.string.moments_carousel_create
                    else R.string.moments_carousel_add
                )
                setupViewsByMomentType(
                    userMomentType = MeeraMomentsItemUserView.UserMomentType.ADD_MOMENT,
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

    private fun MeeraMomentsItemUserView.setupBackground(item: MomentCarouselItem.MomentCreateItem) {
        val group = item.group
        if (group.moments.isEmpty()) {
            setBackground(if (group.placeholder.isNullOrEmpty()) R.drawable.fill_8 else group.placeholder)
        } else {
            setBackground(group.latestMomentPreview)
        }
    }
}

class MeeraShimmerViewHolder(view: View): MeeraMomentItemViewHolder(view) {
    override fun setListeners(
        uiModel: MomentCarouselItem?,
        listener: MeeraMomentItemAdapter.MomentAdapterClickListener
    ) = Unit
}

class MeeraMomentCardUpdatePayload(
    val newBackgroundImage: String?,
    val newIsViewedState: Boolean?
)
