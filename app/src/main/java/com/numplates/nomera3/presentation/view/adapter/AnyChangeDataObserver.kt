package com.numplates.nomera3.presentation.view.adapter

import androidx.recyclerview.widget.RecyclerView

// TODO https://nomera.atlassian.net/browse/BR-27622
abstract class AnyChangeDataObserver : RecyclerView.AdapterDataObserver() {

    abstract fun changesTriggered()

    override fun onChanged() {
        changesTriggered()
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        changesTriggered()
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        changesTriggered()
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        changesTriggered()
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        changesTriggered()
    }

    override fun onStateRestorationPolicyChanged() {
        changesTriggered()
    }
}
