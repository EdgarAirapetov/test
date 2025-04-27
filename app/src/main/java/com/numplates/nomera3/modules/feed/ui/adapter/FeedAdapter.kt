package com.numplates.nomera3.modules.feed.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.adapters.AsyncViewCache
import com.meera.core.extensions.inflate
import com.meera.core.utils.blur.BlurHelper
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemFeedPostsViewedBinding
import com.numplates.nomera3.databinding.ItemRoadReferralBinding
import com.numplates.nomera3.databinding.ItemRoadSuggestionsBinding
import com.numplates.nomera3.databinding.ItemRoadSyncContactsBinding
import com.numplates.nomera3.databinding.MomentCarouselShimmerBinding
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.ui.CacheUtil
import com.numplates.nomera3.modules.feed.ui.PostCallback
import com.numplates.nomera3.modules.feed.ui.data.MOMENTS_POST_ID
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.viewholder.BasePostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.CreatePostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.FeatureHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.ImagePostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MomentsViewHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MultimediaPostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.PostProgressHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.PostsViewedViewHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.RateUsHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.RepostViewHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.RoadReferralHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.RoadSuggestionsViewHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.RoadSyncContactsHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.ShimmerFeedViewHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.ShimmerMomentViewHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.VideoPostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.VideoRepostHolder
import com.numplates.nomera3.modules.moments.show.presentation.MomentCallback
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.AllRemoteStyleFormatter
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.presentation.VolumeStateCallback
import com.numplates.nomera3.presentation.view.utils.inflateBinding
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy
import java.util.concurrent.TimeUnit

