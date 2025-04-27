package com.numplates.nomera3.presentation.view.utils.tedbottompicker.viewholder

import android.Manifest
import android.content.pm.PackageManager
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setBackgroundTint
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setTintColor
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraTedbottompickerGridItemBinding
import com.numplates.nomera3.presentation.view.utils.camera.CameraProvider
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.model.PickerTile
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val MAX_VIDEO_LENGTHS_MINUTES = 5

class MeeraGalleryViewHolder(
    itemView: View,
    private val builder: TedBottomSheetDialogFragment.BaseBuilder<*>,
    isMultiMediaModeEnabled: Boolean,
    private val cameraProvider: CameraProvider,
    private val lifecycleOwner: LifecycleOwner,
    private val onSelectClicked: (position: Int) -> Unit,
    private val onPreviewClicked: (position: Int) -> Unit,
) : RecyclerView.ViewHolder(itemView) {

    private val binding = MeeraTedbottompickerGridItemBinding.bind(itemView)

    init {
        if (isMultiMediaModeEnabled) {
            binding.llContainerSelected.visible()
            binding.llContainerSelected.setThrottledClickListener {
                onSelectClicked.invoke(bindingAdapterPosition)
            }
            binding.ivThumbnail.setThrottledClickListener {
                onPreviewClicked.invoke(bindingAdapterPosition)
            }
        } else {
            binding.llContainerSelected.gone()
            binding.ivThumbnail.setThrottledClickListener {
                onSelectClicked.invoke(bindingAdapterPosition)
            }
        }
    }

    fun setCameraPreviewEnabled(enabled: Boolean) {
        if (enabled) {
            binding.previewCamera.visibility = View.VISIBLE
            binding.vCameraBackground.visibility = View.GONE
        } else {
            binding.previewCamera.visibility = View.GONE
            binding.vCameraBackground.visibility = View.VISIBLE
        }
    }

    fun bind(pickerTile: PickerTile) {
        if (pickerTile.isCameraTile) {
            binding.ivThumbnail.setImageResource(android.R.color.transparent)
            binding.ivThumbnail.loadGlide(builder.cameraTileDrawable)
            builder.cameraTileDrawable
                .setTintColor(itemView.context, com.meera.core.R.color.ui_white)
            binding.ivThumbnail.setBackgroundResource(builder.cameraTileBackgroundResId)
            binding.ivThumbnail.visibility = View.VISIBLE
            binding.ivSelected.visibility = View.GONE
            binding.previewCamera.visibility = View.VISIBLE
            if (ActivityCompat.checkSelfPermission(
                    itemView.context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                cameraProvider.startCameraPreviewViewHolder(lifecycleOwner, binding.previewCamera)
            }
        } else if (pickerTile.isGalleryTile) {
            binding.ivThumbnail.setBackgroundResource(builder.galleryTileBackgroundResId)
            binding.ivThumbnail.setImageDrawable(builder.galleryTileDrawable)
            binding.ivSelected.visibility = View.GONE
        } else {
            binding.previewCamera.visibility = View.GONE
            binding.ivThumbnail.setBackgroundResource(0)
            Glide.with(itemView.context.applicationContext)
                .load(pickerTile.imageUri)
                .apply(
                    RequestOptions().centerCrop()
                        .placeholder(R.drawable.ic_gallery)
                        .error(R.drawable.img_error)
                )
                .into(binding.ivThumbnail)
            binding.ivSelected.visibility = View.VISIBLE
            binding.ivSelected.setBackgroundResource(0)
            if (pickerTile.isSelected) {
                binding.ivSelected.setBackgroundResource(R.drawable.meera_circle_tab_bg)
                binding.ivSelected.text = "${pickerTile.counter}"
            } else {
                binding.ivSelected.setBackgroundResource(R.drawable.white_ring_bg)
                binding.ivSelected.text = ""
            }
        }

        if (pickerTile.tileType == PickerTile.VIDEO) {
            binding.clVideo.visible()
            val minutes = TimeUnit.MILLISECONDS.toMinutes(pickerTile.duration)
            binding.tvVideoTime.text = String.format(
                Locale.getDefault(),
                "%02d:%02d",
                minutes,
                TimeUnit.MILLISECONDS.toSeconds(pickerTile.duration) % 60
            )
            val tintColorRes = when (minutes >= MAX_VIDEO_LENGTHS_MINUTES) {
                true -> R.color.uiKitColorAccentWrong
                else -> R.color.uiKitColorBackgroundInvers
            }
            binding.clVideo.setBackgroundTint(tintColorRes)
        } else {
            binding.clVideo.gone()
        }
    }
}
