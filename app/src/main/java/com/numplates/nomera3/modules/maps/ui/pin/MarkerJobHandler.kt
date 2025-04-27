package com.numplates.nomera3.modules.maps.ui.pin

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import timber.log.Timber

class MarkerJobHandler(fragment: Fragment) {

    private val scope = fragment.viewLifecycleOwner.lifecycleScope
    private val markerJobsContext = SupervisorJob(scope.coroutineContext.job) +
        CoroutineExceptionHandler { _, t -> Timber.e(t) }

    fun launch(block: suspend (CoroutineScope) -> Unit): Job = scope.launch(markerJobsContext) {
        block(this)
    }
}