class FeedAdapter(
    val blurHelper: BlurHelper,
    val contentManager: ISensitiveContentManager,
    val postCallback: PostCallback,
    private val volumeStateCallback: VolumeStateCallback,
    private val zoomyProvider: Zoomy.ZoomyProvider,
    val cacheUtil: CacheUtil,
    private val audioFeedHelper: AudioFeedHelper,
    private val formatter: AllRemoteStyleFormatter,
    private val lifecycleOwner: LifecycleOwner,
    private val needToShowCommunityLabel: Boolean = true,
    private val momentCallback: MomentCallback,
    private val featureTogglesContainer: FeatureTogglesContainer
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var isNeedToShowRepostBtn = true

    private val collection = mutableListOf<PostUIEntity>()
    private var timeLastUpdatedMinutes = 0L

    private var asyncPostViewCache: AsyncViewCache? = null
    private var asyncRepostViewCache: AsyncViewCache? = null

    private fun getCachedView(parent: ViewGroup, @LayoutRes layoutRes: Int): View {
        return when (layoutRes) {
            R.layout.item_post -> asyncPostViewCache?.getView() ?: getLayout(parent, layoutRes)
            R.layout.item_repost -> asyncRepostViewCache?.getView() ?: getLayout(parent, layoutRes)
            else -> getLayout(parent, layoutRes)
        }
    }

    private fun getCurrentTimeMinutes() = TimeUnit.MINUTES.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)

    private fun getLayout(parent: ViewGroup, @LayoutRes layoutRes: Int) =
        LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)

    fun submitList(data: List<PostUIEntity>) {
        val currentTimeMinutes = getCurrentTimeMinutes()
        val shouldUpdateTime = if (currentTimeMinutes > timeLastUpdatedMinutes) {
            timeLastUpdatedMinutes = currentTimeMinutes
            true
        } else false
        submitList(data, isShouldUpdateTime = shouldUpdateTime)
    }

    fun submitList(data: List<PostUIEntity>, isShouldUpdateTime: Boolean) {
        val diffCallback = FeedDiffCallback(collection, data, isShouldUpdateTime)
        DiffUtil.calculateDiff(diffCallback).dispatchUpdatesTo(this)
        collection.clear()
        collection.addAll(data)
    }

    //ищем пост по id и обновляем без пайлоад
    fun updateItemByPostId(id: Long) {
        collection.forEachIndexed { index, postUIEntity ->
            if (postUIEntity.postId == id || postUIEntity.parentPost?.postId == id) {
                notifyItemChanged(index)
            }
        }
    }

    //если позиция null ищет и обновляет через пайлоад если не null обновляет по позиции
    fun updateItem(payload: UIPostUpdate, adapterPos: Int?) {
        if (payload is UIPostUpdate.UpdateUserMomentsState) {
            updateItems(payload)
            return
        }
        if (adapterPos == null) {
            updateItem(payload)
            return
        }
        if (!isCorrectPosition(adapterPos)) {
            return
        }

        updateModel(adapterPos, payload)
        notifyItemChanged(adapterPos, payload)
    }

    fun updateItems(payload: UIPostUpdate.UpdateUserMomentsState) {
        collection.forEachIndexed { index, postUIEntity ->
            if (payload.postMomentsBlock != null && postUIEntity.feedType == FeedType.MOMENTS) {
                updateModel(index, payload.postMomentsBlock)
            } else if (payload.userId == postUIEntity.getUserId()) {
                updateModel(index, payload)
                notifyItemChanged(index, payload)
            }
        }
    }

    //ищем пост по id и обновляем через пайлоад
    fun updateItem(payload: UIPostUpdate) {
        collection.forEachIndexed { index, postUIEntity ->
            if (payload.postId != null && postUIEntity.postId == payload.postId) {
                updateModel(index, payload)
                notifyItemChanged(index, payload)
            }
        }
    }

    fun updateVolumeState(volumeState: VolumeState, visiblePositions: Pair<Int, Int>? = null) {
        val finalPositions = visiblePositions ?: Pair(0, collection.size - 1)
        for (position in finalPositions.first..finalPositions.second) {
            if (isCorrectPosition(position)) {
                val postUIEntity = collection[position]
                if (postUIEntity.feedType != FeedType.MOMENTS) {
                    val payload = UIPostUpdate.UpdateVolumeState(postUIEntity.postId, volumeState)
                    notifyItemChanged(position, payload)
                }
            }
        }
    }

    private fun updateModel(index: Int, payload: UIPostUpdate) {
        if (isCorrectPosition(index)) {
            collection[index] = collection[index].updateModel(payload)
        }
    }

    fun updateModel(index: Int, post: PostUIEntity) {
        if (isCorrectPosition(index)) {
            collection[index] = post
            notifyItemChanged(index)
        }
    }

    fun stopCurrentAudio(position: Int) {
        audioFeedHelper.apply {
            if (position == getHolderPosition()) {
                stopPlaying()
            }
        }
    }

    fun getMomentsItemPosition(): Int {
        return collection.indexOfFirst { it.postId == MOMENTS_POST_ID }
    }

    fun scrollMomentsToStart(viewHolder: RecyclerView.ViewHolder?) {
        if (viewHolder != null && viewHolder is MomentsViewHolder) {
            viewHolder.scrollMomentsToStart()
        }
    }

    private fun isCorrectPosition(pos: Int) = pos >= 0 && pos < collection.size

    fun showLoader(show: Boolean) {
        if (show) {
            collection.add(PostUIEntity(feedType = FeedType.PROGRESS))
            notifyItemInserted(collection.size - 1)
        } else {
            val index = collection.indexOfFirst { it.feedType == FeedType.PROGRESS }
            if (isCorrectPosition(index)) {
                collection.removeAt(index)
                notifyItemRemoved(index)
            }
        }
    }

    fun getItem(position: Int): PostUIEntity? =
        if (collection.size > 0 && position < collection.size) collection[position] else null

    fun getPositionById(postId: Long): Int {
        return collection.indexOfFirst { post -> post.postId == postId }
    }

    override fun getItemCount(): Int = collection.size

    override fun getItemViewType(position: Int) = collection.getOrNull(position)?.feedType?.viewType ?: UNDEFINED_VIEW_TYPE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val parentWidth = parent.width
        return when (viewType) {
            FeedType.MOMENTS.viewType -> {
                MomentsViewHolder(
                    view = parent.inflate(R.layout.moments_post_viewholder),
                    postCallback = postCallback,
                    momentCallback = momentCallback,
                    lifecycleOwner = lifecycleOwner
                )
            }
            FeedType.MULTIMEDIA_POST.viewType -> {
                MultimediaPostHolder(
                    contentManager = contentManager,
                    blurHelper = blurHelper,
                    zoomyProvider = zoomyProvider,
                    postCallback = postCallback,
                    volumeStateCallback = volumeStateCallback,
                    view = getCachedView(parent, R.layout.item_post),
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
                    view = getCachedView(parent, R.layout.item_post),
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
                    view = getCachedView(parent, R.layout.item_repost),
                    contentManager = contentManager,
                    blurHelper = blurHelper,
                    zoomyProvider = zoomyProvider,
                    parentWeight = parentWidth,
                    audioFeedHelper = audioFeedHelper,
                    needToShowCommunityLabel = needToShowCommunityLabel,
                    isPostsWithBackgroundEnabled = featureTogglesContainer.postsWithBackgroundFeatureToggle.isEnabled,
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
                    view = getCachedView(parent, R.layout.item_post),
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
                    view = getCachedView(parent, R.layout.item_repost),
                    postCallback = postCallback,
                    volumeStateCallback = volumeStateCallback,
                    parentWeight = parentWidth,
                    audioFeedHelper = audioFeedHelper,
                    needToShowCommunityLabel = needToShowCommunityLabel,
                    isPostsWithBackgroundEnabled = featureTogglesContainer.postsWithBackgroundFeatureToggle.isEnabled
                )
            }
            FeedType.PROGRESS.viewType -> PostProgressHolder(parent.inflate(R.layout.progress_view))
            FeedType.CREATE_POST.viewType -> CreatePostHolder(postCallback, parent.inflate(R.layout.layout_new_post))
            FeedType.ANNOUNCEMENT.viewType -> FeatureHolder(
                cacheUtil = cacheUtil,
                postCallback = postCallback,
                volumeStateCallback = volumeStateCallback,
                blurHelper = blurHelper,
                zoomyProvider = zoomyProvider,
                view = parent.inflate(R.layout.item_feature),
                parentWidth = parentWidth,
                audioFeedHelper = audioFeedHelper,
            )
            FeedType.SHIMMER_PLACEHOLDER.viewType -> ShimmerFeedViewHolder(parent)
            FeedType.SHIMMER_MOMENTS_PLACEHOLDER.viewType -> ShimmerMomentViewHolder(
                binding = MomentCarouselShimmerBinding.bind(parent.inflate(R.layout.moment_carousel_shimmer))
            )
            FeedType.RATE_US.viewType -> {
                RateUsHolder(
                    view = parent.inflate(R.layout.item_post_rate_us),
                    callback = postCallback
                )
            }
            FeedType.POSTS_VIEWED_ROAD.viewType,
            FeedType.POSTS_VIEWED_PROFILE.viewType,
            FeedType.POSTS_VIEWED_PROFILE_VIP.viewType -> {
                val binding = parent.inflateBinding(ItemFeedPostsViewedBinding::inflate)
                PostsViewedViewHolder(
                    binding = binding,
                    callback = postCallback
                )
            }

            FeedType.SYNC_CONTACTS.viewType -> {
                val binding = parent.inflateBinding(ItemRoadSyncContactsBinding::inflate)
                RoadSyncContactsHolder(binding, postCallback)
            }
            FeedType.REFERRAL.viewType -> {
                val binding = parent.inflateBinding(ItemRoadReferralBinding::inflate)
                RoadReferralHolder(binding, postCallback)
            }
            FeedType.SUGGESTIONS.viewType -> {
                val binding = parent.inflateBinding(ItemRoadSuggestionsBinding::inflate)
                RoadSuggestionsViewHolder(binding, postCallback)
            }
            else -> PostProgressHolder(parent.inflate(R.layout.progress_view))
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        (holder as? BasePostHolder)?.isEventsEnabled = featureTogglesContainer.mapEventsFeatureToggle.isEnabled
        val payloadValid = payloads.isNotEmpty() && payloads[0] is UIPostUpdate
        if (payloadValid && holder is BasePostHolder) {
            holder.updatePayload(payloads[0] as UIPostUpdate)
        } else if (payloadValid && holder is MomentsViewHolder) {
            holder.updatePayload(payloads[0] as UIPostUpdate.UpdateMoments)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
        val item = getItem(position)
        if ((item?.event == null && item?.parentPost?.event == null)
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
        if (holder is MomentsViewHolder) {
            momentCallback.onMomentsCarouselBecomeNotVisible()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = collection[position]
        if (holder is BasePostHolder) holder.needToShowRepostBtn = isNeedToShowRepostBtn
        when (item.feedType) {
            FeedType.MOMENTS ->
                (holder as MomentsViewHolder).bind(item)
            FeedType.MULTIMEDIA_POST -> (holder as MultimediaPostHolder).bind(item)
            FeedType.IMAGE_POST, FeedType.IMAGE_POST_VIP ->
                (holder as ImagePostHolder).bind(item)
            FeedType.VIDEO_POST, FeedType.VIDEO_POST_VIP ->
                (holder as VideoPostHolder).bind(item)
            FeedType.REPOST, FeedType.REPOST_VIP -> (holder as
                RepostViewHolder).bind(item)
            FeedType.VIDEO_REPOST, FeedType.VIDEO_REPOST_VIP ->
                (holder as VideoRepostHolder).bind(item)
            FeedType.PROGRESS -> (holder as PostProgressHolder).bind()
            FeedType.CREATE_POST -> (holder as CreatePostHolder).bind()
            FeedType.ANNOUNCEMENT -> (holder as FeatureHolder).bind(item)
            FeedType.RATE_US -> (holder as RateUsHolder).bind(item)
            FeedType.POSTS_VIEWED_ROAD,
            FeedType.POSTS_VIEWED_PROFILE, FeedType.POSTS_VIEWED_PROFILE_VIP ->
                (holder as PostsViewedViewHolder).bind(item.feedType)
            FeedType.REFERRAL -> (holder as RoadReferralHolder).bind(item)
            FeedType.SUGGESTIONS -> (holder as RoadSuggestionsViewHolder).bind(item)
            else -> Unit
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        asyncPostViewCache = AsyncViewCache(lifecycleOwner, R.layout.item_post, recyclerView)
        asyncRepostViewCache = AsyncViewCache(lifecycleOwner, R.layout.item_repost, recyclerView)
    }

    private class FeedDiffCallback(
        private val oldList: List<PostUIEntity>,
        private val newList: List<PostUIEntity>,
        private val shouldUpdateTime: Boolean
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldPostItem = oldList[oldItemPosition]
            val newPostItem = newList[newItemPosition]

            return oldPostItem.postId == newPostItem.postId &&
                oldPostItem.feedType == newPostItem.feedType &&
                oldPostItem.getVideoUrl() == newPostItem.getVideoUrl()
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldPost = oldList[oldItemPosition]
            val newPost = newList[newItemPosition]

            if (oldPost.feedType == FeedType.CREATE_POST && newPost.feedType == FeedType.CREATE_POST) {
                return true
            }
            if (oldPost.feedType == FeedType.POSTS_VIEWED_ROAD && newPost.feedType == FeedType.POSTS_VIEWED_ROAD) {
                return true
            }
            if (oldPost.feedType == FeedType.POSTS_VIEWED_PROFILE && newPost.feedType == FeedType.POSTS_VIEWED_PROFILE) {
                return true
            }
            if (oldPost.feedType == FeedType.SUGGESTIONS && newPost.feedType == FeedType.SUGGESTIONS) {
                return false
            }
            if (oldPost.feedType == FeedType.REFERRAL && newPost.feedType == FeedType.REFERRAL) {
                return false
            }

            return (!shouldUpdateTime &&
                oldPost.repostCount == newPost.repostCount &&
                oldPost.commentCount == newPost.commentCount &&
                oldPost.tagSpan == newPost.tagSpan &&
                oldPost.user == newPost.user &&
                oldPost.isPostSubscribed == newPost.isPostSubscribed &&
                oldPost.mainVehicle?.number == newPost.mainVehicle?.number &&
                oldPost.parentPost?.deleted == newPost.parentPost?.deleted &&
                oldPost.reactions?.equals(newPost.reactions) ?: false &&
                oldPost.needToShowFollowButton == newPost.needToShowFollowButton) &&
                oldPost.postUpdatingLoadingInfo == newPost.postUpdatingLoadingInfo &&
                oldPost.moments == newPost.moments &&
                oldPost.media == newPost.media &&
                oldPost.getVideoUrl() == newPost.getVideoUrl() &&
                (oldPost.backgroundUrl == newPost.backgroundUrl || oldPost.backgroundId == newPost.backgroundId) &&
                oldPost.assets == newPost.assets
        }
    }

    companion object {
        private const val UNDEFINED_VIEW_TYPE = -1
    }
}
