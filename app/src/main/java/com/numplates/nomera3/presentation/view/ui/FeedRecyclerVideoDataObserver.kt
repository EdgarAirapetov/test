package com.numplates.nomera3.presentation.view.ui

import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.doDelayed
import kotlinx.coroutines.Job
import timber.log.Timber

private const val ANIMATION_LISTENERS_DELAY = 100L

class FeedRecyclerVideoDataObserver(
    private var recyclerView: RecyclerView?,
    private var tryTriggerVideoPlay: ((playerChangeOffset: Int) -> Unit)?,
    private var isCurrentPlayPositionAffected: ((startPositionOfDiff: Int) -> Boolean)?
) : RecyclerView.AdapterDataObserver() {

    var isRegistered: Boolean = false
        set(registered) {
            field = registered
            if (registered.not()) {
                setDelayedListenersJob?.cancel()
            }
        }

    private var setDelayedListenersJob: Job? = null
    private var playPositionDifference: Int = 0

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        super.onItemRangeInserted(positionStart, itemCount)
        isCurrentPlayPositionAffected?.let { playPositionAffected ->
            if (playPositionAffected(positionStart)) playPositionDifference += itemCount
            setFinishedAnimationsListener()
        }
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        super.onItemRangeRemoved(positionStart, itemCount)
        isCurrentPlayPositionAffected?.let { playPositionAffected ->
            if (playPositionAffected(positionStart)) playPositionDifference -= itemCount
            setFinishedAnimationsListener()
        }
    }

    fun registerObserver() {
        recyclerView?.adapter?.let {
            runCatching {
                if (isRegistered.not()) {
                    it.registerAdapterDataObserver(this)
                    isRegistered = true
                }
            }.onFailure {
                Timber.d("observer $this was registered already")
                isRegistered = true
            }
        }

    }

    fun unregisterObserver() {
        recyclerView?.adapter?.let {
            runCatching {
                if (isRegistered) {
                    it.unregisterAdapterDataObserver(this)
                    isRegistered = false
                }
            }.onFailure {
                Timber.d("observer $this was unregistered already")
                isRegistered = false
            }
        }
    }

    fun onDestroy() {
        recyclerView = null
        tryTriggerVideoPlay = null
        isCurrentPlayPositionAffected = null
    }

    private fun setFinishedAnimationsListener() {
        setDelayedListenersJob?.cancel()
        setDelayedListenersJob = recyclerView?.doDelayed(ANIMATION_LISTENERS_DELAY) {
            recyclerView?.itemAnimator?.isRunning {
                tryTriggerVideoPlay?.invoke(playPositionDifference)
                playPositionDifference = 0
            } ?: kotlin.run {
                tryTriggerVideoPlay?.invoke(playPositionDifference)
                playPositionDifference = 0
            }
        }
    }

}
