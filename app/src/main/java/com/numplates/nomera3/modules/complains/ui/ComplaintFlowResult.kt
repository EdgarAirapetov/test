package com.numplates.nomera3.modules.complains.ui

import java.util.concurrent.LinkedBlockingQueue

enum class ComplaintFlowResult {
    SUCCESS, FAILURE, CANCELLED;
}

/** To send flow result first call setIsFinishing(true), then call finishComplaintFlow(result) */
interface ComplaintFlowInteraction {
    fun addResultListener(listener: (ComplaintFlowResult) -> Unit)
    fun setIsFinishing(isFinishing: Boolean)
    fun finishComplaintFlow(result: ComplaintFlowResult)
}

class ComplaintFlowInteractionDelegate: ComplaintFlowInteraction {

    private val listeners = LinkedBlockingQueue<(ComplaintFlowResult) -> Unit>()
    private var isFinishing: Boolean = true

    override fun addResultListener(listener: (ComplaintFlowResult) -> Unit) {
        listeners.add(listener)
    }

    override fun finishComplaintFlow(result: ComplaintFlowResult) {
        if (isFinishing) {
            listeners.forEach { it.invoke(result) }
            listeners.clear()
        }
    }

    override fun setIsFinishing(isFinishing: Boolean) {
        this.isFinishing = isFinishing
    }
}
