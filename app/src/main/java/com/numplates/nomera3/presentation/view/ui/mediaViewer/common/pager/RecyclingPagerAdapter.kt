package com.numplates.nomera3.presentation.view.ui.mediaViewer.common.pager

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.numplates.nomera3.presentation.view.ui.mediaViewer.viewer.adapter.ImagesPagerAdapter
import java.util.*

internal abstract class RecyclingPagerAdapter<VH : RecyclingPagerAdapter.ViewHolder>
    : PagerAdapter() {

    companion object {
        private val STATE = RecyclingPagerAdapter::class.java.simpleName
        const val VIEW_TYPE_IMAGE = 0
        const val VIEW_TYPE_VIDEO = 1
        const val VIEW_TYPE_VIDEO_NOT_PLAYING = 2
    }

    internal abstract fun getItemCount(): Int
    internal abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int, position: Int): VH
    internal abstract fun onBindViewHolder(holder: VH, position: Int)
    internal abstract fun getViewTypeForPosition(position: Int): Int

    private val typeCaches = SparseArray<RecycleCache>()
    private var savedStates = SparseArray<Parcelable>()

    override fun destroyItem(parent: ViewGroup, position: Int, item: Any) {
        if (item is ViewHolder) {
            item.detach(parent)
        }
    }

    override fun getCount() = getItemCount()

    override fun getItemPosition(item: Any) = POSITION_NONE

    @Suppress("UNCHECKED_CAST")
    override fun instantiateItem(parent: ViewGroup, position: Int): Any {
        var cache = typeCaches.get(VIEW_TYPE_IMAGE)

        if (cache == null) {
            cache = RecycleCache(this)
            typeCaches.put(VIEW_TYPE_IMAGE, cache)
        }
        return cache.getFreeViewHolder(parent, position)
                .apply {
                    attach(parent, position)
                    onBindViewHolder(this as VH, position)
                    onRestoreInstanceState(savedStates.get(getItemId(position)))
                }
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean =
            obj is ViewHolder && obj.itemView === view

    override fun saveState(): Parcelable? {
        for (viewHolder in getAttachedViewHolders()) {
            savedStates.put(getItemId(viewHolder.position), viewHolder.onSaveInstanceState())
        }
        return Bundle().apply { putSparseParcelableArray(STATE, savedStates) }
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        if (state != null && state is Bundle) {
            state.classLoader = loader
            val sparseArray: SparseArray<Parcelable>? = state.getSparseParcelableArray(STATE)
            savedStates = sparseArray ?: SparseArray()
        }
        super.restoreState(state, loader)
    }

    private fun getItemId(position: Int) = position

    private fun getAttachedViewHolders(): List<ViewHolder> {
        val attachedViewHolders = ArrayList<ViewHolder>()

        val size = typeCaches.size()
        for (index in 0 until size) {
            if (size != typeCaches.size()) throw ConcurrentModificationException()

            typeCaches.valueAt(index).caches.forEach { holder ->
                if (holder.isAttached) {
                    attachedViewHolders.add(holder)
                }
            }
        }
        return attachedViewHolders
    }

    private fun getAllViewHolders(): List<ViewHolder> {
        val viewHolders = ArrayList<ViewHolder>()

        val size = typeCaches.size()
        for (index in 0 until size) {
            if (size != typeCaches.size()) throw ConcurrentModificationException()

            typeCaches.valueAt(index).caches.forEach { holder ->
                viewHolders.add(holder)
            }
        }
        return viewHolders
    }

    fun release() {
        val holders = getAllViewHolders()
        holders.forEach{  holder ->
            if (holder is ImagesPagerAdapter.VideoViewHolder){
                holder.releasePlayer()
            }
        }
    }

    fun pauseVideo() {
        val holders = getAllViewHolders()
        holders.forEach{ holder ->
            if (holder is ImagesPagerAdapter.VideoViewHolder){
                holder.pausePlayer()
            }
        }
    }


    fun pauseSidePositionVideo(position: Int) {
        val holders = getAllViewHolders()
        holders.forEach { holder ->
            if (holder is ImagesPagerAdapter.VideoViewHolder && holder.position != position) {
                holder.pausePlayer()
            }
        }
    }

    fun getViewHolderByPosition(position: Int): ViewHolder? = getAllViewHolders().find { it.position == position }
    fun getAllViewHolders(position: Int): ViewHolder? = getAllViewHolders().find { it.position == position }

    private class RecycleCache(
            private val adapter: RecyclingPagerAdapter<*>
    ) {

        val caches = mutableListOf<ViewHolder>()

        fun getFreeViewHolder(parent: ViewGroup, position: Int): ViewHolder {
            var iterationsCount = 0
            var viewHolder: ViewHolder
            val viewType = adapter.getViewTypeForPosition(position)
            while (iterationsCount < caches.size) {
                viewHolder = caches[iterationsCount]
                if (!viewHolder.isAttached) {
                    if ((viewHolder is ImagesPagerAdapter.ViewHolder) && viewType == VIEW_TYPE_IMAGE)//check viewHolder instance (video/photo)
                        return viewHolder
                    else if ((viewHolder is ImagesPagerAdapter.VideoViewHolder) && viewType == VIEW_TYPE_VIDEO){
                        return viewHolder
                    }
                }
                iterationsCount++
            }

            return adapter.onCreateViewHolder(parent, viewType, position).also { caches.add(it) }
        }
    }

    internal open class ViewHolder(internal val itemView: View) {

        companion object {
            private val STATE = ViewHolder::class.java.simpleName
        }

        internal var position: Int = 0
        internal var isAttached: Boolean = false

        internal fun attach(parent: ViewGroup, position: Int) {
            this.isAttached = true
            this.position = position
            parent.addView(itemView)
        }

        internal fun detach(parent: ViewGroup) {
            parent.removeView(itemView)
            isAttached = false
        }

        internal fun onRestoreInstanceState(state: Parcelable?) {
            getStateFromParcelable(state)?.let { itemView.restoreHierarchyState(it) }
        }

        internal fun onSaveInstanceState(): Parcelable {
            val state = SparseArray<Parcelable>()
            itemView.saveHierarchyState(state)
            return Bundle().apply { putSparseParcelableArray(STATE, state) }
        }

        private fun getStateFromParcelable(state: Parcelable?): SparseArray<Parcelable>? {
            if (state != null && state is Bundle && state.containsKey(STATE)) {
                return state.getSparseParcelableArray<Parcelable>(STATE)
            }
            return null
        }
    }
}
