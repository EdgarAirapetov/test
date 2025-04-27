package com.numplates.nomera3.modules.moments.show.presentation.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentEntryPoint
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentCarouselItem
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentCarouselItemType
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupUiModel
import com.numplates.nomera3.modules.moments.show.presentation.view.carousel.MeeraMomentsItemUserView
import com.numplates.nomera3.modules.moments.show.presentation.view.carousel.MomentItemShimmerView
import com.numplates.nomera3.modules.moments.show.presentation.view.carousel.MomentShimmerType
import com.numplates.nomera3.modules.moments.show.presentation.viewholder.MeeraMomentCreateItemHolder
import com.numplates.nomera3.modules.moments.show.presentation.viewholder.MeeraMomentItemViewHolder
import com.numplates.nomera3.modules.moments.show.presentation.viewholder.MeeraMomentUserItemHolder
import com.numplates.nomera3.modules.moments.show.presentation.viewholder.MeeraShimmerViewHolder
import com.numplates.nomera3.modules.moments.show.presentation.viewholder.MomentCardUpdatePayload
import timber.log.Timber

class MeeraMomentItemAdapter(
    private val momentClick: (MomentCarouselItem, Int, View?) -> Unit,
    private val momentLongClick: (MomentCarouselItem) -> Unit,
    private val createMomentClick: (entryPoint: AmplitudePropertyMomentEntryPoint) -> Unit,
    private val onProfileClick: (MomentGroupUiModel) -> Unit,
    diffCallback: DiffUtil.ItemCallback<MomentCarouselItem>,
) : ListAdapter<MomentCarouselItem, MeeraMomentItemViewHolder>(diffCallback) {

    private val listener = MomentAdapterClickListener()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraMomentItemViewHolder {
        return when (viewType) {
            MOMENT_USER_VIEW_TYPE -> MeeraMomentUserItemHolder(
                MeeraMomentsItemUserView(context = parent.context)
            )
            MOMENT_ADD_VIEW_TYPE -> MeeraMomentCreateItemHolder(
                MeeraMomentsItemUserView(context = parent.context)
            )
            MOMENT_SHIMMER_USER_VIEW_TYPE -> MeeraShimmerViewHolder(
                MomentItemShimmerView(
                    context = parent.context,
                    type = MomentShimmerType.UserShimmer
                )
            )
            MOMENT_SHIMMER_PLACE_VIEW_TYPE -> MeeraShimmerViewHolder(
                MomentItemShimmerView(
                    context = parent.context,
                    type = MomentShimmerType.PlaceShimmer
                )
            )
            MOMENT_SHIMMER_BLANK_VIEW_TYPE -> MeeraShimmerViewHolder(
                MomentItemShimmerView(
                    context = parent.context,
                    type = MomentShimmerType.BlankShimmer
                )
            )
            else -> error("Places arent supported yet")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position].displayType) {
            MomentCarouselItemType.CreateMoment -> MOMENT_ADD_VIEW_TYPE
            MomentCarouselItemType.User -> MOMENT_USER_VIEW_TYPE
            MomentCarouselItemType.Place -> MOMENT_PLACE_VIEW_TYPE
            MomentCarouselItemType.UserShimmer -> MOMENT_SHIMMER_USER_VIEW_TYPE
            MomentCarouselItemType.PlaceShimmer -> MOMENT_SHIMMER_PLACE_VIEW_TYPE
            MomentCarouselItemType.BlankShimmer -> MOMENT_SHIMMER_BLANK_VIEW_TYPE
        }
    }

    override fun onBindViewHolder(holder: MeeraMomentItemViewHolder, position: Int) {
        val item = currentList[position]
        when (holder) {
            is MeeraMomentUserItemHolder -> holder.bindMoment(item)
            is MeeraMomentCreateItemHolder -> holder.bindMoment(item)
        }
        holder.setListeners(uiModel = item, listener = listener)
    }

    override fun onBindViewHolder(
        holder: MeeraMomentItemViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            when (holder) {
                is MeeraMomentUserItemHolder -> holder.bindPayload(payloads.first() as MomentCardUpdatePayload)
                is MeeraMomentCreateItemHolder -> holder.bindPayload(payloads.first() as MomentCardUpdatePayload)
            }
        }
    }

    //TODO https://nomera.atlassian.net/browse/BR-26682
    inner class MomentAdapterClickListener {

        fun onAddMomentClick() = createMomentClick.invoke(AmplitudePropertyMomentEntryPoint.MY_CARD)

        fun onProfileClick(momentGroupUiModel: MomentGroupUiModel) = onProfileClick.invoke(momentGroupUiModel)

        fun onClick(adapterPos: Int, view: View?) {
            if (isCarouselItemCorrect(adapterPos)) momentClick.invoke(currentList[adapterPos], adapterPos, view)
        }

        fun onLongClick(adapterPos: Int) {
            if (isCarouselItemCorrect(adapterPos)) momentLongClick.invoke(currentList[adapterPos])
        }

        private fun isCarouselItemCorrect(adapterPos: Int): Boolean {
            if (adapterPos < 0) {
                Timber
                    .tag("Moments carousel")
                    .e("Invalid carousel item index: $adapterPos. Skipping Moment Click callback...")
                return false
            }
            return true
        }
    }

    companion object {
        const val MOMENT_USER_VIEW_TYPE = 0
        const val MOMENT_PLACE_VIEW_TYPE = 1
        const val MOMENT_ADD_VIEW_TYPE = 10
        const val MOMENT_SHIMMER_USER_VIEW_TYPE = 100
        const val MOMENT_SHIMMER_PLACE_VIEW_TYPE = 111
        const val MOMENT_SHIMMER_BLANK_VIEW_TYPE = 122
    }
}
