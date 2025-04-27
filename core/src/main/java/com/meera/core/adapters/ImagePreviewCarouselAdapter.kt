package com.meera.core.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.meera.core.R
import com.meera.core.extensions.gone
import com.meera.core.extensions.inflate
import com.meera.core.extensions.visible
import com.meera.core.utils.getDurationSeconds
import kotlin.properties.Delegates

class ImagePreviewCarouselAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var time: Int? = null

    /**
     * List URLs
     */
    internal var collection: List<String> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    internal var clickListener: (String) -> Unit = { _ -> }


    override fun getItemCount(): Int = collection.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.item_image_carousel));
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vh = holder as ViewHolder
        vh.bind(collection[position], clickListener, time)
    }


    inner class ViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val container: FrameLayout = itemView.findViewById(R.id.llContent)
        private val ivPicture: ImageView = itemView.findViewById(R.id.ivPicture)
        private val videoContainer: ConstraintLayout = itemView.findViewById(R.id.cl_video)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_video_time)

        fun bind(url: String, clickListener: (String) -> Unit, duration: Int?) {
            if (collection.size == 1) {
                container.setPadding(0, 0, 0, 0)
            }

            Glide.with(itemView.context)
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivPicture)

            itemView.setOnClickListener { clickListener(url) }

            if (duration != null){
                videoContainer.visible()
                tvTime.text =  getDurationSeconds(duration)
            } else {
                videoContainer.gone()
            }
        }
    }

}
