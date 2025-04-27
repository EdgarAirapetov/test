package com.numplates.nomera3.presentation.view.utils.tedbottompicker.adapter

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.utils.camera.CameraProvider
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.model.PickerTile
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.viewholder.MeeraGalleryViewHolder

class MeeraGalleryAdapter(
    private val builder: TedBottomSheetDialogFragment.BaseBuilder<*>,
    private val isMultiMediaModeEnabled: Boolean,
    private val cameraProvider: CameraProvider,
    private val lifecycleOwner: LifecycleOwner,
    private val onOpenImagePreview: OnOpenImagePreview,
    private val onItemClickListener: OnItemClickListener,
) : ListAdapter<PickerTile, MeeraGalleryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraGalleryViewHolder {
        val itemView = View.inflate(parent.context, R.layout.meera_tedbottompicker_grid_item, null)
        return MeeraGalleryViewHolder(
            itemView = itemView,
            builder = builder,
            isMultiMediaModeEnabled = isMultiMediaModeEnabled,
            cameraProvider = cameraProvider,
            lifecycleOwner = lifecycleOwner,
            onSelectClicked = { position -> onItemClickListener.onItemClick(getItem(position)) },
            onPreviewClicked = { position -> onOpenImagePreview.onOpenPreview(getItem(position), position) },
        )
    }

    override fun onBindViewHolder(holder: MeeraGalleryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    interface OnItemClickListener {
        fun onItemClick(pickerTile: PickerTile)
    }

    interface OnOpenImagePreview {
        fun onOpenPreview(pickerTile: PickerTile, position: Int)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PickerTile>() {
            override fun areContentsTheSame(oldItem: PickerTile, newItem: PickerTile): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: PickerTile, newItem: PickerTile): Boolean {
                return oldItem.imageUri == newItem.imageUri
            }
        }
    }
}
