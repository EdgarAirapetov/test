package com.numplates.nomera3.modules.maps.ui.events.list.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.viewpager.widget.PagerAdapter
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.LayoutEventsListPageBinding
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListType
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListItem
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListPageUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapUiAction
import java.lang.ref.WeakReference
import kotlin.collections.set


class EventsListsPagerAdapter(
    private val itemActionListener: (MapUiAction.EventsListUiAction) -> Unit
) : PagerAdapter() {

    private var eventListPages: List<EventsListPageUiModel> = emptyList()
    private var eventListPageViews = HashMap<Int, WeakReference<View>>()

    override fun getPageTitle(position: Int): CharSequence = eventListPages[position].title
    override fun getCount(): Int = eventListPages.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding = LayoutInflater.from(container.context)
            .inflate(R.layout.layout_events_list_page, container, false)
            .apply(container::addView)
            .let(LayoutEventsListPageBinding::bind)
        val pageUiModel = eventListPages[position]
        with(binding.rvLayoutEventsListPage) {
            listType = pageUiModel.eventsListType
            itemActionListener = this@EventsListsPagerAdapter.itemActionListener
            setItems(pageUiModel.eventsListItems.items)
            onLast = { eventListPages[position].eventsListItems.isLastPage }
            isLoading = { eventListPages[position].eventsListItems.isLoadingNextPage }
            loadMore = {
                val uiAction = MapUiAction.EventsListUiAction.LoadNextListPageRequested(pageUiModel.eventsListType)
                itemActionListener.invoke(uiAction)
            }
        }
        binding.elewLayoutEventsListPage.uiActionListener = itemActionListener
        binding.elewLayoutEventsListPage.setUiModel(pageUiModel.emptyUiModel)
        eventListPageViews[position] = WeakReference(binding.root)
        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        eventListPageViews.remove(position)
        container.removeView(`object` as View)
    }

    fun selectItem(eventsListType: EventsListType, item: EventsListItem) {
        eventListPages.forEachIndexed { index, eventsListPageUiModel ->
            if (eventsListPageUiModel.eventsListType == eventsListType) {
                eventListPageViews[index]?.get()?.let { view ->
                    val binding = LayoutEventsListPageBinding.bind(view)
                    val position = (binding.rvLayoutEventsListPage.adapter as EventsListAdapter).items.indexOf(item)
                    binding.rvLayoutEventsListPage.scrollToPosition(position)
                }
            }
        }
    }

    fun setEventListPages(eventListPages: List<EventsListPageUiModel>) {
        if (eventListPages == this.eventListPages) return
        eventListPages.forEachIndexed { index, eventsListPageUiModel ->
            eventListPageViews[index]?.get()?.let { view ->
                val binding = LayoutEventsListPageBinding.bind(view)
                (binding.rvLayoutEventsListPage.adapter as? EventsListAdapter)?.items =
                    eventsListPageUiModel.eventsListItems.items
                val listIsEmpty = eventsListPageUiModel.eventsListItems.isLastPage
                    && eventsListPageUiModel.eventsListItems.isLoadingNextPage.not()
                    && eventsListPageUiModel.eventsListItems.items.isEmpty()
                binding.elewLayoutEventsListPage.isVisible = listIsEmpty
                binding.rvLayoutEventsListPage.isVisible = listIsEmpty.not()
                binding.elewLayoutEventsListPage.setUiModel(eventsListPageUiModel.emptyUiModel)
            }
        }
        val pageListChanged = eventListPages.map { it.eventsListType } != this.eventListPages.map { it.eventsListType }
        this.eventListPages = eventListPages
        if (pageListChanged) {
            notifyDataSetChanged()
        }
    }
}
