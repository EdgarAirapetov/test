package com.numplates.nomera3.presentation.view.adapter

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.camera.view.PreviewView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlideWithCacheAndError
import com.meera.core.extensions.visible
import com.meera.core.utils.files.FileManager
import com.meera.media_controller_api.MediaControllerFeatureApi
import com.meera.media_controller_api.model.MediaControllerNeedEditResponse
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.numplates.nomera3.MEDIA_GALLERY_ITEM_COUNT_IN_ROW
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.feed.ui.getScreenWidth
import com.numplates.nomera3.presentation.view.adapter.MediaListAdapter.MediaItemGallery.Companion.TYPE_CAMERA
import com.numplates.nomera3.presentation.view.adapter.MediaListAdapter.MediaItemGallery.Companion.TYPE_GALLERY
import com.numplates.nomera3.presentation.view.adapter.MediaListAdapter.MediaItemGallery.Companion.TYPE_MEDIA
import com.numplates.nomera3.presentation.view.adapter.MediaListAdapter.MediaItemGallery.Companion.TYPE_VIDEO
import com.numplates.nomera3.presentation.view.utils.camera.CameraProvider

class MediaListAdapter(
    private val cameraProvider: CameraProvider,
    private val lifecycleOwner: LifecycleOwner,
    private val fileManager: FileManager,
    private val mediaEditorViewController: MediaControllerFeatureApi
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val collection = mutableListOf<MediaItemGallery>()

    var onClickListener: (MediaItemGallery) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_media, parent, false)
        return MediaViewHolder(v, cameraProvider, lifecycleOwner, fileManager, mediaEditorViewController)
    }

    override fun getItemCount() = collection.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? MediaViewHolder)?.bind(collection[position], onClickListener)
    }

    fun resetAndAddData(collection: List<MediaItemGallery>) {
        this.collection.clear()
        this.collection.addAll(collection)
        notifyDataSetChanged()
    }

    class MediaViewHolder(
        val v: View,
        private val cameraProvider: CameraProvider,
        private val lifecycleOwner: LifecycleOwner,
        private val fileManager: FileManager,
        private val mediaEditorViewController: MediaControllerFeatureApi
    ) : RecyclerView.ViewHolder(v) {

        private val ITEM_PADDING = 10

        // icons
        private val llGalleryIconContainer = v.findViewById<LinearLayout>(R.id.ll_gallery_media)
        private val ivCameraIcon = v.findViewById<ImageView>(R.id.iv_img_camera)
        private val ivMediaPreview = v.findViewById<ImageView>(R.id.iv_media_preview)
        private val cameraPreview = v.findViewById<PreviewView>(R.id.preview_camera)
        private val clVideo = v.findViewById<ConstraintLayout>(R.id.cl_video)
        private val cvRoot = v.findViewById<CardView>(R.id.cv_gallery_media)
        private val tvVideoTime = v.findViewById<TextView>(R.id.tv_video_time)
        private lateinit var innerItem: MediaItemGallery
        private var thread: Thread? = null

        fun bind(item: MediaItemGallery, onClickListener: (MediaItemGallery) -> Unit) {
            this.innerItem = item

            cancelThread()

            // duration view default state
            clVideo.gone()
            clVideo.setBackgroundColor(Color.parseColor("#73000000"))

            when (item.type) {
                TYPE_MEDIA -> handleMedia()
                TYPE_GALLERY -> handleGallery()
                TYPE_CAMERA -> handleCamera()
                TYPE_VIDEO -> handleVideo()
            }

            cvRoot.updateLayoutParams {
                val size = (getScreenWidth() / MEDIA_GALLERY_ITEM_COUNT_IN_ROW) - ITEM_PADDING.dp
                width = size
                height = size
            }

            cvRoot.setOnClickListener {
                onClickListener(innerItem)
            }
        }

        private fun handleVideo() {
            ivMediaPreview.visible()
            cameraPreview.gone()

            llGalleryIconContainer.gone()
            ivCameraIcon.gone()

            innerItem.mediaPreview?.path?.let {
                ivMediaPreview.loadGlideWithCacheAndError(it)
            }

            thread = Thread {
                innerItem.mediaPreview?.let { mediaUri ->
                    val isNeedEditResponse = mediaEditorViewController.needEditMedia(
                        uri = mediaUri,
                        openPlace = MediaControllerOpenPlace.Post
                    ) as? MediaControllerNeedEditResponse.VideoTooLong

                    val videoDurationMils = fileManager.getVideoDurationMils(mediaUri)

                    if (Thread.interrupted()) return@Thread

                    val minutesInt = videoDurationMils / (1000 * 60)

                    if (isNeedEditResponse != null) {
                        clVideo.post {
                            clVideo.setBackgroundColor(Color.parseColor("#65ee2121")) //#73000000
                        }
                    }

                    var minutes: String = minutesInt.toString()
                    if (minutes.length == 1) minutes = "0$minutes"
                    var seconds: String = (videoDurationMils / 1000 % 60).toString()
                    if (seconds.length == 1) seconds = "0$seconds"

                    tvVideoTime.post {
                        tvVideoTime.text = String.format("%s:%s", minutes, seconds)
                    }

                    clVideo.post {
                        clVideo.visible()
                    }
                }
            }
            thread?.start()
        }

        private fun handleMedia() {
            ivMediaPreview.visible()
            cameraPreview.gone()

            llGalleryIconContainer.gone()
            ivCameraIcon.gone()

            innerItem.mediaPreview?.path?.let {
                ivMediaPreview.loadGlideWithCacheAndError(it)
            }
        }

        private fun handleCamera() {
            llGalleryIconContainer.gone()
            ivCameraIcon.visible()

            // Camera preview
            ivMediaPreview.gone()
            cameraPreview.visible()
            if (ActivityCompat.checkSelfPermission(itemView.context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                /*cameraProvider.run {
                    cameraProvider.startCameraPreview(lifecycleOwner, cameraPreview)
                }*/
                cameraProvider.startCameraPreviewViewHolder(lifecycleOwner, cameraPreview)
            } else {
                cameraPreview.gone()
                ivMediaPreview.visible()
                ivMediaPreview.setImageResource(R.color.color_soft_black)
            }
        }

        private fun handleGallery() {
            ivMediaPreview.gone()
            cameraPreview.gone()

            ivCameraIcon.gone()

            llGalleryIconContainer.visible()
            ivMediaPreview.setImageResource(R.color.ui_bg_media_gallery)
        }

        private fun cancelThread() {
            thread?.let {
                if (!it.isInterrupted) {
                    it.interrupt()
                }
            }
            thread = null
        }
    }

    data class MediaItemGallery(
        var mediaPreview: Uri? = null,
        var type: Int,
        var isNeedToEditResponse: MediaControllerNeedEditResponse.VideoTooLong? = null
    ) {
        companion object {
            const val TYPE_MEDIA = 0
            const val TYPE_GALLERY = 1
            const val TYPE_CAMERA = 2
            const val TYPE_VIDEO = 3
        }
    }

}
