package com.meera.media_controller_implementation

import androidx.appcompat.app.AppCompatActivity
import com.meera.application_api.ApplicationApi
import com.meera.media_controller_api.MediaControllerFeatureApi
import com.meera.media_controller_implementation.di.DaggerMediaControllerInternalComponent

object MediaControllerFeatureBuilder {
    fun build(
        applicationApi: ApplicationApi,
        rootActivity: AppCompatActivity,
    ): MediaControllerFeatureApi {

        val component = if (MediaControllerFeatureImplementation.isCreated()) MediaControllerFeatureImplementation.getComponent()
            else DaggerMediaControllerInternalComponent.factory().create(applicationApi = applicationApi)

        return MediaControllerFeatureImplementation(
            rootActivity = rootActivity,
            component = component
        )
    }
}
