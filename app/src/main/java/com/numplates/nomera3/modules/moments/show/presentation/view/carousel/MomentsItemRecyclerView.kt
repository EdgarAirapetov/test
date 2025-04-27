package com.numplates.nomera3.modules.moments.show.presentation.view.carousel

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dpToPx
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentEntryPoint
import com.numplates.nomera3.modules.feed.ui.PostCallback
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoCarouselUiModel
import com.numplates.nomera3.modules.moments.show.data.entity.MomentPagingParams.Companion.FORBID_PAGING_TICKET
import com.numplates.nomera3.modules.moments.show.presentation.MomentCallback
import com.numplates.nomera3.modules.moments.show.presentation.MomentRecyclerPaginationListener
import com.numplates.nomera3.modules.moments.show.presentation.adapter.MomentItemAdapter
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentCarouselItem
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupUiModel
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.modules.moments.show.presentation.viewholder.MomentCardUpdatePayload

class MomentsItemRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    companion object {
        private const val AMOUNT_OF_MOMENTS_ON_SCREEN = 3.5
        private const val DEFAULT_PENDING_PAGING_TICKET = "DEFAULT_PENDING_PAGING_TICKET"
    }

    var currentPagingTicket: String? = null
        private set
    private var pendingPagingTicket: String = DEFAULT_PENDING_PAGING_TICKET
    private val momentItemPaddingPx = dpToPx(4)
    private var checkedWidthSpec = 0
    private var checkedHeightSpec = 0
    private var selectedWidthSpec = 0
    private var selectedHeightSpec = 0
    private var momentCardIsClickedForScroll: Boolean = false
    private var postListener: PostCallback? = null
    private var momentListener: MomentCallback? = null
    private var clickListener: ((MomentItemUiModel) -> Unit)? = null
    private var longClickListener: ((MomentItemUiModel) -> Unit)? = null
    private var addMomentClickListener: (() -> Unit)? = null
    private var momentRecyclerPaginationListener: MomentRecyclerPaginationListener? = null
    private val momentsAdapter: MomentItemAdapter

    private val diffUtil = object : DiffUtil.ItemCallback<MomentCarouselItem>() {

        override fun areItemsTheSame(
            oldItem: MomentCarouselItem,
            newItem: MomentCarouselItem
        ): Boolean {
            return oldItem.displayType == newItem.displayType && oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: MomentCarouselItem,
            newItem: MomentCarouselItem
        ): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(
            oldItem: MomentCarouselItem,
            newItem: MomentCarouselItem
        ): Any? {
            return when {
                oldItem is MomentCarouselItem.MomentGroupItem && newItem is MomentCarouselItem.MomentGroupItem -> {
                    val oldItemPreview = oldItem.group.firstNotViewedMomentPreview ?: oldItem.group.latestMomentPreview
                    val newItemPreview = newItem.group.firstNotViewedMomentPreview ?: newItem.group.latestMomentPreview

                    val backgroundImg = if (oldItemPreview != newItemPreview) {
                        newItemPreview
                    } else {
                        null
                    }
                    val isViewed = if (oldItem.group.isViewed != newItem.group.isViewed) newItem.group.isViewed else null
                    MomentCardUpdatePayload(
                        backgroundImg,
                        isViewed
                    )
                }
                else -> null
            }
        }
    }

    init {
        momentsAdapter = MomentItemAdapter(
            momentClick = { moment, _, view -> notifyClickListeners(moment, view) },
            momentLongClick = ::notifyLongClickListeners,
            createMomentClick = ::notifyCreateMomentListeners,
            onProfileClick = ::notifyOpenProfileListeners,
            diffCallback = diffUtil
        )
        adapter = momentsAdapter
        addItemDecoration(
            SpaceItemDecoration(space = momentItemPaddingPx, addHorizontal = true)
        )
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        if (checkIfRemeasureIsNeeded(widthSpec, heightSpec)) {
            checkedWidthSpec = widthSpec
            checkedHeightSpec = heightSpec
            val width = MeasureSpec.getSize(widthSpec)
            val cellWidth = ((width - getMomentTotalHorizontalPaddings()) / AMOUNT_OF_MOMENTS_ON_SCREEN).toInt()
            val cellHeight = (cellWidth * 16f / 9f).toInt()
            val rvHeight = cellHeight + paddingTop + paddingBottom
            selectedWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
            selectedHeightSpec = MeasureSpec.makeMeasureSpec(rvHeight, MeasureSpec.EXACTLY)
        }
        super.onMeasure(selectedWidthSpec, selectedHeightSpec)
    }

    override fun getAdapter() = super.getAdapter() as MomentItemAdapter?

    override fun getLayoutManager() = super.getLayoutManager() as LinearLayoutManager?

    fun setListeners(postCallback: PostCallback?, momentCallback: com.numplates.nomera3.modules.moments.show.presentation.MomentCallback? = null) {
        this.postListener = postCallback
        this.momentListener = momentCallback
        if (momentCallback != null) setPagingScrollListener(momentCallback)
    }

    fun setListeners(
        momentClick: ((MomentItemUiModel) -> Unit)? = null,
        momentLongClick: ((MomentItemUiModel) -> Unit)? = null,
        addMomentClick: (() -> Unit)? = null
    ) {
        clickListener = momentClick
        longClickListener = momentLongClick
        addMomentClickListener = addMomentClick
    }

    fun submitMoments(moments: MomentInfoCarouselUiModel?) {
        updatePendingPagingTicket(moments?.pagingTicket)
        momentsAdapter.submitList(moments?.momentsCarouselList) {
            updatePagingTicket(moments?.pagingTicket)
        }
    }

    fun scrollToWatchedMomentCard(lastWatchedGroupId: Long?) {
        if (lastWatchedGroupId == null || isLayoutSuppressed || !momentCardIsClickedForScroll) return
        val scroller = object : LinearSmoothScroller(context) {

            override fun getHorizontalSnapPreference(): Int = SNAP_TO_START

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                return super.calculateSpeedPerPixel(displayMetrics) * 2
            }
        }
        momentCardIsClickedForScroll = false
        val index = momentsAdapter.currentList.indexOfFirst { it.id == lastWatchedGroupId.toInt() }
        if (index < 0 || index >= momentsAdapter.currentList.size) return
        scroller.targetPosition = index
        layoutManager?.startSmoothScroll(scroller)
    }

    fun scrollToStart() {
        if (isLayoutSuppressed) return
        layoutManager?.scrollToPosition(0)
    }

    fun notifyCreateMomentListeners(entryPoint: AmplitudePropertyMomentEntryPoint) {
        postListener?.onAddMomentClicked()
        addMomentClickListener?.invoke()
        momentListener?.onMomentTapCreate(entryPoint)
    }

    private fun notifyOpenProfileListeners(momentGroupUiModel: MomentGroupUiModel) {
        postListener?.onMomentProfileClicked(momentGroupUiModel)
    }

    private fun setPagingScrollListener(momentCallback: com.numplates.nomera3.modules.moments.show.presentation.MomentCallback) {
        momentRecyclerPaginationListener = MomentRecyclerPaginationListener(this, momentCallback).also { listener ->
            addOnScrollListener(listener)
        }
    }

    private fun updatePendingPagingTicket(ticket: String?) {
        if (ticket != null && ticket != FORBID_PAGING_TICKET) pendingPagingTicket = ticket
    }

    private fun updatePagingTicket(ticket: String?) {
        currentPagingTicket = when {
            isAnyTicketForbidsPaging(currentPagingTicket, ticket, pendingPagingTicket) -> FORBID_PAGING_TICKET
            isCommittedTicketSameAsPending(ticket, pendingPagingTicket) -> ticket
            isPendingTicketNotCommittedButValid(ticket, pendingPagingTicket) -> pendingPagingTicket
            else -> currentPagingTicket
        }
        pendingPagingTicket = DEFAULT_PENDING_PAGING_TICKET
    }

    private fun isAnyTicketForbidsPaging(currentTicket: String?, committedListTicket: String?, pendingTicket: String): Boolean {
        return currentTicket == FORBID_PAGING_TICKET || committedListTicket == FORBID_PAGING_TICKET || pendingTicket == FORBID_PAGING_TICKET
    }

    private fun isCommittedTicketSameAsPending(committedListTicket: String?, pendingTicket: String): Boolean {
        return committedListTicket == pendingTicket
    }

    private fun isPendingTicketNotCommittedButValid(committedListTicket: String?, pendingTicket: String): Boolean {
        return committedListTicket == null && pendingTicket != DEFAULT_PENDING_PAGING_TICKET
    }

    private fun getMomentTotalHorizontalPaddings(): Int {
        val amountOfPaddingItemsOnScreen = 2 * AMOUNT_OF_MOMENTS_ON_SCREEN.toInt() + 1
        val itemsPadding = amountOfPaddingItemsOnScreen * momentItemPaddingPx
        return itemsPadding + paddingStart
    }

    private fun notifyClickListeners(moment: MomentCarouselItem, view: View?) {
        when (moment) {
            is MomentCarouselItem.MomentGroupItem -> {
                momentCardIsClickedForScroll = true
                postListener?.onMomentGroupClicked(moment.group, view, moment.group.isViewed)
            }
            is MomentCarouselItem.MomentCreateItem -> {
                momentCardIsClickedForScroll = true
                postListener?.onMomentGroupClicked(moment.group, view, moment.group.isViewed)
            }
            else -> Unit
        }
    }

    private fun notifyLongClickListeners(moment: MomentCarouselItem) {
        when (moment) {
            is MomentCarouselItem.MomentGroupItem -> postListener?.onMomentGroupLongClicked(moment.group)
            else -> Unit
        }
    }

    private fun checkIfRemeasureIsNeeded(widthSpec: Int, heightSpec: Int): Boolean {
        return widthSpec != checkedWidthSpec || heightSpec != checkedHeightSpec
    }

    fun clearResources() {
        momentRecyclerPaginationListener?.let {
            removeOnScrollListener(it)
            momentRecyclerPaginationListener = null
        }
        postListener = null
        momentListener = null
        clickListener = null
        longClickListener = null
        addMomentClickListener = null
        clearOnScrollListeners()
        adapter?.clearResources()
        adapter = null
        layoutManager = null
    }
}

private class SpaceItemDecoration(
    private val space: Int,
    private val addVertical: Boolean = false,
    private val addHorizontal: Boolean = false
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        with (outRect) {
            if (addVertical) {
                top = space
                bottom = space
            }
            if (addHorizontal) {
                left = space
                right = space
            }
        }
    }
}
