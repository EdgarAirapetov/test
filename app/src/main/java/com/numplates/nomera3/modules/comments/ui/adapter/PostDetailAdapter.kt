package com.numplates.nomera3.modules.comments.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.inflate
import com.meera.core.utils.blur.BlurHelper
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.comments.ui.entity.PostDetailsMode
import com.numplates.nomera3.modules.comments.ui.viewholder.EmptyCommentsViewHolder
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.ui.CacheUtil
import com.numplates.nomera3.modules.feed.ui.PostCallback
import com.numplates.nomera3.modules.feed.ui.adapter.FeedType
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.viewholder.BasePostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.ImagePostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MultimediaPostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.PostProgressHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.RepostViewHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.ShimmerPostDetailViewHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.VideoPostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.VideoRepostHolder
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.AllRemoteStyleFormatter
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.presentation.VolumeStateCallback
import com.numplates.nomera3.presentation.view.ui.VideoViewHolder
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy

class PostDetailAdapter(
    val blurHelper: BlurHelper,
    val contentManager: ISensitiveContentManager,
    val postCallback: PostCallback,
    val volumeStateCallback: VolumeStateCallback,
    private val zoomyProvider: Zoomy.ZoomyProvider,
    val cacheUtil: CacheUtil,
    private val audioFeedHelper: AudioFeedHelper,
    private val formatter: AllRemoteStyleFormatter,
    private val needToShowCommunityLabel: Boolean = true,
    private val postDetailsMode: PostDetailsMode,
    private val featureTogglesContainer: FeatureTogglesContainer
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val post = mutableListOf<PostUIEntity>()

    var isNeedToShowRepostBtn = true
    var isNeedToShowFlyingReactions = false
    var postLatestReactionType: ReactionType? = null
    var bindListener: (PostUIEntity) -> Unit = {}
    var videoBinded: (VideoViewHolder) -> Unit = {}

    fun submitList(list: List<PostUIEntity>) {
        val diffCallback = PostDetailDiffCallback(post, list)
        DiffUtil.calculateDiff(diffCallback).dispatchUpdatesTo(this)
        post.clear()
        post.addAll(list)
    }

    fun updatePost(payload: UIPostUpdate) {
        notifyItemChanged(0, payload)
    }

    fun forceUpdatePost() {
        if (post.size >= 0)
            notifyItemChanged(0)
    }

    fun updateItem(position: Int, payload: UIPostUpdate) {
        if (position >= post.size) return
        post[position] = post[position].updateModel(payload)
        notifyItemChanged(position, payload)
    }

    fun updateVolumeState(volumeState: VolumeState) {
        if (post.size > 0) {
            val postId = post[0].postId
            val payload = UIPostUpdate.UpdateVolumeState(postId, volumeState)
            notifyItemChanged(0, payload)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty() && payloads[0] is UIPostUpdate && holder is BasePostHolder) {
            holder.updatePayload(payloads[0] as UIPostUpdate)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
        val item = post[position]
        if ((item.event == null && item.parentPost?.event == null)
            || featureTogglesContainer.mapEventsFeatureToggle.isEnabled.not()
        ) {
            formatter.format(holder)
        } else {
            formatter.formatDefault(holder)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is BasePostHolder) {
            holder.clearResource()
        }
    }

    private fun getLayout(parent: ViewGroup, @LayoutRes layoutRes: Int) =
        LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)

    override fun getItemViewType(position: Int) =
        post[position].feedType.viewType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val parentWidth = parent.width
        return when (viewType) {
            FeedType.MULTIMEDIA_POST.viewType -> {
                MultimediaPostHolder(
                    contentManager = contentManager,
                    blurHelper = blurHelper,
                    zoomyProvider = zoomyProvider,
                    postCallback = postCallback,
                    volumeStateCallback = volumeStateCallback,
                    view = getLayout(parent, R.layout.item_post),
                    parentWidth = parentWidth,
                    audioFeedHelper = audioFeedHelper,
                    needToShowCommunityLabel = needToShowCommunityLabel,
                    isPostsWithBackground = featureTogglesContainer.postsWithBackgroundFeatureToggle.isEnabled,
                    isNeedMediaPositioning = featureTogglesContainer.postMediaPositioningFeatureToggle.isEnabled
                )
            }
            FeedType.IMAGE_POST.viewType, FeedType.IMAGE_POST_VIP.viewType -> {
                ImagePostHolder(
                    contentManager = contentManager,
                    blurHelper = blurHelper,
                    zoomyProvider = zoomyProvider,
                    postCallback = postCallback,
                    view = getLayout(parent, R.layout.item_post),
                    parentWidth = parentWidth,
                    audioFeedHelper = audioFeedHelper,
                    needToShowCommunityLabel = needToShowCommunityLabel,
                    isPostsWithBackground = featureTogglesContainer.postsWithBackgroundFeatureToggle.isEnabled,
                    isNeedMediaPositioning = featureTogglesContainer.postMediaPositioningFeatureToggle.isEnabled
                )
            }
            FeedType.REPOST.viewType, FeedType.REPOST_VIP.viewType -> {
                RepostViewHolder(
                    postCallback = postCallback,
                    view = getLayout(parent, R.layout.item_repost),
                    contentManager = contentManager,
                    blurHelper = blurHelper,
                    zoomyProvider = zoomyProvider,
                    parentWeight = parentWidth,
                    audioFeedHelper = audioFeedHelper,
                    needToShowCommunityLabel = needToShowCommunityLabel,
                    isNeedMediaPositioning = featureTogglesContainer.postMediaPositioningFeatureToggle.isEnabled
                )
            }
            FeedType.VIDEO_POST.viewType, FeedType.VIDEO_POST_VIP.viewType -> {
                VideoPostHolder(
                    cacheUtilTool = cacheUtil,
                    contentManager = contentManager,
                    blurHelper = blurHelper,
                    zoomyProvider = zoomyProvider,
                    postCallback = postCallback,
                    volumeStateCallback = volumeStateCallback,
                    view = getLayout(parent, R.layout.item_post),
                    parentWidth = parentWidth,
                    audioFeedHelper = audioFeedHelper,
                    needToShowCommunityLabel = needToShowCommunityLabel,
                    isPostsWithBackgroundEnabled = featureTogglesContainer.postsWithBackgroundFeatureToggle.isEnabled
                )
            }
            FeedType.VIDEO_REPOST.viewType, FeedType.VIDEO_REPOST_VIP.viewType -> {
                VideoRepostHolder(
                    cacheUtilTool = cacheUtil,
                    contentManager = contentManager,
                    blurHelper = blurHelper,
                    zoomyProvider = zoomyProvider,
                    view = getLayout(parent, R.layout.item_repost),
                    postCallback = postCallback,
                    volumeStateCallback = volumeStateCallback,
                    parentWeight = parentWidth,
                    audioFeedHelper = audioFeedHelper,
                    needToShowCommunityLabel = needToShowCommunityLabel,
                    isPostsWithBackgroundEnabled = featureTogglesContainer.postsWithBackgroundFeatureToggle.isEnabled
                )
            }

            FeedType.EMPTY_PLACEHOLDER.viewType -> {
                EmptyCommentsViewHolder(parent.inflate(R.layout.item_empty_comments_placeholder))
            }

            FeedType.SHIMMER_PLACEHOLDER.viewType -> ShimmerPostDetailViewHolder(parent)
            FeedType.PROGRESS.viewType -> PostProgressHolder(parent.inflate(R.layout.progress_view))

            else -> throw IllegalArgumentException("UnsupportedViewType")
        }.apply {
            (this as? BasePostHolder)?.isEventsEnabled = featureTogglesContainer.mapEventsFeatureToggle.isEnabled
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = post[position]
        if (holder is BasePostHolder) {
            holder.needToShowRepostBtn = isNeedToShowRepostBtn
            holder.postDetailsMode = postDetailsMode
            holder.isInSnippet = postDetailsMode == PostDetailsMode.EVENT_SNIPPET
        }
        when (item.feedType) {
            FeedType.IMAGE_POST, FeedType.IMAGE_POST_VIP -> {
                (holder as ImagePostHolder).bind(item)
                holder.showExpandMediaIndicator()
            }
            FeedType.MULTIMEDIA_POST -> {
                (holder as MultimediaPostHolder).bind(item)
                holder.showExpandMediaIndicator()
                videoBinded(holder)
            }
            FeedType.VIDEO_POST, FeedType.VIDEO_POST_VIP -> {
                (holder as VideoPostHolder).bind(item)
                videoBinded(holder)
            }
            FeedType.REPOST, FeedType.REPOST_VIP -> (holder as
                RepostViewHolder).bind(item)
            FeedType.VIDEO_REPOST, FeedType.VIDEO_REPOST_VIP -> {
                (holder as VideoRepostHolder).bind(item)
                videoBinded(holder)
            }
            FeedType.PROGRESS -> (holder as PostProgressHolder).bind()
            else -> Unit
        }
        bindListener(item)
        if (holder is BasePostHolder && isNeedToShowFlyingReactions) {
            holder.playFlyingReactions(postLatestReactionType)
            isNeedToShowFlyingReactions = false
            postLatestReactionType = null
        }
    }

    override fun getItemCount() = post.size

    private class PostDetailDiffCallback(
        private val oldList: List<PostUIEntity>,
        private val newList: List<PostUIEntity>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].postId == newList[newItemPosition].postId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
