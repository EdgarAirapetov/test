package com.numplates.nomera3.modules.feed.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.MEDIA_VIDEO
import com.numplates.nomera3.databinding.MeeraItemPostMultimediaPagerItemBinding
import com.numplates.nomera3.modules.feed.ui.MeeraPostCallback
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.presentation.VolumeStateCallback
import com.numplates.nomera3.presentation.view.utils.zoomy.CanPerformZoom
import com.numplates.nomera3.presentation.view.utils.zoomy.ZoomListener
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy

class MeeraPostMultimediaPagerAdapter(
    var onItemClicked: ((MediaAssetEntity, PostUIEntity?) -> Unit)?
): RecyclerView.Adapter<MeeraPostMultimediaPagerAdapter.MeeraPostMultimediaPagerViewHolder>() {

    private var mediaPreviewStrictHeight = 0
    private var mediaPreviewStrictWidth = 0

    private var postMultimediaAssetDiffItemCallback : MeeraPostMultimediaAssetDiffItemCallback? = null
    private var differ: AsyncListDiffer<MediaAssetEntity>? = null

    var post: PostUIEntity? = null
    var zoomyProvider: Zoomy.ZoomyProvider? = null
    var zoomListener: ZoomListener? = null
    var postCallback: MeeraPostCallback? = null
    var volumeStateCallback: VolumeStateCallback? = null
    var canPerformZoom: CanPerformZoom? = null
    var contentManager: ISensitiveContentManager? = null

    fun bind(
        post: PostUIEntity,
        zoomyProvider: Zoomy.ZoomyProvider?,
        postCallback: MeeraPostCallback?,
        volumeStateCallback: VolumeStateCallback?,
        canPerformZoom: CanPerformZoom,
        contentManager: ISensitiveContentManager?,
        zoomListener : ZoomListener
    ) {
        postMultimediaAssetDiffItemCallback = MeeraPostMultimediaAssetDiffItemCallback().also { callback ->
            differ = AsyncListDiffer(this, callback)
        }

        this.post = post
        this.zoomyProvider = zoomyProvider
        this.zoomListener = zoomListener
        this.postCallback = postCallback
        this.volumeStateCallback = volumeStateCallback
        this.canPerformZoom = canPerformZoom
        this.contentManager = contentManager
    }

    fun unbind() {
        post = null
        zoomyProvider = null
        zoomListener = null
        postCallback = null
        volumeStateCallback = null
        canPerformZoom = null
        contentManager = null
        onItemClicked = null
    }

    fun submitList(list: List<MediaAssetEntity>, commitCallback: (() -> Unit)? = null) {
        differ?.submitList(list) {
            commitCallback?.invoke()
        }
    }

    fun isItemVideo(position: Int): Boolean {
        val differ = this.differ ?: return false
        if (differ.currentList.isEmpty() || differ.currentList.size <= position) return false
        return differ.currentList[position].type == MEDIA_VIDEO
    }

    fun getCurrentVideoUrl(position: Int): String? {
        if (!isItemVideo(position)) return null
        return differ?.currentList?.get(position)?.video
    }

    fun getItem(position: Int): MediaAssetEntity? {
        val differ = this.differ ?: return null
        if (differ.currentList.isEmpty() || differ.currentList.size <= position) return null
        return differ.currentList[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraPostMultimediaPagerViewHolder {
        val binding = MeeraItemPostMultimediaPagerItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MeeraPostMultimediaPagerViewHolder(binding)
    }
    override fun onBindViewHolder(holder: MeeraPostMultimediaPagerViewHolder, position: Int) {
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
            canPerformZoom = canPerformZoom,
            contentManager = contentManager,
            zoomListener = zoomListener
        )
    }

    override fun onViewRecycled(holder: MeeraPostMultimediaPagerViewHolder) {
        holder.clearResources()
        super.onViewRecycled(holder)
    }

    override fun onViewDetachedFromWindow(holder: MeeraPostMultimediaPagerViewHolder) {
        holder.clearResources()
        super.onViewDetachedFromWindow(holder)
    }

    override fun getItemCount() = differ?.currentList?.size?: 0

    fun setStrictMeasures(mediaPreviewStrictHeight: Int, mediaPreviewStrictWidth: Int) {
        this.mediaPreviewStrictHeight = mediaPreviewStrictHeight
        this.mediaPreviewStrictWidth = mediaPreviewStrictWidth
    }

    class MeeraPostMultimediaPagerViewHolder(
        private val binding: MeeraItemPostMultimediaPagerItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MediaAssetEntity,
            onItemClicked: ((MediaAssetEntity, PostUIEntity?) -> Unit)?,
            itemPosition: Int,
            mediaPreviewStrictHeight: Int,
            mediaPreviewStrictWidth: Int,
            zoomyProvider: Zoomy.ZoomyProvider?,
            post: PostUIEntity?,
            postCallback: MeeraPostCallback?,
            volumeStateCallback: VolumeStateCallback?,
            canPerformZoom: CanPerformZoom?,
            contentManager: ISensitiveContentManager?,
            zoomListener: ZoomListener?
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
                        canPerformZoom = canPerformZoom,
                        contentManager = contentManager,
                        zoomListener = zoomListener
                    )
                }
            }
        }

        fun clearResources() = binding.pmpivMediaItemView.unbind()

        fun getVideoPlayer() = binding.pmpivMediaItemView.getVideoPlayer()

        fun getVideoDuration() = binding.pmpivMediaItemView.getDurationView()

        fun startPlayingVideo(position: Long?) = binding.pmpivMediaItemView.startPlayingVideo(position)

        fun stopPlayingVideo() = binding.pmpivMediaItemView.stopPlayingVideo()

        fun showVideoDuration() = binding.pmpivMediaItemView.showVideoDuration()

        fun hideVideoDuration() = binding.pmpivMediaItemView.hideVideoDuration()

        fun updateVolumeState(volumeState: VolumeState) = binding.pmpivMediaItemView.updateVolumeState(volumeState)
    }

}

private class MeeraPostMultimediaAssetDiffItemCallback : DiffUtil.ItemCallback<MediaAssetEntity>() {
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
