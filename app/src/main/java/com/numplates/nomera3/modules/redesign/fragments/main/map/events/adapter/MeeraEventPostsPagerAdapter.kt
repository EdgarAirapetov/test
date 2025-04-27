package com.numplates.nomera3.modules.redesign.fragments.main.map.events.adapter

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.numplates.nomera3.modules.maps.ui.MapParametersCache
import com.numplates.nomera3.modules.maps.ui.events.model.EventSnippetItem
import com.numplates.nomera3.modules.maps.ui.events.snippet.EventSnippetLoaderFragment
import com.numplates.nomera3.modules.maps.ui.view.MapSnippetPage
import com.numplates.nomera3.modules.redesign.fragments.main.map.snippet.MeeraEventPostPageFragment
import com.numplates.nomera3.modules.redesign.fragments.main.map.snippet.MeeraEventSnippetErrorFragment

class MeeraEventPostsPagerAdapter(
    fragmentManager: FragmentManager,
    private val mapParametersCache: MapParametersCache
) : FragmentStatePagerAdapter(fragmentManager) {

    private var items: List<EventSnippetItem> = listOf()

    override fun getCount(): Int {
        return items.size
    }

    override fun getItemPosition(obj: Any): Int {
        return when (obj) {
            is MeeraEventSnippetErrorFragment -> POSITION_NONE
            is MeeraEventPostPageFragment -> {
                val id = obj.getPageIndex()
                    ?.let { (items.getOrNull(it) as? EventSnippetItem.EventPostItem) }
                    ?.eventObject?.eventPost?.postId
                if (id != null && id == obj.getPostId()) {
                    POSITION_UNCHANGED
                } else {
                    POSITION_NONE
                }
            }
            else -> POSITION_NONE
        }
    }

    override fun getItem(position: Int): Fragment {
        return when (val item = items.getOrNull(position)) {
            is EventSnippetItem.EventPostItem -> {
                mapParametersCache.putEventPostItem(item)
                MeeraEventPostPageFragment.newInstance(
                    postId = item.eventObject.eventPost.postId,
                    pageIndex = position
                )
            }
            EventSnippetItem.LoaderItem -> {
                EventSnippetLoaderFragment()
            }
            is EventSnippetItem.ErrorItem -> {
                MeeraEventSnippetErrorFragment()
            }
            else -> Fragment()
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        (`object` as? MapSnippetPage)?.onDestroyPage()
        super.destroyItem(container, position, `object`)
    }

    fun setItemModels(items: List<EventSnippetItem>) {
        if (items == this.items) return
        if (items.isEmpty()) mapParametersCache.clearEventObjects()
        this.items = items
        notifyDataSetChanged()
    }
}
