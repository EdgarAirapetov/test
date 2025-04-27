package com.numplates.nomera3.modules.maps.ui.friends

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.ui.events.list.EventsListSnapHelper
import com.numplates.nomera3.modules.maps.ui.friends.adapter.EventFriendsListItemAdapter
import com.numplates.nomera3.modules.maps.ui.friends.adapter.MapFriendItemDecorator
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendListItem
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiAction

class MapFriendsListRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    var itemActionListener: ((MapFriendsListUiAction) -> Unit)? = {}

    var isLoading: () -> Boolean = { false }
    var loadMore: (Int) -> Unit = {}
    var onLast: () -> Boolean = { true }


    private val itemTopMarginPx = resources.getDimensionPixelSize(R.dimen.material1)

    private var childBeforeSelected: View? = null
    private var childAfterSelected: View? = null
    private var selectedItemPosition = 0

    private val eventSnap = EventsListSnapHelper()

    fun init(
        itemActionListener: (MapFriendsListUiAction) -> Unit
    ) {
        this.itemActionListener = itemActionListener
        val eventItemSelectionListener = object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState != SCROLL_STATE_IDLE) return
                val itemPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                val item = (adapter as EventFriendsListItemAdapter).items?.getOrNull(itemPosition)
                if (item != null && item is MapFriendListItem.MapFriendUiModel) {
                    val uiAction = MapFriendsListUiAction.MapFriendListItemSelected(item, position = itemPosition)
                    itemActionListener?.invoke(uiAction)
                }
            }
        }
        addOnScrollListener(eventItemSelectionListener)
        layoutManager = LinearLayoutManager(context)
        adapter = EventFriendsListItemAdapter { itemActionListener?.invoke(it) }
        addItemDecoration(MapFriendItemDecorator(this.context))
        addSnapHelper()
    }

    fun addSnapHelper() {
        eventSnap.attachToRecyclerView(this)
    }

    fun removeSnapHelper() {
        eventSnap.attachToRecyclerView(null)
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

    fun setItems(items: List<MapFriendListItem>) {
        (adapter as EventFriendsListItemAdapter).items = items
    }

    private fun findChildBeforeSelected(): View? =
        findViewHolderForAdapterPosition(selectedItemPosition - 1)?.itemView

    private fun findChildAfterSelected(): View? =
        findViewHolderForAdapterPosition(selectedItemPosition + 1)?.itemView
}
