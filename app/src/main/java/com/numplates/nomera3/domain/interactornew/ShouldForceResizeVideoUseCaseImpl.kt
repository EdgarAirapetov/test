package com.numplates.nomera3.domain.interactornew

import android.app.ActivityManager
import android.content.Context
import com.meera.application_api.media.domain.ShouldForceResizeVideoUseCase
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import javax.inject.Inject

const val LARGE_HEAP_MEM_THRESHOLD = 256

class ShouldForceResizeVideoUseCaseImpl @Inject constructor(
    private val application: App,
    private val featureTogglesContainer: FeatureTogglesContainer
) : ShouldForceResizeVideoUseCase {
    override fun invoke(): Boolean {
        val activityManager = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val forceResizeToggle = featureTogglesContainer.videoEditorFeatureToggle.isEnabled
        return forceResizeToggle || activityManager.largeMemoryClass < LARGE_HEAP_MEM_THRESHOLD
    }
}
