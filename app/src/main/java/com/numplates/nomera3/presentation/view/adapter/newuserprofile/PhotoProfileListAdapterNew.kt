package com.numplates.nomera3.presentation.view.adapter.newuserprofile

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.meera.core.extensions.gone
import com.meera.core.extensions.inflate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.blur.UiKitRealtimeBlurView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.userprofile.ui.entity.GalleryPhotoEntity
import kotlin.properties.Delegates

class PhotoProfileListAdapterNew : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    internal var collection: List<GalleryPhotoEntity> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }
    internal var loadSuccess: (() -> Unit?)? = null
    internal var clickListener: (Int, ImageView) -> Unit = { _, _ -> }

    override fun getItemViewType(position: Int): Int = if (collection.isNotEmpty()) ITEM_TYPE_COMMON else ITEM_TYPE_ZERO

    override fun getItemCount(): Int = collection.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val photoViewHolder = PhotoProfileViewHolder(parent.inflate(R.layout.item_gallery_preview), loadSuccess)

        photoViewHolder.ivPicture.setThrottledClickListener(PHOTO_CLICK_DELAY) {
            clickListener(photoViewHolder.adapterPosition, photoViewHolder.ivPicture)
        }

        return when (viewType) {
            ITEM_TYPE_COMMON -> photoViewHolder
            ITEM_TYPE_ZERO -> ZeroItemViewHolder(parent.inflate(R.layout.item_zero))
            else -> ZeroItemViewHolder(parent.inflate(R.layout.item_zero))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_TYPE_COMMON -> {
                val commonVh = holder as PhotoProfileViewHolder
                commonVh.bind(collection[position])
            }
        }
    }

    class PhotoProfileViewHolder(
        itemView: View,
        private val loadSuccess: (() -> Unit?)? = null
    ) : RecyclerView.ViewHolder(itemView) {

        val ivPicture: ImageView = itemView.findViewById(R.id.ivPicture)
        val ukrbvSensitive: UiKitRealtimeBlurView = itemView.findViewById(R.id.ukrbv_sensitive)
        val tvSensitive: TextView = itemView.findViewById(R.id.tv_sensitive)

        fun bind(item: GalleryPhotoEntity) {
            Glide.with(itemView.context).load(item.link).override(Target.SIZE_ORIGINAL).fitCenter()
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(
                    object : RequestListener<Drawable?> {
                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable?>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            loadSuccess?.invoke()
                            return false
                        }

                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable?>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }
                    }
                )
                .into(ivPicture)
            if (item.isAdult) {
                ukrbvSensitive.visible()
                tvSensitive.visible()
            } else {
                ukrbvSensitive.gone()
                tvSensitive.gone()

            }
        }
    }

    class ZeroItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        const val ITEM_TYPE_COMMON = 1
        const val ITEM_TYPE_ZERO = 0
        const val PHOTO_CLICK_DELAY = 2500L
    }
}
