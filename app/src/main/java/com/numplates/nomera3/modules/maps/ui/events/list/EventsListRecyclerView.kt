package com.numplates.nomera3.modules.maps.ui.events.list

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.setPaddingBottom
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListType
import com.numplates.nomera3.modules.maps.ui.events.list.adapter.EventsListAdapter
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListItem
import com.numplates.nomera3.modules.maps.ui.events.list.model.SelectedEventsListItemUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapUiAction
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator

class EventsListRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    var itemActionListener: ((MapUiAction.EventsListUiAction) -> Unit) = {}
    var isLoading: () -> Boolean = { false }
    var loadMore: (Int) -> Unit = {}
    var onLast: () -> Boolean = { true }
    var listType: EventsListType? = null

    private val itemTopMarginPx = resources.getDimensionPixelSize(R.dimen.map_events_list_item_top_margin)
    private val lastItemDecoration = object : ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
            val isLastItem = parent.getChildAdapterPosition(view) == (parent.adapter?.itemCount ?: 0) - 1
            if (isLastItem) {
                val hItemMargins = view.resources.getDimensionPixelSize(R.dimen.map_events_list_item_horizontal_margin) * 2
                val wMeasureSpec = MeasureSpec.makeMeasureSpec(parent.width - hItemMargins, MeasureSpec.EXACTLY)
                val hMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                view.measure(wMeasureSpec, hMeasureSpec)
                parent.setPaddingBottom(parent.height - view.measuredHeight - itemTopMarginPx)
            }
            super.getItemOffsets(outRect, view, parent, state)
        }
    }
    private var childBeforeSelected: View? = null
    private var childAfterSelected: View? = null
    private var selectedItemPosition = 0

    init {
        val eventItemSelectionListener = object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState != SCROLL_STATE_IDLE) return
                val itemPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                val item = (adapter as EventsListAdapter).items?.getOrNull(itemPosition)
                if (item != null) {
                    val selectedItem = SelectedEventsListItemUiModel(
                        listType = listType ?: throw RuntimeException("EventsListType not set for this RecyclerView"),
                        itemPosition = itemPosition,
                        eventItem = item
                    )
                    val uiAction = MapUiAction.EventsListUiAction.EventsListItemSelected(selectedItem)
                    itemActionListener.invoke(uiAction)
                }
            }
        }
        addOnScrollListener(eventItemSelectionListener)
        layoutManager = LinearLayoutManager(context)
        adapter = EventsListAdapter { itemActionListener.invoke(it) }
        addItemDecoration(lastItemDecoration)
        RecyclerViewPaginator(
            recyclerView = this,
            onLast = { onLast.invoke() },
            isLoading = { isLoading.invoke() },
            loadMore = { page -> loadMore.invoke(page) },
        ).apply {
            endWithAuto = true
        }
        EventsListSnapHelper().attachToRecyclerView(this)
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_DOWN) {
            selectedItemPosition = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            childBeforeSelected = findChildBeforeSelected()
            childAfterSelected = findChildAfterSelected()
        }
        return super.onInterceptTouchEvent(e)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_MOVE) return super.onTouchEvent(e)
        if (childBeforeSelected == null) {
            childBeforeSelected = findChildBeforeSelected()
        }
        val childBeforeSelectedY = childBeforeSelected?.y
        val childAfterSelectedY = childAfterSelected?.y
        val isChildBeforeOverscrolled = childBeforeSelectedY != null && childBeforeSelectedY > itemTopMarginPx
        val isChildAfterOverscrolled = childAfterSelectedY != null && childAfterSelectedY < itemTopMarginPx
        return if (isChildBeforeOverscrolled || isChildAfterOverscrolled) {
            false
        } else {
            super.onTouchEvent(e)
        }
    }

    fun setItems(items: List<EventsListItem>) {
        (adapter as EventsListAdapter).items = items
    }

    private fun findChildBeforeSelected(): View? =
        findViewHolderForAdapterPosition(selectedItemPosition - 1)?.itemView

    private fun findChildAfterSelected(): View? =
        findViewHolderForAdapterPosition(selectedItemPosition + 1)?.itemView
}
