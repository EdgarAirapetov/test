package com.numplates.nomera3.presentation.view.adapter.profilephoto

import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.blur.UiKitRealtimeBlurView
import com.numplates.nomera3.R

class ProfilePhotoAdapter(
    displayMetrics: DisplayMetrics, callback: ProfilePhotoCallback
) : PagedListAdapter<GridPhotoRecyclerModel, RecyclerView.ViewHolder>(diffCallback) {

    private val pxWidth = displayMetrics.widthPixels
    private var adapterInteractor: ProfilePhotoCallback = callback

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ITEM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_grid_photo, parent, false)
                PhotoViewHolder(view)
            }

            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_grid_photo_title, parent, false)
                TitleViewHolder(view)
            }

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_ITEM -> (holder as PhotoViewHolder).bind(getItem(position), position)
            else -> Unit
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position) as GridPhotoRecyclerModel
        return when (item.type) {
            GridPhotoRecyclerModel.TYPE_PHOTO -> TYPE_ITEM
            else -> TYPE_EMPTY
        }
    }


    inner class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var img: ImageView = view.findViewById(R.id.iv_grid_photo)
        private val ukrbvSensitive: UiKitRealtimeBlurView = itemView.findViewById(R.id.ukrbv_sensitive)
        private var tvSensitive: TextView = view.findViewById(R.id.tv_sensitive)

        private lateinit var photoModel: GridPhotoRecyclerModel
        private var photoPos: Int = 0

        fun bind(item: GridPhotoRecyclerModel?, pos: Int) {
            this.photoPos = pos
            item?.let {
                photoModel = it

                img.layoutParams.height = pxWidth / 3
                img.layoutParams.width = pxWidth / 3
                img.setThrottledClickListener {
                    var headers = 0
                    for (i in photoPos downTo 0) {
                        if (getItem(i)?.type == GridPhotoRecyclerModel.TYPE_TITLE) headers++
                    }
                    it.photo?.let { photo ->
                        adapterInteractor.onPhotoClicked(photoPos - headers)
                    }
                }

                photoModel.photo?.image?.url?.let { url ->
                    img.loadGlide(url)
                }
                if (photoModel.photo?.isAdult == true) {
                    tvSensitive.visible()
                    ukrbvSensitive.visible()
                } else {
                    tvSensitive.gone()
                    ukrbvSensitive.gone()
                }
            }
        }
    }

    inner class TitleViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var photoModel: GridPhotoRecyclerModel
        private var title: TextView = view.findViewById(R.id.tv_item_grid_photo_title)
        fun bind(item: GridPhotoRecyclerModel?) {
            item?.let {
                photoModel = it
                it.date?.let { date ->
                    title.text = date
                }
            }
        }


    }


    companion object {

        const val TYPE_FULL = 1
        const val TYPE_ITEM = 2
        const val TYPE_EMPTY = 3

        private val diffCallback = object : DiffUtil.ItemCallback<GridPhotoRecyclerModel>() {
            override fun areItemsTheSame(oldI: GridPhotoRecyclerModel, newI: GridPhotoRecyclerModel): Boolean {
                //Timber.d("areItemsTheSame called with walue = ${oldItem.id == newItem.id}")
                val oldItem = oldI.photo
                val newItem = newI.photo
                if (oldItem != null && newItem != null) {
                    return oldItem.id == newItem.id
                }
                return false
            }


            /**
             * Note that in kotlin, == checking on data classes compares all contents, but in Java,
             * typically you'll implement Object#equals, and use it to compare object contents.
             */
            override fun areContentsTheSame(oldI: GridPhotoRecyclerModel, newI: GridPhotoRecyclerModel): Boolean {
                val oldItem = oldI.photo
                val newItem = newI.photo
                // BUG FIX: blink items
                if (oldItem != null && newItem != null) {
                    return oldItem.image.url == newItem.image.url

                }
                return false
            }
        }
    }

    interface ProfilePhotoCallback {
        fun onPhotoClicked(position: Int)
    }
}
