package com.meera.core.adapters

import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

class AsyncViewCache(
    private val lifecycleOwner: LifecycleOwner,
    @LayoutRes val resId: Int,
    private var parent: RecyclerView?,
) : DefaultLifecycleObserver {

    private val cachedViews: BlockingQueue<View> = ArrayBlockingQueue(INITIAL_CAPACITY)

    init {
        lifecycleOwner.lifecycle.addObserver(this)
        inflateViewsAsync(INITIAL_CAPACITY)
    }

    fun getView(): View {
        return if (cachedViews.isNotEmpty()) {
            cachedViews.take()
        } else {
            LayoutInflater.from(parent?.context).inflate(resId, parent, false)
        }
    }

    fun clearResources() {
        cachedViews?.clear()
        lifecycleOwner.lifecycle.removeObserver(this)
        parent = null
    }

    private fun inflateViewsAsync(count: Int) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            while (count > cachedViews.size && isActive) {
                try {
                    cachedViews.offer(LayoutInflater.from(parent?.context).inflate(resId, parent, false))
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        cachedViews.offer(
                            LayoutInflater.from(parent?.context)
                                .inflate(resId, null, false)
                        )
                    }
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        lifecycleOwner.lifecycle.removeObserver(this)
        cachedViews.clear()
    }

    companion object {
        private const val INITIAL_CAPACITY = 5
    }

}
