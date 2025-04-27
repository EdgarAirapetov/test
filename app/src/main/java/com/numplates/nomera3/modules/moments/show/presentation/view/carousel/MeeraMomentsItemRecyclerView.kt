package com.numplates.nomera3.modules.moments.show.presentation.view.carousel

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentEntryPoint
import com.numplates.nomera3.modules.feed.ui.MeeraPostCallback
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoCarouselUiModel
import com.numplates.nomera3.modules.moments.show.data.entity.MomentPagingParams
import com.numplates.nomera3.modules.moments.show.presentation.MeeraMomentRecyclerPaginationListener
import com.numplates.nomera3.modules.moments.show.presentation.MomentCallback
import com.numplates.nomera3.modules.moments.show.presentation.adapter.MeeraMomentItemAdapter
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentCarouselItem
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupUiModel
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.modules.moments.show.presentation.viewholder.MomentCardUpdatePayload
import kotlin.math.abs

private const val SCROLL_THRESHOLD = 5F

class MeeraMomentsItemRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_PENDING_PAGING_TICKET = "DEFAULT_PENDING_PAGING_TICKET"
    }

    var currentPagingTicket: String? = null
        private set
    private var pendingPagingTicket: String = DEFAULT_PENDING_PAGING_TICKET
    private val momentItemPaddingPx = 4.dp
    private var momentCardIsClickedForScroll: Boolean = false
    private var postListener: MeeraPostCallback? = null
    private var momentListener: MomentCallback? = null
    private var clickListener: ((MomentItemUiModel) -> Unit)? = null
    private var longClickListener: ((MomentItemUiModel) -> Unit)? = null
    private var addMomentClickListener: (() -> Unit)? = null
    private var momentsAdapter: MeeraMomentItemAdapter? = null

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
        momentsAdapter = MeeraMomentItemAdapter(
            momentClick = { moment, _, view -> notifyClickListeners(moment, view) },
            momentLongClick = ::notifyLongClickListeners,
            createMomentClick = ::notifyCreateMomentListeners,
            onProfileClick = ::notifyOpenProfileListeners,
            diffCallback = diffUtil
        )
        adapter = momentsAdapter
        addItemDecoration(
            MeeraSpaceItemDecoration(space = momentItemPaddingPx, addHorizontal = true)
        )
        addOnItemTouchListener(object : OnItemTouchListener {
            var downX = 0F
            var downY = 0F
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = e.x
                        downY = e.y
                        momentListener?.isMomentsScroll(true)
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val distanceX = abs(e.x - downX)
                        val distanceY = abs(e.y - downY)
                        val isVerticalScroll = distanceY > SCROLL_THRESHOLD && distanceX < SCROLL_THRESHOLD
                        momentListener?.isMomentsScroll(!isVerticalScroll)
                        parent.requestDisallowInterceptTouchEvent(!isVerticalScroll)
                    }
                    MotionEvent.ACTION_UP -> {
                        momentListener?.isMomentsScroll(false)
                        parent.requestDisallowInterceptTouchEvent(false)
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        parent.requestDisallowInterceptTouchEvent(false)
                    }
                }

                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) = Unit

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) = Unit
        })

    }

    override fun getAdapter() = super.getAdapter() as MeeraMomentItemAdapter?

    override fun getLayoutManager() = super.getLayoutManager() as LinearLayoutManager?

    fun setListeners(postCallback: MeeraPostCallback?, momentCallback: MomentCallback? = null) {
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
        momentsAdapter?.submitList(moments?.momentsCarouselList) {
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
        val index = momentsAdapter?.currentList?.indexOfFirst { it.id == lastWatchedGroupId.toInt() } ?: return
        if (index < 0 || index >= (momentsAdapter?.currentList?.size ?: 0)) return
        scroller.targetPosition = index
        layoutManager?.startSmoothScroll(scroller)
    }

    fun scrollToStart(smoothScroll: Boolean) {
        if (isLayoutSuppressed) return
        if (!smoothScroll) {
            layoutManager?.scrollToPosition(0)
        } else {
            smoothScrollToPosition(0)
        }
    }

    fun notifyCreateMomentListeners(entryPoint: AmplitudePropertyMomentEntryPoint) {
        postListener?.onAddMomentClicked()
        addMomentClickListener?.invoke()
        momentListener?.onMomentTapCreate(entryPoint)
    }

    fun release() {
        momentsAdapter = null
        adapter = null
        postListener = null
        longClickListener = null
        addMomentClickListener = null
        momentListener = null
    }

    private fun notifyOpenProfileListeners(momentGroupUiModel: MomentGroupUiModel) {
        postListener?.onMomentProfileClicked(momentGroupUiModel)
    }

    private fun setPagingScrollListener(momentCallback: MomentCallback) {
        addOnScrollListener(MeeraMomentRecyclerPaginationListener(this, momentCallback))
    }

    private fun updatePendingPagingTicket(ticket: String?) {
        if (ticket != null && ticket != MomentPagingParams.FORBID_PAGING_TICKET) pendingPagingTicket = ticket
    }

    private fun updatePagingTicket(ticket: String?) {
        currentPagingTicket = when {
            isAnyTicketForbidsPaging(currentPagingTicket, ticket, pendingPagingTicket) -> MomentPagingParams.FORBID_PAGING_TICKET
            isCommittedTicketSameAsPending(ticket, pendingPagingTicket) -> ticket
            isPendingTicketNotCommittedButValid(ticket, pendingPagingTicket) -> pendingPagingTicket
            else -> currentPagingTicket
        }
        pendingPagingTicket = DEFAULT_PENDING_PAGING_TICKET
    }

    private fun isAnyTicketForbidsPaging(
        currentTicket: String?,
        committedListTicket: String?,
        pendingTicket: String
    ): Boolean {
        return currentTicket == MomentPagingParams.FORBID_PAGING_TICKET ||
            committedListTicket == MomentPagingParams.FORBID_PAGING_TICKET ||
            pendingTicket == MomentPagingParams.FORBID_PAGING_TICKET
    }

    private fun isCommittedTicketSameAsPending(committedListTicket: String?, pendingTicket: String): Boolean {
        return committedListTicket == pendingTicket
    }

    private fun isPendingTicketNotCommittedButValid(committedListTicket: String?, pendingTicket: String): Boolean {
        return committedListTicket == null && pendingTicket != DEFAULT_PENDING_PAGING_TICKET
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
}

private class MeeraSpaceItemDecoration(
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
