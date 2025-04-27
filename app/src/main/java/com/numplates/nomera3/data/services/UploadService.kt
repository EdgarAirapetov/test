package com.numplates.nomera3.data.services

import android.app.IntentService
import android.content.Intent
import com.numplates.nomera3.App
import com.numplates.nomera3.domain.interactornew.UploadAlbumImageUseCase
import com.numplates.nomera3.presentation.view.utils.eventbus.busevents.BusEvents
import timber.log.Timber
import javax.inject.Inject


class UploadService: IntentService("uploadService") {

    @Inject
    lateinit var uploadAlbumImageUseCase: UploadAlbumImageUseCase

    private var progress = -1
    private var total = -1

    init {
        App.component.inject(this)
    }

    override fun onHandleIntent(intent: Intent?) {
        val images = intent?.getStringArrayListExtra(IMAGES)
        images?.let{ arr->
            total = arr.size
            for (i in 0 until arr.size){
                uploadAlbumImageUseCase.uploadAlbumImage(arr[i])
                        ?.subscribe({
                            Timber.d("UploadService Image uploaded !!")
                            progress = i
                            App.bus.send(BusEvents.UploadProgress(images.size, i))
                        },{
                            Timber.e("UploadService Error while uploading image: $it")
                        })
            }
        }
    }

    companion object {
        //ARRAY local image URLS
        const val IMAGES = "IMAGES"

        //SERVICE MODE
        const val SERVICE_MODE = "SERVICE_MODE"
        const val SERVICE_MODE_REQUEST_UPLOAD = 1

    }
}
