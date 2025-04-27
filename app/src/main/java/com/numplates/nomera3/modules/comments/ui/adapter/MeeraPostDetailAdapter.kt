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
import com.numplates.nomera3.modules.feed.ui.MeeraPostCallback
import com.numplates.nomera3.modules.feed.ui.adapter.FeedType
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraBasePostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraImagePostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraMultimediaPostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraPostProgressHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraRepostViewHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraVideoPostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraVideoRepostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.ShimmerPostDetailViewHolder
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.AllRemoteStyleFormatter
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.presentation.VolumeStateCallback
import com.numplates.nomera3.presentation.view.ui.VideoViewHolder
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy

class MeeraPostDetailAdapter(
    val blurHelper: BlurHelper,
    val contentManager: ISensitiveContentManager,
    val postCallback: MeeraPostCallback,
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
    val currentList: List<PostUIEntity>
        get() = post

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
        if (holder is MeeraBasePostHolder) {
            holder.initCallbacks(
                meeraPostCallback = postCallback,
                contentManager = contentManager,
                audioFeedHelper = audioFeedHelper,
                blurHelper = blurHelper,
                zoomyProvider = zoomyProvider,
                volumeStateCallback = volumeStateCallback
            )
        }

        if (payloads.isNotEmpty() && payloads[0] is UIPostUpdate && holder is MeeraBasePostHolder) {
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
        if (holder is MeeraBasePostHolder) {
            holder.clearResources()
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
                MeeraMultimediaPostHolder(
                    view = getLayout(parent, R.layout.meera_item_post),
                    parentWidth = parentWidth,
                    needToShowCommunityLabel = needToShowCommunityLabel,
                    isPostsWithBackground = featureTogglesContainer.postsWithBackgroundFeatureToggle.isEnabled,
                    isNeedMediaPositioning = featureTogglesContainer.postMediaPositioningFeatureToggle.isEnabled
                )
            }
            FeedType.IMAGE_POST.viewType, FeedType.IMAGE_POST_VIP.viewType -> {
                MeeraImagePostHolder(
                    view = getLayout(parent, R.layout.meera_item_post),
                    parentWidth = parentWidth,
                    needToShowCommunityLabel = needToShowCommunityLabel,
                    isPostsWithBackground = featureTogglesContainer.postsWithBackgroundFeatureToggle.isEnabled,
                    isNeedMediaPositioning = featureTogglesContainer.postMediaPositioningFeatureToggle.isEnabled
                )
            }
            FeedType.REPOST.viewType, FeedType.REPOST_VIP.viewType -> {
                MeeraRepostViewHolder(
                    view = getLayout(parent, R.layout.meera_item_repost),
                    parentWeight = parentWidth,
                    needToShowCommunityLabel = needToShowCommunityLabel,
                    isPostsWithBackgroundEnabled = featureTogglesContainer.postsWithBackgroundFeatureToggle.isEnabled,
                    isNeedMediaPositioning = featureTogglesContainer.postMediaPositioningFeatureToggle.isEnabled,
                    isSetRepostMode = true
                )
            }
            FeedType.VIDEO_POST.viewType, FeedType.VIDEO_POST_VIP.viewType -> {
                MeeraVideoPostHolder(
                    cacheUtilTool = cacheUtil,
                    view = getLayout(parent, R.layout.meera_item_post),
                    parentWidth = parentWidth,
                    needToShowCommunityLabel = needToShowCommunityLabel,
                    isPostsWithBackgroundEnabled = featureTogglesContainer.postsWithBackgroundFeatureToggle.isEnabled
                )
            }
            FeedType.VIDEO_REPOST.viewType, FeedType.VIDEO_REPOST_VIP.viewType -> {
                MeeraVideoRepostHolder(
                    cacheUtilTool = cacheUtil,
                    view = getLayout(parent, R.layout.meera_item_repost),
                    parentWeight = parentWidth,
                    needToShowCommunityLabel = needToShowCommunityLabel,
                    isPostsWithBackgroundEnabled = featureTogglesContainer.postsWithBackgroundFeatureToggle.isEnabled
                )
            }

            FeedType.EMPTY_PLACEHOLDER.viewType -> {
                EmptyCommentsViewHolder(parent.inflate(R.layout.meera_item_empty_comments_placeholder))
            }

            FeedType.SHIMMER_PLACEHOLDER.viewType -> ShimmerPostDetailViewHolder(parent)
            FeedType.PROGRESS.viewType -> MeeraPostProgressHolder(parent.inflate(R.layout.meera_progress_view))

            else -> throw IllegalArgumentException("UnsupportedViewType")
        }.apply {
            (this as? MeeraBasePostHolder)?.isEventsEnabled = featureTogglesContainer.mapEventsFeatureToggle.isEnabled
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = post[position]
        if (holder is MeeraBasePostHolder) {
            holder.needToShowRepostBtn = isNeedToShowRepostBtn
            holder.postDetailsMode = postDetailsMode
            holder.isInSnippet = postDetailsMode == PostDetailsMode.EVENT_SNIPPET
            holder.initCallbacks(
                meeraPostCallback = postCallback,
                contentManager = contentManager,
                audioFeedHelper = audioFeedHelper,
                blurHelper = blurHelper,
                zoomyProvider = zoomyProvider,
                volumeStateCallback = volumeStateCallback
            )
        }
        when (item.feedType) {
            FeedType.IMAGE_POST, FeedType.IMAGE_POST_VIP -> {
                (holder as MeeraImagePostHolder).bind(item)
                holder.showExpandMediaIndicator()
            }
            FeedType.MULTIMEDIA_POST -> {
                (holder as MeeraMultimediaPostHolder).bind(item)
                holder.showExpandMediaIndicator()
                videoBinded(holder)
            }
            FeedType.VIDEO_POST, FeedType.VIDEO_POST_VIP -> {
                (holder as MeeraVideoPostHolder).bind(item)
                videoBinded(holder)
            }
            FeedType.REPOST, FeedType.REPOST_VIP -> (holder as
                MeeraRepostViewHolder).bind(item)
            FeedType.VIDEO_REPOST, FeedType.VIDEO_REPOST_VIP -> {
                (holder as MeeraVideoRepostHolder).bind(item)
                videoBinded(holder)
            }
            FeedType.PROGRESS -> (holder as MeeraPostProgressHolder).bind()
            else -> Unit
        }
        bindListener(item)
        if (holder is MeeraBasePostHolder && isNeedToShowFlyingReactions) {
            holder.playFlyingReactions(postLatestReactionType)
            isNeedToShowFlyingReactions = false
            postLatestReactionType = null
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder is MeeraBasePostHolder) {
            holder.initCallbacks(
                meeraPostCallback = postCallback,
                contentManager = contentManager,
                audioFeedHelper = audioFeedHelper,
                blurHelper = blurHelper,
                zoomyProvider = zoomyProvider,
                volumeStateCallback = volumeStateCallback
            )
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
