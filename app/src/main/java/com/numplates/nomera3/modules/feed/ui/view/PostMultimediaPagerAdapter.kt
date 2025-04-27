package com.numplates.nomera3.modules.feed.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.MEDIA_VIDEO
import com.numplates.nomera3.databinding.ItemPostMultimediaPagerItemBinding
import com.numplates.nomera3.modules.feed.ui.PostCallback
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.presentation.VolumeStateCallback
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy.ZoomyProvider

class PostMultimediaPagerAdapter(
    val onItemClicked: (MediaAssetEntity, PostUIEntity?) -> Unit
): RecyclerView.Adapter<PostMultimediaPagerAdapter.PostMultimediaPagerViewHolder>() {

    private var mediaPreviewStrictHeight = 0
    private var mediaPreviewStrictWidth = 0

    private val differ = AsyncListDiffer(this, PostMultimediaAssetDiffItemCallback())

    var post: PostUIEntity? = null
    var zoomyProvider: ZoomyProvider? = null
    var postCallback: PostCallback? = null
    var volumeStateCallback: VolumeStateCallback? = null
    var canZoom: Boolean = true
    var contentManager: ISensitiveContentManager? = null

    fun bind(
        post: PostUIEntity,
        zoomyProvider: ZoomyProvider?,
        postCallback: PostCallback?,
        volumeStateCallback: VolumeStateCallback?,
        canZoom: Boolean,
        contentManager: ISensitiveContentManager?
    ) {
        this.post = post
        this.zoomyProvider = zoomyProvider
        this.postCallback = postCallback
        this.volumeStateCallback = volumeStateCallback
        this.canZoom = canZoom
        this.contentManager = contentManager
    }

    fun submitList(list: List<MediaAssetEntity>, commitCallback: (() -> Unit)? = null) {
        differ.submitList(list) {
            commitCallback?.invoke()
        }
    }

    fun isItemVideo(position: Int): Boolean {
        if (differ.currentList.isEmpty() || differ.currentList.size <= position) return false
        return differ.currentList[position].type == MEDIA_VIDEO
    }

    fun getCurrentVideoUrl(position: Int): String? {
        if (!isItemVideo(position)) return null
        return differ.currentList[position].video
    }

    fun getItem(position: Int): MediaAssetEntity? {
        if (differ.currentList.isEmpty() || differ.currentList.size <= position) return null
        return differ.currentList[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostMultimediaPagerViewHolder {
        val binding = ItemPostMultimediaPagerItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostMultimediaPagerViewHolder(binding)
    }
    override fun onBindViewHolder(holder: PostMultimediaPagerViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(
            item = item,
            itemPosition = position,
            onItemClicked = onItemClicked,
            mediaPreviewStrictHeight = mediaPreviewStrictHeight,
            mediaPreviewStrictWidth = mediaPreviewStrictWidth,
            zoomyProvider = zoomyProvider,
            post = post,
            postCallback = postCallback,
            volumeStateCallback = volumeStateCallback,
            canZoom = canZoom,
            contentManager = contentManager
        )
    }
    override fun getItemCount() = differ.currentList.size
    fun setStrictMeasures(mediaPreviewStrictHeight: Int, mediaPreviewStrictWidth: Int) {
        this.mediaPreviewStrictHeight = mediaPreviewStrictHeight
        this.mediaPreviewStrictWidth = mediaPreviewStrictWidth
    }

    class PostMultimediaPagerViewHolder(
        private val binding: ItemPostMultimediaPagerItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MediaAssetEntity,
            onItemClicked: (MediaAssetEntity, PostUIEntity?) -> Unit,
            itemPosition: Int,
            mediaPreviewStrictHeight: Int,
            mediaPreviewStrictWidth: Int,
            zoomyProvider: ZoomyProvider?,
            post: PostUIEntity?,
            postCallback: PostCallback?,
            volumeStateCallback: VolumeStateCallback?,
            canZoom: Boolean,
            contentManager: ISensitiveContentManager?
        ) {
            binding.apply {
                pmpivMediaItemView.apply {
                    bind(
                        onItemClicked = onItemClicked,
                        media = item,
                        position = itemPosition,
                        mediaStrictWidth = mediaPreviewStrictWidth,
                        mediaStrictHeight = mediaPreviewStrictHeight,
                        zoomyProvider = zoomyProvider,
                        post = post,
                        postCallback = postCallback,
                        volumeStateCallback = volumeStateCallback,
                        canZoom = canZoom,
                        contentManager = contentManager
                    )
                }
            }
        }

        fun getVideoPlayer() = binding.pmpivMediaItemView.getVideoPlayer()

        fun getVideoDuration() = binding.pmpivMediaItemView.getDurationView()

        fun startPlayingVideo(position: Long?) = binding.pmpivMediaItemView.startPlayingVideo(position)

        fun stopPlayingVideo() = binding.pmpivMediaItemView.stopPlayingVideo()

        fun showVideoDuration() = binding.pmpivMediaItemView.showVideoDuration()

        fun hideVideoDuration() = binding.pmpivMediaItemView.hideVideoDuration()

        fun updateVolumeState(volumeState: VolumeState) = binding.pmpivMediaItemView.updateVolumeState(volumeState)
    }

}

private class PostMultimediaAssetDiffItemCallback : DiffUtil.ItemCallback<MediaAssetEntity>() {
    override fun areItemsTheSame(
        oldItem: MediaAssetEntity,
        newItem: MediaAssetEntity
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: MediaAssetEntity,
        newItem: MediaAssetEntity
    ): Boolean {
        return oldItem == newItem
    }
}

